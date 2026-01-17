package com.healthfamily.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医生病历记录/随访记录实体
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "doctor_notes")
public class DoctorNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 医生用户ID（创建记录的医生）
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    /**
     * 患者用户ID
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_user_id", nullable = false)
    private User patient;

    /**
     * 家庭ID（冗余字段，方便查询）
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    /**
     * 问诊日期
     */
    @Column(name = "consultation_date", nullable = false)
    private LocalDate consultationDate;

    /**
     * 主诉
     */
    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;

    /**
     * 既往史
     */
    @Column(name = "past_history", columnDefinition = "TEXT")
    private String pastHistory;

    /**
     * 用药情况
     */
    @Column(name = "medication", columnDefinition = "TEXT")
    private String medication;

    /**
     * 生活方式评估
     */
    @Column(name = "lifestyle_assessment", columnDefinition = "TEXT")
    private String lifestyleAssessment;

    /**
     * 诊疗意见
     */
    @Column(name = "diagnosis_opinion", columnDefinition = "TEXT")
    private String diagnosisOpinion;

    /**
     * 随访建议
     */
    @Column(name = "followup_suggestion", columnDefinition = "TEXT")
    private String followupSuggestion;

    /**
     * 备注（Markdown格式的完整内容，兼容简单富文本）
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoctorNote)) return false;
        DoctorNote that = (DoctorNote) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

