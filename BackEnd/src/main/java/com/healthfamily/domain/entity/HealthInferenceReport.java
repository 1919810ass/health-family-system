package com.healthfamily.domain.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 多维健康关联推理报告表
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "health_inference_report", indexes = {
        @Index(name = "idx_user_date", columnList = "user_id, report_date")
})
public class HealthInferenceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 报告归属日期
     */
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    /**
     * 推理时的体质类型快照(如: 阴虚质)
     */
    @Column(name = "constitution_snapshot", length = 100)
    private String constitutionSnapshot;

    /**
     * 投喂给AI的数据摘要(日志ID列表、异常指标等)
     * JSON 格式
     */
    @Column(name = "input_summary", columnDefinition = "JSON")
    private String inputSummary;

    /**
     * AI生成的深度推理报告(Markdown格式)
     */
    @Column(name = "ai_analysis_result", columnDefinition = "TEXT")
    private String aiAnalysisResult;

    /**
     * 用户是否已查阅
     */
    @Column(name = "is_viewed")
    private Boolean isViewed;

    @Column(name = "gmt_create")
    private LocalDateTime gmtCreate;

    @Column(name = "gmt_modified")
    private LocalDateTime gmtModified;

    @PrePersist
    protected void onCreate() {
        if (gmtCreate == null) {
            gmtCreate = LocalDateTime.now();
        }
        if (gmtModified == null) {
            gmtModified = LocalDateTime.now();
        }
        if (isViewed == null) {
            isViewed = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        gmtModified = LocalDateTime.now();
    }
}
