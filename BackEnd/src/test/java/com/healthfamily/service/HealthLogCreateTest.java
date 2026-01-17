package com.healthfamily.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.common.exception.BusinessException;
import com.healthfamily.domain.constant.HealthLogType;
import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.service.impl.HealthLogServiceImpl;
import com.healthfamily.web.dto.HealthLogRequest;
import com.healthfamily.web.dto.HealthLogResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthLogCreateTest {

    @Mock
    private HealthLogRepository healthLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HealthDataAiService healthDataAiService;

    @InjectMocks
    private HealthLogServiceImpl healthLogService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
    }

    @Test
    void createLog_NullRequest_ShouldThrowException() {
        assertThrows(BusinessException.class, () -> {
            healthLogService.createLog(1L, null);
        });
    }

    @Test
    void createLog_AnomalyDetectionFailure_ShouldNotBlockCreation() {
        // Arrange
        HealthLogRequest request = new HealthLogRequest(
                LocalDate.now(),
                HealthLogType.VITALS,
                Map.of("value", 120.0),
                null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // Mock cleanAndNormalize
        when(healthDataAiService.cleanAndNormalize(any(), any())).thenReturn(Map.of("value", 120.0));
        
        // Mock detectAnomaly throwing exception
        when(healthDataAiService.detectAnomaly(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("AI Service Down"));

        when(healthLogRepository.save(any())).thenAnswer(invocation -> {
            HealthLog log = invocation.getArgument(0);
            log.setId(100L);
            return log;
        });

        // Act
        HealthLogResponse response = healthLogService.createLog(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.id());
        verify(healthLogRepository).save(any());
        // Verify detectAnomaly was called but exception was caught
        verify(healthDataAiService).detectAnomaly(any(), any(), any(), any());
    }

    @Test
    void createLog_Success() {
        // Arrange
        HealthLogRequest request = new HealthLogRequest(
                LocalDate.now(),
                HealthLogType.VITALS,
                Map.of("value", 120.0),
                null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(healthDataAiService.cleanAndNormalize(any(), any())).thenReturn(Map.of("value", 120.0));
        when(healthDataAiService.detectAnomaly(any(), any(), any(), any()))
                .thenReturn(new HealthDataAiService.AnomalyResult(false, "", "LOW", ""));
        when(healthLogRepository.save(any())).thenAnswer(invocation -> {
            HealthLog log = invocation.getArgument(0);
            log.setId(100L);
            return log;
        });

        // Act
        HealthLogResponse response = healthLogService.createLog(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.id());
    }
}
