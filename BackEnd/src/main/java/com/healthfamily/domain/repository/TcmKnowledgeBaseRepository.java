package com.healthfamily.domain.repository;

import com.healthfamily.domain.entity.TcmKnowledgeBase;
import com.healthfamily.domain.entity.TcmKnowledgeBase.KnowledgeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 中医体质知识Base数据访问接口
 * <p>
 * 基于 Spring Data JPA 的数据访问层，用于领域对象的 CRUD 与查询。
 * </p>
 */
import java.util.List;

public interface TcmKnowledgeBaseRepository extends JpaRepository<TcmKnowledgeBase, Long> {
    List<TcmKnowledgeBase> findByConstitutionType(String constitutionType);
    
    List<TcmKnowledgeBase> findByTypeAndConstitutionType(KnowledgeType type, String constitutionType);
    
    @Query("SELECT t FROM TcmKnowledgeBase t WHERE t.type = :type AND t.constitutionType = :constitutionType AND t.tags LIKE %:tag%")
    List<TcmKnowledgeBase> findByTypeAndConstitutionTypeAndTag(@Param("type") KnowledgeType type, 
                                                              @Param("constitutionType") String constitutionType, 
                                                              @Param("tag") String tag);
}