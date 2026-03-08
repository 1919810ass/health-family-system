package com.healthfamily.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * FoodRecognition服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * 不上传图片时使用 qwen2.5:7b 文字分析；上传图片时使用 qwen2.5vl:3b 图像识别分析卡路里。
 * </p>
 */
public interface FoodRecognitionService {

    /** 通用识别：返回食物名与置信度 */
    RecognitionResult recognize(Path imagePath);

    /**
     * 饮食图片分析：使用视觉模型识别图中食物并估算热量（千卡）
     * @param imagePath 本地图片路径
     * @return 每项含 name、calories，及总热量
     */
    DietAnalysisResult dietAnalysisFromImage(Path imagePath);

    record RecognitionResult(String foodName, Double confidence) {}

    record DietAnalysisResult(List<Map<String, Object>> items, double totalCalories) {}
}
