package com.healthfamily.modules.recommendationv2.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;

@Configuration
public class RagConfig {

    @Value("${spring.rag.vector-store-path:./vector-store.json}")
    private String vectorStorePath;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = new SimpleVectorStore(embeddingModel);
        
        File file = new File(vectorStorePath);
        if (file.exists()) {
            vectorStore.load(file);
        }
        
        return vectorStore;
    }
}
