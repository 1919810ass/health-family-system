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
@Table(name = "tcm_personalized_plans")
public class TcmPersonalizedPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_name", nullable = false, length = 255)
    private String planName;

    @Column(name = "primary_constitution", nullable = false, length = 32)
    private String primaryConstitution;

    @Column(name = "plan_content", nullable = false, columnDefinition = "json")
    private String planContent;

    @Column(name = "seasonal_recommendations", columnDefinition = "json")
    private String seasonalRecommendations;

    @Column(name = "priority_recommendations", columnDefinition = "json")
    private String priorityRecommendations;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}