package com.healthfamily.modules.recommendationv2.repository;

import com.healthfamily.modules.recommendationv2.domain.SuggestionFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestionFeedbackRepository extends JpaRepository<SuggestionFeedback, Long> {
}
