package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.SeasonalWellnessService;
import com.healthfamily.web.dto.SeasonalWellnessDTO;
import com.healthfamily.web.dto.Result; // Assuming there is a Result wrapper
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/wellness")
@RequiredArgsConstructor
public class SeasonalWellnessController {

    private final SeasonalWellnessService wellnessService;

    @GetMapping("/today")
    public Result<SeasonalWellnessDTO> getTodayWellness(@AuthenticationPrincipal UserPrincipal principal,
                                                       @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) Long userHeader) {
        Long userId = (principal != null) ? principal.getUserId() : userHeader;
        // 如果未登录且没有Header，userId为null，服务层会返回通用建议
        return Result.success(wellnessService.getWellnessAdvice(userId));
    }

    @GetMapping(value = "/today/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getTodayWellnessStream(@AuthenticationPrincipal UserPrincipal principal,
                                                               @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) Long userHeader) {
        Long userId = (principal != null) ? principal.getUserId() : userHeader;
        return wellnessService.getWellnessAdviceStream(userId);
    }
}
