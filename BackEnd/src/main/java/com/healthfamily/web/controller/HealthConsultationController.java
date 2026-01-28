package com.healthfamily.web.controller;

import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.HealthConsultationService;
import com.healthfamily.web.dto.ConsultationRequest;
import com.healthfamily.web.dto.ConsultationResponse;
import com.healthfamily.web.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/consultation")
public class HealthConsultationController {

    private final HealthConsultationService consultationService;

    @PostMapping
    public Result<ConsultationResponse> consult(@AuthenticationPrincipal UserPrincipal principal,
                                                 @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                 @Valid @RequestBody ConsultationRequest request) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        return Result.success(consultationService.consult(userId, request));
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> consultStream(@AuthenticationPrincipal UserPrincipal principal,
                                                               @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                               @Valid @RequestBody ConsultationRequest request) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        Flux<String> stream = consultationService.consultStream(userId, request);
        StreamingResponseBody body = outputStream -> {
            stream.doOnNext(chunk -> {
                        if (chunk == null) {
                            return;
                        }
                        String payload = "data: " + chunk + "\n\n";
                        try {
                            outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
                            outputStream.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .doOnError(error -> {
                        String payload = "event: error\ndata: " + error.getMessage() + "\n\n";
                        try {
                            outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
                            outputStream.flush();
                        } catch (IOException ignored) {
                        }
                    })
                    .blockLast();
        };
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .header("Cache-Control", "no-cache")
                .header("X-Accel-Buffering", "no")
                .body(body);
    }

    @GetMapping("/history")
    public Result<List<ConsultationResponse>> getHistory(@AuthenticationPrincipal UserPrincipal principal,
                                                          @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                                          @RequestParam(value = "sessionId", required = false) String sessionId) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        return Result.success(consultationService.getHistory(userId, sessionId));
    }

    @PostMapping("/{id}/feedback")
    public Result<Void> feedback(@AuthenticationPrincipal UserPrincipal principal,
                                 @RequestHeader(value = "X-User-Id", required = false) Long userHeader,
                                 @PathVariable("id") Long consultationId,
                                 @RequestBody Map<String, Integer> request) {
        Long userId = principal != null ? principal.getUserId() : userHeader;
        Integer feedback = request.get("feedback");
        consultationService.feedback(consultationId, userId, feedback);
        return Result.success();
    }
}

