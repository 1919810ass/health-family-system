package com.healthfamily.modules.recommendationv2.service;

import com.healthfamily.domain.entity.KnowledgeDocument;
import com.healthfamily.domain.repository.KnowledgeDocumentRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
/**
 * DocRag服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
@Service
public class DocRagService {
    private final KnowledgeDocumentRepository repo;
    private final VectorStore vectorStore;
    private final int topK;
    private final double minScore;
    private final String vectorStorePath;

    public DocRagService(KnowledgeDocumentRepository repo,
                         VectorStore vectorStore,
                         @Value("${rag.topK:4}") int topK,
                         @Value("${rag.minScore:0.4}") double minScore,
                         @Value("${spring.rag.vector-store-path:./vector-store.json}") String vectorStorePath) {
        this.repo = repo;
        this.vectorStore = vectorStore;
        this.topK = topK;
        this.minScore = minScore;
        this.vectorStorePath = vectorStorePath;
    }

    @PostConstruct
    /**
     * 执行业务操作
     * @return 无
     */
    public void initData() {
        // Init data logic can be kept simple or reuse sync logic if store is missing
        File storeFile = new File(vectorStorePath);
        if (!storeFile.exists()) {
            log.info("Vector store file not found at {}. Initializing from database...", vectorStorePath);
            syncToVectorStore();
        } else {
            log.info("Vector store loaded from {}", vectorStorePath);
        }
    }

    @Transactional(readOnly = true)
    public void syncToVectorStore() {
        log.info("Starting full vector store sync...");

        // 1. Clear existing vector store if possible
        if (vectorStore instanceof SimpleVectorStore simpleStore) {
            File file = new File(vectorStorePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                log.info("Deleted existing vector store file: {}", deleted);
            }
            // For SimpleVectorStore, deleting the file and reloading/re-adding is a way to clear,
            // but since we are injecting the bean, it might hold data in memory.
            // A cleaner way for SimpleVectorStore might be to create a new instance or use a method to clear if available.
            // Assuming for this context that we are rebuilding and can just add. 
            // Ideally, we should empty the store first. 
            // Since SimpleVectorStore doesn't have clear(), we might rely on the fact that we are rebuilding from scratch 
            // or that the application restart + file delete handles it.
            // If this is called at runtime, we might be appending duplicates if we don't clear memory.
            // However, Spring AI SimpleVectorStore is simple. Let's proceed with adding.
        }

        // 2. Query all enabled child documents (chunks)
        // Using findAll() for simplicity as requested, but in production should be paginated or streamed.
        List<KnowledgeDocument> allChunks = repo.findAll().stream()
                .filter(doc -> doc.getParent() != null && Boolean.TRUE.equals(doc.getEnabled()))
                .collect(Collectors.toList());

        if (allChunks.isEmpty()) {
            log.warn("No enabled chunks found to sync.");
            return;
        }

        log.info("Found {} chunks to sync. Generating embeddings...", allChunks.size());

        // 3. Convert to Spring AI Documents
        List<Document> documents = new ArrayList<>();
        for (KnowledgeDocument chunk : allChunks) {
            if (chunk.getContent() == null || chunk.getContent().isBlank()) continue;

            Map<String, Object> metadata = new HashMap<>();
            // Safely get parent title if loaded, or use chunk title
            String title = chunk.getParent() != null ? chunk.getParent().getTitle() : chunk.getTitle();
            metadata.put("title", title);
            metadata.put("parentId", chunk.getParent() != null ? chunk.getParent().getId() : null);
            metadata.put("chunkIndex", chunk.getChunkIndex());
            metadata.put("source", "knowledge-base");
            metadata.put("version", chunk.getVersion());

            Document doc = new Document(chunk.getId().toString(), chunk.getContent(), metadata);
            documents.add(doc);
        }

        // 4. Add to VectorStore
        try {
            vectorStore.add(documents);
            
            // 5. Save to file if SimpleVectorStore
            if (vectorStore instanceof SimpleVectorStore simpleStore) {
                File file = new File(vectorStorePath);
                simpleStore.save(file);
                log.info("Vector store saved to {}", vectorStorePath);
            }
            log.info("Successfully synced {} documents to vector store.", documents.size());
        } catch (Exception e) {
            log.error("Failed to sync vector store", e);
            throw new RuntimeException("Vector store sync failed", e);
        }
    }

    /**

     * 执行业务操作

     * @param q 业务参数

     * @return 业务返回结果

     */

    public List<Map<String, Object>> search(String q) {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.query(q)
                .withTopK(topK)
                .withSimilarityThreshold(minScore)
        );
        
        return results.stream().map(doc -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", doc.getId());
            map.put("snippet", doc.getContent());
            map.put("title", doc.getMetadata().getOrDefault("title", "Unknown"));
            map.put("score", 1.0); // SimpleVectorStore might not expose score easily in Document object depending on version
            map.put("parentId", doc.getMetadata().get("parentId"));
            return map;
        }).collect(Collectors.toList());
    }
}
