package com.healthfamily.service.impl;

import com.healthfamily.service.FoodRecognitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Random;

@Slf4j
@Service
public class LocalFoodRecognitionServiceImpl implements FoodRecognitionService {

    private final Random random = new Random();

    @Override
    public RecognitionResult recognize(Path imagePath) {
        // Simulate local model inference latency (<500ms)
        try {
            Thread.sleep(random.nextInt(300) + 100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Mock logic: In a real scenario, this would load a TensorFlow/ONNX model
        // For demonstration, we assume the model is highly accurate for "Baozi"
        
        // Return high confidence "Baozi" result as requested by the user scenario
        return new RecognitionResult("包子", 0.98);
    }
}
