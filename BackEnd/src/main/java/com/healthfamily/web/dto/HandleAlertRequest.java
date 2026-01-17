package com.healthfamily.web.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 处理异常请求DTO
 */
public record HandleAlertRequest(
        String action,           // 处理方式：notify(发送提醒), call(电话联系), referral(转诊建议)
        String content,          // 处理内容/提醒内容
        Boolean followUpRequired, // 是否需要后续跟踪
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime followUpTime, // 跟踪时间
        String handlingNote      // 处理备注
) {
}