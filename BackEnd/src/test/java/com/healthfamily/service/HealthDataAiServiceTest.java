package com.healthfamily.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.repository.ProfileRepository;
import com.healthfamily.service.HealthDataAiService.AnomalyResult;
import com.healthfamily.service.impl.HealthDataAiServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthDataAiServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HealthLogRepository healthLogRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private HealthDataAiServiceImpl healthDataAiService;

    @Test
    void detectAnomaly_WithUnknownDataType_ShouldNotThrowNPE() {
        // Arrange
        Long userId = 1L;
        String dataType = "UNKNOWN";
        Double value = 100.0;

        // Mock repository behavior
        when(healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        AnomalyResult result = healthDataAiService.detectAnomaly(userId, dataType, value, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isAnomaly());
        assertNull(result.suggestion());
    }

    @Test
    void optimizeDietText_Fallback_ShouldEstimateNewFoods() {
        // Arrange
        String text = "煎饺5个，鸭腿2个，花生饼3个";
        // Force fallback path
        when(chatClientBuilder.build()).thenThrow(new RuntimeException("AI Service Unavailable"));

        // Act
        java.util.Map<String, Object> result = healthDataAiService.optimizeDietText(text);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("items"));
        assertTrue(result.containsKey("totalCalories"));

        java.util.List<java.util.Map<String, Object>> items =
                (java.util.List<java.util.Map<String, Object>>) result.get("items");
        assertFalse(items.isEmpty());
        double total = Double.parseDouble(result.get("totalCalories").toString());
        assertTrue(total > 0, "Total calories should be greater than 0");

        boolean hasJianjiao = items.stream().anyMatch(item ->
                item.get("name").toString().contains("煎饺") &&
                        item.containsKey("calories") &&
                        Double.parseDouble(item.get("calories").toString()) > 0
        );
        boolean hasDuckLeg = items.stream().anyMatch(item ->
                item.get("name").toString().contains("鸭腿") &&
                        item.containsKey("calories") &&
                        Double.parseDouble(item.get("calories").toString()) > 0
        );
        boolean hasHuashengbing = items.stream().anyMatch(item ->
                item.get("name").toString().contains("花生饼") &&
                        item.containsKey("calories") &&
                        Double.parseDouble(item.get("calories").toString()) > 0
        );
        assertTrue(hasJianjiao && hasDuckLeg && hasHuashengbing,
                "Fallback dict should estimate calories for 煎饺/鸭腿/花生饼");
    }

    @Test
    void detectAnomaly_WithNullValue_ShouldReturnNonAnomaly() {
        // Arrange
        Long userId = 1L;
        String dataType = "血压";
        Double value = null;

        // Act
        AnomalyResult result = healthDataAiService.detectAnomaly(userId, dataType, value, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isAnomaly());
        assertEquals("数值为空", result.reason());
    }

    @Test
    void detectAnomaly_WithValidDataType_ShouldWork() {
        // Arrange
        Long userId = 1L;
        String dataType = "血压";
        Double value = 180.0; // High blood pressure

        when(healthLogRepository.findByUser_IdAndTypeOrderByLogDateDesc(any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        AnomalyResult result = healthDataAiService.detectAnomaly(userId, dataType, value, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isAnomaly()); // Should be anomaly because 180 is > 140
    }

    @Test
    void optimizeDietText_AiFailure_ShouldUseFallback() {
        // Arrange
        String text = "汉堡1个，可乐1杯";
        
        // Mock ChatClient builder to throw exception to simulate AI failure
        when(chatClientBuilder.build()).thenThrow(new RuntimeException("AI Service Unavailable"));

        // Act
        java.util.Map<String, Object> result = healthDataAiService.optimizeDietText(text);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("items"));
        assertTrue(result.containsKey("totalCalories"));
        
        java.util.List<java.util.Map<String, Object>> items = (java.util.List<java.util.Map<String, Object>>) result.get("items");
        assertFalse(items.isEmpty());
        
        // Check if "汉堡" is parsed with calories (fallback logic)
        boolean hasBurger = items.stream().anyMatch(item -> 
            item.get("name").toString().contains("汉堡") && 
            item.containsKey("calories") && 
            Double.parseDouble(item.get("calories").toString()) > 0
        );
        assertTrue(hasBurger, "Fallback logic should estimate calories for Burger");
    }
}
