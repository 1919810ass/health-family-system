package com.healthfamily.domain.entity;

import com.healthfamily.domain.constant.RuleCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rules")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RuleCategory category;

    @Column(name = "condition_json", nullable = false, columnDefinition = "json")
    private String conditionJson;

    @Column(name = "action_json", nullable = false, columnDefinition = "json")
    private String actionJson;

    @Column(nullable = false, columnDefinition = "DECIMAL(6,3)")
    private BigDecimal weight;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(length = 255)
    private String source;

    @Column(length = 32, nullable = false)
    private String version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

