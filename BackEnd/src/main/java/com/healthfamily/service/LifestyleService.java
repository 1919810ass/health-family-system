package com.healthfamily.service;

import com.healthfamily.web.dto.*;

import java.util.List;

public interface LifestyleService {
    DietIngestResponse ingestDiet(Long requesterId, DietIngestRequest request);
    List<RecipeRecommendResponse> recommendRecipes(Long requesterId, RecipeRecommendRequest request);
    String analyzeDietWeekly(Long requesterId, Long familyId, Boolean dp, Double epsilon);
    void recordExercise(Long requesterId, ExerciseRecordRequest request);
    String suggestExercise(Long requesterId);
    void recordSleep(Long requesterId, SleepRecordRequest request);
    String analyzeSleep(Long requesterId);
    /**
     * 上传饮食图片并识别
     */
    ImageUploadResponse uploadDietImage(Long requesterId, org.springframework.web.multipart.MultipartFile file);
}
