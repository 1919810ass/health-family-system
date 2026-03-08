package com.healthfamily.domain.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "assessment_questionnaires")
public class AssessmentQuestionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title; // e.g., "中医体质九分法问卷"

    @Column(columnDefinition = "TEXT")
    private String description;

    @JsonManagedReference
    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssessmentQuestion> questions = new ArrayList<>();

    private boolean isActive;
}
