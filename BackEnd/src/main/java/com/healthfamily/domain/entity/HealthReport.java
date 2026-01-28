package com.healthfamily.domain.entity;

import com.healthfamily.domain.constant.ReportStatus;
import com.healthfamily.domain.constant.ReportType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "health_reports")
public class HealthReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "report_name")
    private String reportName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReportType reportType;

    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReportStatus status;

    @Lob
    @Column(name = "ocr_data", columnDefinition = "text")
    private String ocrData;

    @Lob
    @Column(name = "interpretation", columnDefinition = "text")
    private String interpretation;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "progress_percent")
    private Integer progressPercent;

    @Column(name = "progress_stage", length = 128)
    private String progressStage;

    @Column(name = "doctor_comment", columnDefinition = "text")
    private String doctorComment;

    @Column(name = "doctor_comment_time")
    private LocalDateTime doctorCommentTime;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
