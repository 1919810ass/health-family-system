package com.healthfamily.domain.entity;

import com.healthfamily.domain.constant.Sex;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "profiles")
public class Profile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Sex sex;

    private LocalDate birthday;

    @Column(name = "height_cm", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal heightCm;

    @Column(name = "weight_kg", columnDefinition = "DECIMAL(5,2)")
    private BigDecimal weightKg;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String allergies;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferences", columnDefinition = "json")
    private String preferences;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "goals", columnDefinition = "json")
    private String goals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tcm_tags", columnDefinition = "json")
    private String tcmTags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "health_tags", columnDefinition = "json")
    private String healthTags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lifestyle", columnDefinition = "json")
    private String lifestyle;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

