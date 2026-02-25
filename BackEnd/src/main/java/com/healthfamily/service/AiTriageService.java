/**
 * AITriage服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
package com.healthfamily.service;

public interface AiTriageService {
    /**
     * 用户发送消息，获取AI回复 (多轮对话)
     * @param sessionId 会话ID
     * @param userContent 用户消息内容
     * @return AI回复
     */
    String chat(Long sessionId, String userContent);

    /**
     * 生成最终摘要
     * @param sessionId 会话ID
     * @return 生成的摘要
     */
    String generateSummary(Long sessionId);
}
