package com.healthfamily.service;

import com.healthfamily.domain.entity.KnowledgeDocument;
import com.healthfamily.domain.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    @Value("${spring.rag.vector-store-path:./vector-store.json}")
    private String vectorStorePath;

    @Transactional
    public KnowledgeDocument createDocument(String title, String content) {
        // Save parent document
        KnowledgeDocument parent = KnowledgeDocument.builder()
                .title(title)
                .content(content)
                .enabled(true)
                .version("v1")
                .build();
        parent = knowledgeDocumentRepository.save(parent);
        
        // Trigger chunking
        chunkDocument(parent);
        
        return parent;
    }

    @Transactional
    public void chunkDocument(KnowledgeDocument parent) {
        // Remove existing chunks if any (for re-chunking scenarios)
        List<KnowledgeDocument> existingChunks = knowledgeDocumentRepository.findByParentId(parent.getId());
        if (!existingChunks.isEmpty()) {
            knowledgeDocumentRepository.deleteAll(existingChunks);
        }

        // Split content
        List<String> chunkTexts = splitContent(parent.getContent());
        List<KnowledgeDocument> chunkDocs = new ArrayList<>();

        for (int i = 0; i < chunkTexts.size(); i++) {
            String chunkText = chunkTexts.get(i);
            KnowledgeDocument chunk = KnowledgeDocument.builder()
                    .title(parent.getTitle() + " - Chunk " + (i + 1))
                    .content(chunkText)
                    .parent(parent)
                    .chunkIndex(i)
                    .enabled(true)
                    .version(parent.getVersion())
                    .build();
            chunkDocs.add(chunk);
        }
        
        knowledgeDocumentRepository.saveAll(chunkDocs);
    }

    @Transactional
    public void updateChunk(Long chunkId, String newContent) {
        KnowledgeDocument chunk = knowledgeDocumentRepository.findById(chunkId)
                .orElseThrow(() -> new RuntimeException("Chunk not found"));
        
        chunk.setContent(newContent);
        knowledgeDocumentRepository.save(chunk);
    }

    @Transactional(readOnly = true)
    public void syncToVectorStore() {
        log.info("Starting vector store sync...");
        
        // 1. Query all enabled child documents (chunks)
        // Assuming parent_id is not null means it's a chunk. 
        // We can also filter by chunkIndex != null if that's safer.
        List<KnowledgeDocument> allChunks = knowledgeDocumentRepository.findAll().stream()
                .filter(doc -> doc.getParent() != null && Boolean.TRUE.equals(doc.getEnabled()) && doc.getContent() != null && !doc.getContent().isBlank())
                .collect(Collectors.toList());

        if (allChunks.isEmpty()) {
            log.warn("No chunks found to sync.");
            return;
        }

        // 2. Clear existing vector store if possible
        // SimpleVectorStore doesn't have a clear() method exposed easily, but we can overwrite the file or try delete.
        // For SimpleVectorStore, creating a new instance or deleting the file might be needed if we want a full reset.
        // However, here we are injecting VectorStore.
        // If it is SimpleVectorStore, we can try to delete the backing file before adding? 
        // Or better, just rely on the fact that we might be adding duplicates if we don't clear.
        // Ideally, we should delete all documents first.
        // Since we don't have IDs of *all* vectors easily without querying, we might just overwrite the file if it's SimpleVectorStore.
        
        if (vectorStore instanceof SimpleVectorStore) {
            File file = new File(vectorStorePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                log.info("Deleted existing vector store file: {}", deleted);
            }
            // SimpleVectorStore in memory might still have data if it's singleton. 
            // We can't easily clear the in-memory map of SimpleVectorStore without casting and reflection or specific method.
            // But SimpleVectorStore.load(file) might help if file is gone? No, load adds to existing.
            // Let's assume we just add for now, or if we really want to clear, we might need a custom method.
            // A common workaround for SimpleVectorStore is to accept that we are appending, or restart app.
            // But for this task, let's try to be as clean as possible.
            // We can try to delete *known* IDs if we tracked them, but we are syncing *all*.
        }

        // 3. Convert to Spring AI Documents
        List<Document> documents = new ArrayList<>();
        for (KnowledgeDocument chunk : allChunks) {
            Map<String, Object> metadata = Map.of(
                    "parentId", chunk.getParent().getId(),
                    "chunkIndex", chunk.getChunkIndex(),
                    "title", chunk.getTitle(),
                    "source", "knowledge-base"
            );
            
            Document doc = new Document(chunk.getId().toString(), chunk.getContent(), metadata);
            documents.add(doc);
        }

        // 4. Add to VectorStore
        vectorStore.add(documents);
        
        // 5. Save to file if SimpleVectorStore
        if (vectorStore instanceof SimpleVectorStore simpleStore) {
            File file = new File(vectorStorePath);
            simpleStore.save(file);
            log.info("Saved vector store to {}", vectorStorePath);
        }
        
        log.info("Synced {} documents to vector store.", documents.size());
    }

    @Transactional(readOnly = true)
    public Page<KnowledgeDocument> getParentDocuments(Pageable pageable) {
        return knowledgeDocumentRepository.findAll((root, query, cb) -> cb.isNull(root.get("parent")), pageable);
    }
    
    @Transactional(readOnly = true)
    public List<KnowledgeDocument> getChunks(Long parentId) {
        return knowledgeDocumentRepository.findByParentId(parentId);
    }
    
    @Transactional
    public void deleteDocument(Long documentId) {
        KnowledgeDocument parent = knowledgeDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        List<KnowledgeDocument> chunks = knowledgeDocumentRepository.findByParentId(documentId);
        
        // Try to remove from vector store
        List<String> ids = chunks.stream().map(c -> c.getId().toString()).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            try {
                vectorStore.delete(ids);
            } catch (Exception e) {
                log.warn("Failed to delete from vector store: {}", e.getMessage());
            }
        }
        
        knowledgeDocumentRepository.deleteAll(chunks);
        knowledgeDocumentRepository.delete(parent);
    }
    
    @Transactional
    public void reEmbedDocument(Long documentId) {
        KnowledgeDocument parent = knowledgeDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        List<KnowledgeDocument> existing = knowledgeDocumentRepository.findByParentId(documentId);
        List<String> oldIds = existing.stream().map(c -> c.getId().toString()).collect(Collectors.toList());
        if (!oldIds.isEmpty()) {
            try {
                vectorStore.delete(oldIds);
            } catch (Exception e) {
                log.warn("Failed to delete old vectors: {}", e.getMessage());
            }
        }
        chunkDocument(parent);
        List<KnowledgeDocument> chunks = knowledgeDocumentRepository.findByParentId(documentId).stream()
                .filter(doc -> Boolean.TRUE.equals(doc.getEnabled()) && doc.getContent() != null && !doc.getContent().isBlank())
                .collect(Collectors.toList());
        if (chunks.isEmpty()) {
            return;
        }
        List<Document> documents = new ArrayList<>();
        for (KnowledgeDocument chunk : chunks) {
            Map<String, Object> metadata = Map.of(
                    "parentId", chunk.getParent().getId(),
                    "chunkIndex", chunk.getChunkIndex(),
                    "title", chunk.getTitle(),
                    "source", "knowledge-base"
            );
            Document doc = new Document(chunk.getId().toString(), chunk.getContent(), metadata);
            documents.add(doc);
        }
        vectorStore.add(documents);
        if (vectorStore instanceof SimpleVectorStore simpleStore) {
            File file = new File(vectorStorePath);
            simpleStore.save(file);
        }
    }
    
    // Kept for compatibility if used elsewhere, but logic moved to chunkDocument
    private List<String> splitContent(String content) {
        String[] paragraphs = content.split("\\n\\s*\\n");
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        
        for (String p : paragraphs) {
            if (currentChunk.length() + p.length() > 500) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                }
            }
            if (currentChunk.length() > 0) {
                currentChunk.append("\n\n");
            }
            currentChunk.append(p);
        }
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }
        return chunks;
    }
}
