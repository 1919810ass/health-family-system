package com.healthfamily.service.recommendation;

public class RecommendationAiException extends RuntimeException {

    public RecommendationAiException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecommendationAiException(String message) {
        super(message);
    }
}


