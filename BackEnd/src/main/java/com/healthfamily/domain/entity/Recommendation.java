package com.healthfamily.domain.entity;

import com.healthfamily.domain.constant.RecommendationCategory;
import com.healthfamily.domain.constant.RecommendationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "for_date", nullable = false)
    private LocalDate forDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RecommendationCategory category;

    @Column(name = "items_json", nullable = false, columnDefinition = "json")
    private String itemsJson;

    @Column(name = "evidence_json", nullable = false, columnDefinition = "json")
    private String evidenceJson;

    @Column(length = 32, nullable = false)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private RecommendationStatus status;

    @Column(name = "ai_model", length = 64)
    private String aiModel;

    @Column(name = "prompt_version", length = 32)
    private String promptVersion;

    @Column(name = "metadata_json", columnDefinition = "json")
    private String metadataJson;

    @Column(name = "is_accepted", nullable = false)
    private Boolean accepted;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

