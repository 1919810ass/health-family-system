package com.healthfamily.service;

/**
 * FoodRecognition服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import java.nio.file.Path;

public interface FoodRecognitionService {
    RecognitionResult recognize(Path imagePath);

    record RecognitionResult(String foodName, Double confidence) {}
}
