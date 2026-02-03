package com.healthfamily.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.domain.entity.ConstitutionAssessment;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.ConstitutionAssessmentRepository;
import com.healthfamily.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import com.healthfamily.ai.OllamaLegacyClient;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TcmAssessmentAiServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ConstitutionAssessmentRepository assessmentRepository;

    @Mock
    private OllamaLegacyClient ollamaLegacyClient;
    
    private TcmAssessmentAiService aiService;
    private ObjectMapper objectMapper;
    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        aiService = new TcmAssessmentAiService(chatClientBuilder, userRepository, assessmentRepository, objectMapper, ollamaLegacyClient);
    }

    @Test
    void testStartAiAssessment() {
        // 准备测试数据
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(chatClient.prompt().user(anyString()).call().content())
                .thenReturn("您好！请告诉我您的基本情况，比如年龄和性别。");

        // 执行测试
        Map<String, Object> result = aiService.startAiAssessment(userId);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("sessionId"));
        assertTrue(result.containsKey("question"));
        assertEquals(1, result.get("step"));
        assertEquals(10, result.get("totalSteps"));
        verify(userRepository).findById(userId);
    }

    @Test
    void testProcessAnswer() {
        // 准备测试数据
        Long userId = 1L;
        String sessionId = "test_session_123";
        String userAnswer = "我经常感到疲倦，容易出汗。";
        
        User mockUser = new User();
        mockUser.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(chatClient.prompt().user(anyString()).call().content())
                .thenReturn("了解了您的情况，那么您平时的食欲如何？");

        // 执行测试
        Map<String, Object> result = aiService.processAnswer(userId, sessionId, userAnswer);

        // 验证结果
        assertNotNull(result);
        assertEquals(sessionId, result.get("sessionId"));
        assertTrue(result.containsKey("question"));
        assertFalse((Boolean) result.get("shouldEnd"));
        verify(userRepository).findById(userId);
    }

    @Test
    void testGenerateFinalAssessment() {
        // 准备测试数据
        Long userId = 1L;
        String sessionId = "test_session_123";
        String finalAnswers = "用户回答汇总：经常疲倦、容易出汗、食欲一般";
        
        String mockAiResponse = "{\n" +
                "  \"primaryType\": \"QI_DEFICIENCY\",\n" +
                "  \"scores\": {\"QI_DEFICIENCY\": 75.0, \"BALANCED\": 20.0, \"YANG_DEFICIENCY\": 30.0},\n" +
                "  \"confidence\": 0.8,\n" +
                "  \"summary\": \"根据您的回答，判断为气虚质，主要表现为气力不足，易疲劳。\",\n" +
                "  \"recommendations\": [\"适当进行太极、散步等轻度运动\", \"饮食以温补为主\"]\n" +
                "}";
        
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn(mockAiResponse);

        // 执行测试
        Map<String, Object> result = aiService.generateFinalAssessment(userId, sessionId, finalAnswers);

        // 验证结果
        assertNotNull(result);
        assertEquals("QI_DEFICIENCY", result.get("primaryType"));
        assertEquals(0.8, result.get("confidence"));
    }
}
