package com.healthfamily.domain.entity;

import com.healthfamily.domain.constant.ConstitutionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "constitutions")
public class Constitution {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32)
    private ConstitutionType type;

    @Column(nullable = false)
    private String name; // e.g., "平和质"

    @Column(columnDefinition = "TEXT")
    private String description; // 体质解读

    @Column(columnDefinition = "TEXT")
    private String cause; // 成因

    @Column(columnDefinition = "TEXT")
    private String performance; // 表现

    @Column(name = "diet_advice", columnDefinition = "TEXT")
    private String dietAdvice; // 饮食建议

    @Column(name = "sport_advice", columnDefinition = "TEXT")
    private String sportAdvice; // 运动建议

    @Column(name = "lifestyle_advice", columnDefinition = "TEXT")
    private String lifestyleAdvice; // 生活建议
}
