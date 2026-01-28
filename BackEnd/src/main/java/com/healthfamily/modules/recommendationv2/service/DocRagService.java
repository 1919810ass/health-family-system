package com.healthfamily.modules.recommendationv2.service;

import com.healthfamily.modules.recommendationv2.domain.DocFragment;
import com.healthfamily.modules.recommendationv2.repository.DocFragmentRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocRagService {
    private final DocFragmentRepository repo;
    private final VectorStore vectorStore;
    private final int topK;
    private final double minScore;
    private final String vectorStorePath;

    public DocRagService(DocFragmentRepository repo,
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
    public void initData() {
        File storeFile = new File(vectorStorePath);
        if (!storeFile.exists()) {
            log.info("Vector store file not found at {}. Initializing from database...", vectorStorePath);
            List<DocFragment> fragments = repo.findAll();
            if (fragments.isEmpty()) {
                log.warn("No documents found in database to initialize vector store.");
                return;
            }

            log.info("Found {} documents in database. Generating embeddings...", fragments.size());
            List<Document> documents = new ArrayList<>();
            for (DocFragment f : fragments) {
                if (f.getContent() == null || f.getContent().isBlank()) continue;
                
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("title", f.getTitle() != null ? f.getTitle() : "Untitled");
                metadata.put("source", f.getSource() != null ? f.getSource() : "Unknown");
                metadata.put("fragmentId", f.getId());

                Document doc = new Document(f.getId().toString(), f.getContent(), metadata);
                documents.add(doc);
            }

            try {
                vectorStore.add(documents);
                if (vectorStore instanceof SimpleVectorStore simpleStore) {
                    simpleStore.save(storeFile);
                    log.info("Vector store saved to {}", vectorStorePath);
                }
            } catch (Exception e) {
                log.error("Failed to initialize vector store", e);
            }
        } else {
            log.info("Vector store loaded from {}", vectorStorePath);
        }
    }

    public List<Map<String, Object>> search(String q) {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.query(q)
                        .withTopK(topK)
                        .withSimilarityThreshold(minScore)
        );

        return results.stream().map(doc -> {
            Map<String, Object> m = new HashMap<>();
            m.put("title", doc.getMetadata().getOrDefault("title", "Untitled"));
            String content = doc.getContent();
            m.put("snippet", content.length() > 300 ? content.substring(0, 300) + "..." : content);
            m.put("content", content); // Also return full content for RAG context
            m.put("source", doc.getMetadata().getOrDefault("source", "Unknown"));
            m.put("fragmentId", doc.getMetadata().get("fragmentId"));
            // SimpleVectorStore might not expose score in Document metadata by default in all versions, 
            // but usually it's not directly on the Document object unless we use a specific return type.
            // For now, we omit score or set a dummy one as it's not critical for the UI if sorted.
            // Actually, Spring AI 1.0.0 M5 Document might have score? No, it's usually in a wrapper.
            // similaritySearch returns List<Document>. 
            // If we need scores, we need similaritySearch(SearchRequest) which returns List<Document> but docs might have metadata 'distance'.
            // For SimpleVectorStore, it sorts by score.
            return m;
        }).collect(Collectors.toList());
    }
    
    // 供外部调用，手动刷新向量库
    public void refreshVectorStore() {
        File storeFile = new File(vectorStorePath);
        if (storeFile.exists()) {
            storeFile.delete();
        }
        if (vectorStore instanceof SimpleVectorStore simpleStore) {
            // SimpleVectorStore doesn't have a clear() method easily accessible without re-instantiating or reflection in some versions.
            // But since we persist to file, deleting file and restarting/reloading is one way.
            // Or we just re-read DB and add. SimpleVectorStore.add() will update if ID matches.
        }
        initData();
    }
}
