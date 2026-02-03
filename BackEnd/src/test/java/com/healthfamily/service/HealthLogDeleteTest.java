package com.healthfamily.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.common.exception.BusinessException;
import com.healthfamily.domain.constant.UserRole;
import com.healthfamily.domain.entity.HealthLog;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.HealthLogRepository;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.service.HealthDataAiService;
import com.healthfamily.service.impl.HealthLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthLogDeleteTest {

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
    private HealthLog healthLog;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .phone("13800000000")
                .nickname("testuser")
                .role(UserRole.MEMBER)
                .status(1)
                .build();
        healthLog = HealthLog.builder().id(100L).user(user).build();
    }

    @Test
    void deleteLog_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(healthLogRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(healthLog));

        // Act
        healthLogService.deleteLog(1L, 100L);

        // Assert
        verify(healthLogRepository, times(1)).delete(healthLog);
    }

    @Test
    void deleteLog_LogNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(healthLogRepository.findByIdAndUser(100L, user)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            healthLogService.deleteLog(1L, 100L);
        });
        
        assertEquals(40411, exception.getCode());
        assertEquals("日志不存在", exception.getMessage());
        verify(healthLogRepository, never()).delete(any());
    }

    @Test
    void deleteLog_UserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        // Mock default user behavior if implemented in service, or expect exception if configured strictly
        // Looking at HealthLogServiceImpl: resolveDefaultUser() calls userRepository.findAll().stream().findFirst()
        // Let's assume findAll returns empty for this test case to trigger exception
        when(userRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            healthLogService.deleteLog(99L, 100L);
        });
    }
}
