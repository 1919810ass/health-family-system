package com.healthfamily.web.controller;

import com.healthfamily.domain.entity.AssessmentOption;
import com.healthfamily.domain.entity.AssessmentQuestion;
import com.healthfamily.domain.entity.AssessmentQuestionnaire;
import com.healthfamily.service.AdminQuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/questionnaires")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionnaireController {

    private final AdminQuestionnaireService questionnaireService;

    @GetMapping
    public List<AssessmentQuestionnaire> getAllQuestionnaires() {
        return questionnaireService.getAllQuestionnaires();
    }

    @GetMapping("/{id}")
    public AssessmentQuestionnaire getQuestionnaireById(@PathVariable Long id) {
        return questionnaireService.getQuestionnaireById(id);
    }

    @PostMapping
    public AssessmentQuestionnaire createQuestionnaire(@RequestBody AssessmentQuestionnaire questionnaire) {
        return questionnaireService.createQuestionnaire(questionnaire);
    }

    @PutMapping("/{id}")
    public AssessmentQuestionnaire updateQuestionnaire(@PathVariable Long id, @RequestBody AssessmentQuestionnaire details) {
        return questionnaireService.updateQuestionnaire(id, details);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestionnaire(@PathVariable Long id) {
        questionnaireService.deleteQuestionnaire(id);
        return ResponseEntity.noContent().build();
    }

    // --- Questions ---
    @PostMapping("/{id}/questions")
    public AssessmentQuestion addQuestion(@PathVariable Long id, @RequestBody AssessmentQuestion question) {
        return questionnaireService.addQuestionToQuestionnaire(id, question);
    }

    @PutMapping("/questions/{questionId}")
    public AssessmentQuestion updateQuestion(@PathVariable Long questionId, @RequestBody AssessmentQuestion details) {
        return questionnaireService.updateQuestion(questionId, details);
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        questionnaireService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    // --- Options ---
    @PostMapping("/questions/{questionId}/options")
    public AssessmentOption addOption(@PathVariable Long questionId, @RequestBody AssessmentOption option) {
        return questionnaireService.addOptionToQuestion(questionId, option);
    }

    @PutMapping("/options/{optionId}")
    public AssessmentOption updateOption(@PathVariable Long optionId, @RequestBody AssessmentOption details) {
        return questionnaireService.updateOption(optionId, details);
    }

    @DeleteMapping("/options/{optionId}")
    public ResponseEntity<Void> deleteOption(@PathVariable Long optionId) {
        questionnaireService.deleteOption(optionId);
        return ResponseEntity.noContent().build();
    }
}
