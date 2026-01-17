package com.healthfamily.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量处理请求DTO
 */
public record BatchHandleRequest(
        List<Long> alertIds,       // 要处理的异常ID列表
        String action,             // 处理方式：notify(发送提醒), call(电话联系), referral(转诊建议)
        String content,            // 处理内容/提醒内容
        Boolean followUpRequired,  // 是否需要后续跟踪
        LocalDateTime followUpTime, // 跟踪时间
        String handlingNote       // 处理备注
) {
}