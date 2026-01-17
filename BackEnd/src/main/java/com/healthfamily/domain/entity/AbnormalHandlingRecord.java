package com.healthfamily.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "abnormal_handling_records")
public class AbnormalHandlingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id")
    private HealthAlert alert;  // 关联的异常记录

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;  // 处理医生

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private User patient;  // 涉及的患者

    @Column(name = "handling_action", length = 32)
    private String handlingAction;  // 处理动作：notify(发送提醒), call(电话联系), referral(转诊建议)

    @Column(name = "handling_content", columnDefinition = "TEXT")
    private String handlingContent;  // 处理内容

    @Column(name = "handling_note", columnDefinition = "TEXT")
    private String handlingNote;  // 处理备注

    @CreatedDate
    @Column(name = "handled_at", nullable = false, updatable = false)
    private LocalDateTime handledAt;  // 处理时间

    @Column(name = "follow_up_required")
    private Boolean followUpRequired;  // 是否需要后续跟踪

    @Column(name = "follow_up_time")
    private LocalDateTime followUpTime;  // 跟踪时间

    @Column(name = "follow_up_result", columnDefinition = "TEXT")
    private String followUpResult;  // 跟踪结果

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}