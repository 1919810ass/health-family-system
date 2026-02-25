package com.healthfamily.service;

/**
 * 安全服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import java.util.Map;

public interface SecurityService {
    String exportUserDataBase64(Long userId);
    void deleteUserData(Long userId);
    
    Map<String, Object> getPrivacySettings(Long userId);
    void updatePrivacySettings(Long userId, Map<String, Object> settings);
    
    /**
     * Check if AI analysis is allowed for the given scope
     * @param userId user id
     * @param scope specific scope (e.g. "diet", "vitals", "sport", "assessment") or null for global check
     * @return true if allowed
     */
    boolean isAiAnalysisAllowed(Long userId, String scope);
}
