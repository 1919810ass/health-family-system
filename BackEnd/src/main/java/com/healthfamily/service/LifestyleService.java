package com.healthfamily.service;

import com.healthfamily.web.dto.*;

/**
 * Lifestyle服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import java.util.List;

public interface LifestyleService {
    DietIngestResponse ingestDiet(Long requesterId, DietIngestRequest request);
    List<RecipeRecommendResponse> recommendRecipes(Long requesterId, RecipeRecommendRequest request);
    String analyzeDietWeekly(Long requesterId, Long familyId, Boolean dp, Double epsilon);
    void recordExercise(Long requesterId, ExerciseRecordRequest request);
    String suggestExercise(Long requesterId);
    void recordSleep(Long requesterId, SleepRecordRequest request);
    String analyzeSleep(Long requesterId);
    void recordMood(Long requesterId, MoodRecordRequest request);
    String analyzeMood(Long requesterId);
    void recordVitals(Long requesterId, VitalsRecordRequest request);
    String analyzeVitals(Long requesterId);
    /**
     * 上传饮食图片并识别
     */
    ImageUploadResponse uploadDietImage(Long requesterId, org.springframework.web.multipart.MultipartFile file);
}
