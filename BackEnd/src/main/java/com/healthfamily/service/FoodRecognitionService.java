package com.healthfamily.service;

import java.nio.file.Path;

public interface FoodRecognitionService {
    RecognitionResult recognize(Path imagePath);

    record RecognitionResult(String foodName, Double confidence) {}
}
