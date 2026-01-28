package com.healthfamily.service.impl;

import com.healthfamily.domain.entity.ConsultationSession;
import com.healthfamily.domain.entity.ConsultationTriageChat;
import com.healthfamily.domain.repository.ConsultationSessionRepository;
import com.healthfamily.domain.repository.ConsultationTriageChatRepository;
import com.healthfamily.service.AiTriageService;
import com.healthfamily.ai.OllamaLegacyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiTriageServiceImpl implements AiTriageService {

    private final ChatClient.Builder chatClientBuilder;
    private final ConsultationSessionRepository sessionRepo;
    private final ConsultationTriageChatRepository chatRepo;
    private final OllamaLegacyClient ollamaLegacyClient;

    // 1. 用户发送消息，获取AI回复 (多轮对话)
    @Override
    public String chat(Long sessionId, String userContent) {
        // 保存用户消息
        saveMessage(sessionId, "USER", userContent);

        // 加载历史记录 (构建上下文)
        List<ConsultationTriageChat> history = chatRepo.findBySessionIdOrderByGmtCreateAsc(sessionId);
        
        List<Message> messages = new ArrayList<>();
        // 添加 System Prompt (人设)
        Resource systemResource = new ClassPathResource("prompts/triage_chat_system.st");
        SystemMessage systemMsg = new SystemMessage(new PromptTemplate(systemResource).create().getContents());
        messages.add(systemMsg);

        // 添加历史对话
        for (ConsultationTriageChat chat : history) {
            if ("USER".equals(chat.getSenderRole())) {
                messages.add(new UserMessage(chat.getContent()));
            } else {
                messages.add(new AssistantMessage(chat.getContent()));
            }
        }

        // 调用 AI
        String aiReply;
        try {
            ChatClient chatClient = chatClientBuilder.build();
            aiReply = chatClient.prompt(new Prompt(messages)).call().content();
        } catch (WebClientResponseException.NotFound ex) {
            String legacyPrompt = messages.stream().map(Message::getContent).collect(Collectors.joining("\n"));
            aiReply = ollamaLegacyClient.generate(legacyPrompt, null, null);
        }

        // 保存 AI 回复
        saveMessage(sessionId, "AI", aiReply);

        return aiReply;
    }

    // 2. 生成最终摘要
    @Override
    public String generateSummary(Long sessionId) {
        List<ConsultationTriageChat> history = chatRepo.findBySessionIdOrderByGmtCreateAsc(sessionId);
        String historyText = history.stream()
                .map(h -> h.getSenderRole() + ": " + h.getContent())
                .collect(Collectors.joining("\n"));

        PromptTemplate promptTemplate = new PromptTemplate(
            new ClassPathResource("prompts/triage_summary.st")
        );
        String summary;
        try {
            ChatClient chatClient = chatClientBuilder.build();
            summary = chatClient.prompt(promptTemplate.create(java.util.Map.of("history", historyText)))
                    .call().content();
        } catch (WebClientResponseException.NotFound ex) {
            String legacyPrompt = promptTemplate.create(java.util.Map.of("history", historyText)).getContents();
            summary = ollamaLegacyClient.generate(legacyPrompt, null, null);
        }

        // 更新会话表
        ConsultationSession session = sessionRepo.findById(sessionId).orElseThrow();
        session.setTriageSummary(summary);
        session.setIsAiTriaged(true);
        sessionRepo.save(session);

        return summary;
    }

    private void saveMessage(Long sessionId, String role, String content) {
        ConsultationTriageChat chat = new ConsultationTriageChat();
        chat.setSessionId(sessionId);
        chat.setSenderRole(role);
        chat.setContent(content);
        chatRepo.save(chat);
    }
}
