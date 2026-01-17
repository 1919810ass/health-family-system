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
@Table(name = "tcm_knowledge_base")
public class TcmKnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private KnowledgeType type;

    @Column(name = "constitution_type", nullable = false, length = 32)
    private String constitutionType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "json")
    private String tags;

    @Column(name = "seasonality", columnDefinition = "json")
    private String seasonality;

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column(length = 50)
    private String duration;

    @Column(name = "contraindications", columnDefinition = "json")
    private String contraindications;

    @Column(name = "evidence_level", length = 10)
    @Enumerated(EnumType.STRING)
    private EvidenceLevel evidenceLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum KnowledgeType {
        DIET, TEA, ACUPUNCTURE, EXERCISE, SEASONAL, EMOTION, LIFESTYLE, HERBAL
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public enum EvidenceLevel {
        HIGH, MEDIUM, LOW
    }
}