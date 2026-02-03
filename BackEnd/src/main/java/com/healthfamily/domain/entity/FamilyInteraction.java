package com.healthfamily.domain.entity;

import com.healthfamily.domain.constant.InteractionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "family_interactions")
public class FamilyInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InteractionType type;

    @Column(length = 500)
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;
}
