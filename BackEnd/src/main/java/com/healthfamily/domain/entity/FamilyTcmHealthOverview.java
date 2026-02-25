package com.healthfamily.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
/**
 * 家庭中医体质健康Overview实体类
 * <p>
 * 用于持久化领域数据，通常与数据库表一一对应，承载业务状态与属性。
 * </p>
 */
@Table(name = "family_tcm_health_overviews")
public class FamilyTcmHealthOverview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "overview_content", nullable = false, columnDefinition = "json")
    private String overviewContent;

    @Column(name = "constitution_distribution", columnDefinition = "json")
    private String constitutionDistribution;

    @Column(name = "family_recommendation", columnDefinition = "TEXT")
    private String familyRecommendation;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}