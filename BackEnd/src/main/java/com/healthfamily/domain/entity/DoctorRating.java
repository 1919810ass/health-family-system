package com.healthfamily.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 医生评分实体
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "doctor_ratings")
public class DoctorRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 被评价的医生ID
     */
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    /**
     * 评价人ID（家庭成员用户ID）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 评分（1-5）
     */
    @Column(nullable = false)
    private Integer rating;

    /**
     * 评价内容
     */
    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
