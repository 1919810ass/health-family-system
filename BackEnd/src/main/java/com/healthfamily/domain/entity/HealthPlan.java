package com.healthfamily.domain.entity;

import com.healthfamily.domain.constant.FrequencyType;
import com.healthfamily.domain.constant.HealthPlanStatus;
import com.healthfamily.domain.constant.HealthPlanType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康计划与随访实体
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "health_plans")
public class HealthPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 创建计划的医生ID
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    /**
     * 患者用户ID（计划针对的成员）
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_user_id", nullable = false)
    private User patient;

    /**
     * 家庭ID
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    /**
     * 计划类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private HealthPlanType type;

    /**
     * 计划标题
     */
    @Column(nullable = false, length = 128)
    private String title;

    /**
     * 计划描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 开始日期
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * 结束日期（可选，null表示无结束日期）
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * 执行频率类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type", nullable = false, length = 32)
    private FrequencyType frequencyType;

    /**
     * 频率值（如每周3次、每2周1次等，配合frequencyType使用）
     */
    @Column(name = "frequency_value")
    private Integer frequencyValue;

    /**
     * 频率详情（如每周一三五、每月1号和15号等）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "frequency_detail", columnDefinition = "json")
    private String frequencyDetail;

    /**
     * 目标指标（如血压<140/90、体重<70kg等）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_indicators", columnDefinition = "json")
    private String targetIndicators;

    /**
     * 提醒策略（提醒时间、提前多久提醒、提醒渠道等）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reminder_strategy", columnDefinition = "json")
    private String reminderStrategy;

    /**
     * 计划状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private HealthPlanStatus status = HealthPlanStatus.ACTIVE;

    /**
     * 完成度（0-100）
     */
    @Column(name = "completion_rate", columnDefinition = "DECIMAL(5,2)")
    @Builder.Default
    private BigDecimal completionRate = BigDecimal.ZERO;

    /**
     * 依从性（0-100）
     */
    @Column(name = "compliance_rate", columnDefinition = "DECIMAL(5,2)")
    @Builder.Default
    private BigDecimal complianceRate = BigDecimal.ZERO;

    /**
     * 元数据（关联的日志ID、提醒ID等）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "json")
    private String metadataJson;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

