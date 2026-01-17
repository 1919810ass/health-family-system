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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医生扩展信息实体
 * 存储医生的专业信息、认证状态等
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "doctor_profiles")
public class DoctorProfile {

    @Id
    @Column(name = "doctor_id")
    private Long doctorId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "doctor_id")
    private User doctor;

    /**
     * 执业医院
     */
    @Column(name = "hospital", length = 100)
    private String hospital;

    /**
     * 科室
     */
    @Column(name = "department", length = 50)
    private String department;

    /**
     * 专业领域
     */
    @Column(name = "specialty", length = 100)
    private String specialty;

    /**
     * 职称（如：主任医师、副主任医师、主治医师等）
     */
    @Column(name = "title", length = 50)
    private String title;

    /**
     * 医生简介
     */
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 认证状态：PENDING-待审核，APPROVED-已认证，REJECTED-已拒绝
     */
    @Column(name = "certification_status", length = 20, nullable = false)
    private String certificationStatus;

    /**
     * 认证时间
     */
    @Column(name = "certified_at")
    private LocalDateTime certifiedAt;

    /**
     * 认证审核人ID（管理员ID）
     */
    @Column(name = "certified_by")
    private Long certifiedBy;

    /**
     * 拒绝原因
     */
    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    /**
     * 执业证书编号
     */
    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    /**
     * 执业证书图片（存储路径）
     */
    @Column(name = "license_image", length = 255)
    private String licenseImage;

    /**
     * 身份证号（加密存储）
     */
    @Column(name = "id_card", length = 50)
    private String idCard;

    /**
     * 身份证正面图片（存储路径）
     */
    @Column(name = "id_card_front", length = 255)
    private String idCardFront;

    /**
     * 身份证反面图片（存储路径）
     */
    @Column(name = "id_card_back", length = 255)
    private String idCardBack;

    /**
     * 评分（平均评分，0-5分）
     */
    @Column(name = "rating", columnDefinition = "DECIMAL(3,2)")
    private BigDecimal rating;

    /**
     * 评分人数
     */
    @Column(name = "rating_count")
    private Integer ratingCount;

    /**
     * 服务用户数（累计服务过的家庭数）
     */
    @Column(name = "service_count")
    private Integer serviceCount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 判断是否已认证
     */
    public boolean isCertified() {
        return "APPROVED".equals(certificationStatus);
    }

    /**
     * 判断是否待审核
     */
    public boolean isPending() {
        return "PENDING".equals(certificationStatus);
    }

    /**
     * 判断是否已拒绝
     */
    public boolean isRejected() {
        return "REJECTED".equals(certificationStatus);
    }
}
