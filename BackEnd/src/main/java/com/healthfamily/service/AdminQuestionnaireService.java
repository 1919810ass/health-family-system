package com.healthfamily.service;

import com.healthfamily.domain.entity.AssessmentOption;
import com.healthfamily.domain.entity.AssessmentQuestion;
import com.healthfamily.domain.entity.AssessmentQuestionnaire;
import com.healthfamily.domain.repository.AssessmentOptionRepository;
import com.healthfamily.domain.repository.AssessmentQuestionRepository;
import com.healthfamily.domain.repository.AssessmentQuestionnaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminQuestionnaireService {

    private final AssessmentQuestionnaireRepository questionnaireRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentOptionRepository assessmentOptionRepository;

    public List<AssessmentQuestionnaire> getAllQuestionnaires() {
        return questionnaireRepository.findAll();
    }

    public AssessmentQuestionnaire getQuestionnaireById(Long id) {
        return questionnaireRepository.findById(id).orElseThrow(() -> new RuntimeException("Questionnaire not found"));
    }

    public AssessmentQuestionnaire createQuestionnaire(AssessmentQuestionnaire questionnaire) {
        return questionnaireRepository.save(questionnaire);
    }

    public AssessmentQuestionnaire updateQuestionnaire(Long id, AssessmentQuestionnaire details) {
        AssessmentQuestionnaire questionnaire = getQuestionnaireById(id);
        questionnaire.setTitle(details.getTitle());
        questionnaire.setDescription(details.getDescription());
        questionnaire.setActive(details.isActive());
        return questionnaireRepository.save(questionnaire);
    }

    public void deleteQuestionnaire(Long id) {
        questionnaireRepository.deleteById(id);
    }

    // --- Question Management ---

    public AssessmentQuestion addQuestionToQuestionnaire(Long questionnaireId, AssessmentQuestion question) {
        AssessmentQuestionnaire questionnaire = getQuestionnaireById(questionnaireId);
        question.setQuestionnaire(questionnaire);
        return assessmentQuestionRepository.save(question);
    }

    public AssessmentQuestion updateQuestion(Long questionId, AssessmentQuestion details) {
        AssessmentQuestion question = assessmentQuestionRepository.findById(questionId).orElseThrow(() -> new RuntimeException("Question not found"));
        question.setText(details.getText());
        question.setDisplayOrder(details.getDisplayOrder());
        question.setConstitutionType(details.getConstitutionType());
        return assessmentQuestionRepository.save(question);
    }

    public void deleteQuestion(Long questionId) {
        assessmentQuestionRepository.deleteById(questionId);
    }

    // --- Option Management ---

    public AssessmentOption addOptionToQuestion(Long questionId, AssessmentOption option) {
        AssessmentQuestion question = assessmentQuestionRepository.findById(questionId).orElseThrow(() -> new RuntimeException("Question not found"));
        option.setQuestion(question);
        return assessmentOptionRepository.save(option);
    }

    public AssessmentOption updateOption(Long optionId, AssessmentOption details) {
        AssessmentOption option = assessmentOptionRepository.findById(optionId).orElseThrow(() -> new RuntimeException("Option not found"));
        option.setText(details.getText());
        option.setScore(details.getScore());
        return assessmentOptionRepository.save(option);
    }

    public void deleteOption(Long optionId) {
        assessmentOptionRepository.deleteById(optionId);
    }
}
