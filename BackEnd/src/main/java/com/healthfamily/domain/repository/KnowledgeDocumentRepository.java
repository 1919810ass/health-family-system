package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 知识Document数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.List;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByCategoryAndEnabledTrue(String category);

    List<KnowledgeDocument> findByEnabledTrue();

    List<KnowledgeDocument> findByParentId(Long parentId);

    @Query("SELECT k FROM KnowledgeDocument k WHERE k.enabled = true AND " +
           "(k.title LIKE %:keyword% OR k.content LIKE %:keyword%)")
    List<KnowledgeDocument> searchByKeyword(String keyword);
}

