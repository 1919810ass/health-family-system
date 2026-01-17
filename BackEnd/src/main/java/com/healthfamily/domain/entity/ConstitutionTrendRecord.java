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
@Table(name = "constitution_trend_records")
public class ConstitutionTrendRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "assessment_id", nullable = false)
    private Long assessmentId;

    @Column(name = "constitution_scores", nullable = false, columnDefinition = "json")
    private String constitutionScores;

    @Column(name = "primary_type", nullable = false, length = 32)
    private String primaryType;

    @Column(name = "trend_analysis", columnDefinition = "json")
    private String trendAnalysis;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}