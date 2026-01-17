package com.healthfamily.service.impl;

import com.healthfamily.domain.entity.KnowledgeDocument;
import com.healthfamily.domain.repository.KnowledgeDocumentRepository;
import com.healthfamily.service.HealthAiToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthAiToolServiceImpl implements HealthAiToolService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;

    @Override
    public Map<String, Object> queryDrugInfo(String drugName) {
        // 模拟药品信息查询（实际应该对接药品数据库或API）
        Map<String, Object> result = new HashMap<>();
        result.put("drugName", drugName);
        result.put("indications", "用于治疗高血压、心绞痛等");
        result.put("dosage", "成人一次1片，一日1次");
        result.put("contraindications", Arrays.asList("对本品过敏者禁用", "孕妇禁用"));
        result.put("interactions", Arrays.asList("与某些降压药合用需注意"));
        result.put("source", "药品说明书");
        
        log.info("查询药品信息: {}", drugName);
        return result;
    }

    @Override
    public Map<String, Object> getNearbyHospitals(String location, String department) {
        // 模拟医院查询（实际应该对接地图API或医院数据库）
        Map<String, Object> result = new HashMap<>();
        result.put("location", location);
        result.put("department", department);
        
        List<Map<String, Object>> hospitals = new ArrayList<>();
        Map<String, Object> hospital1 = new HashMap<>();
        hospital1.put("name", "市人民医院");
        hospital1.put("address", location + "附近");
        hospital1.put("distance", "2.5公里");
        hospital1.put("phone", "400-xxx-xxxx");
        hospitals.add(hospital1);
        
        result.put("hospitals", hospitals);
        result.put("count", hospitals.size());
        
        log.info("查询附近医院: {} - {}", location, department);
        return result;
    }

    @Override
    public Map<String, Object> queryHealthKnowledge(String keyword, String category) {
        // 从知识库查询
        List<KnowledgeDocument> documents;
        
        if (category != null && !category.isEmpty()) {
            documents = knowledgeDocumentRepository.findByCategoryAndEnabledTrue(category);
        } else {
            documents = knowledgeDocumentRepository.searchByKeyword(keyword);
        }
        
        // 过滤包含关键词的文档
        List<Map<String, Object>> results = new ArrayList<>();
        for (KnowledgeDocument doc : documents) {
            if (doc.getContent().contains(keyword) || doc.getTitle().contains(keyword)) {
                Map<String, Object> item = new HashMap<>();
                item.put("title", doc.getTitle());
                item.put("content", doc.getContent().substring(0, Math.min(500, doc.getContent().length())));
                item.put("source", doc.getSource());
                item.put("category", doc.getCategory());
                results.add(item);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("keyword", keyword);
        result.put("category", category);
        result.put("documents", results);
        result.put("count", results.size());
        
        log.info("查询健康知识: {} - {}", keyword, category);
        return result;
    }
}

