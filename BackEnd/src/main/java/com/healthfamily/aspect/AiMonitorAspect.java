package com.healthfamily.aspect;

import com.healthfamily.domain.entity.AiRequestLog;
import com.healthfamily.service.AiMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AiMonitorAspect {

    private final AiMonitorService aiMonitorService;

    @Pointcut("execution(* com.healthfamily.service.impl.*Ai*Impl.*(..)) || execution(* com.healthfamily.service.impl.HealthInferenceServiceImpl.*(..)) || execution(* com.healthfamily.service.AiAssistantService.*(..))")
    public void aiServicePointcut() {}

    @Around("aiServicePointcut()")
    public Object monitorAiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        
        // Use AtomicLong to store startTime in a thread-safe way if needed, 
        // but here 'start' is captured in closure.
        
        try {
            Object result = joinPoint.proceed();

            if (result instanceof Flux) {
                return ((Flux<?>) result)
                        .doFinally(signalType -> {
                            long duration = System.currentTimeMillis() - start;
                            String status = signalType.name().equals("ON_COMPLETE") ? "SUCCESS" : "FAIL";
                            String errorMessage = signalType.name().equals("ON_ERROR") ? "Stream Error" : null;
                            recordLog(joinPoint, duration, status, errorMessage, null, null); // Pass null result for Flux
                        });
            } else if (result instanceof Mono) {
                return ((Mono<?>) result)
                        .doFinally(signalType -> {
                            long duration = System.currentTimeMillis() - start;
                            String status = signalType.name().equals("ON_COMPLETE") ? "SUCCESS" : "FAIL";
                            String errorMessage = signalType.name().equals("ON_ERROR") ? "Mono Error" : null;
                            recordLog(joinPoint, duration, status, errorMessage, null, null);
                        });
            } else {
                // Synchronous call
                long duration = System.currentTimeMillis() - start;
                recordLog(joinPoint, duration, "SUCCESS", null, result, null);
                return result;
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            recordLog(joinPoint, duration, "FAIL", e.getMessage(), null, null);
            throw e;
        }
    }

    private void recordLog(ProceedingJoinPoint joinPoint, long duration, String status, String errorMessage, Object result, Long explicitUserId) {
        // 1. Get User ID
        Long userId = explicitUserId;
        if (userId == null) {
            userId = getUserId();
        }
        if (userId == null) {
            // Try to find in args
            for (Object arg : joinPoint.getArgs()) {
                if (arg instanceof Long) {
                    userId = (Long) arg;
                    break; // Assume first Long is userId if not in context
                }
            }
        }
        if (userId == null) userId = -1L;

        // 2. Service & Model Name
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String serviceName = className + "." + methodName;
        
        // Try to get model name from field or annotation? 
        // For now hardcode or use simple logic, as reflection is brittle without specific convention
        String modelName = "default-model"; 

        // 3. Token Usage (Attempt extraction)
        int inputTokens = 0;
        int outputTokens = 0;
        // If result is a specific type with token usage, extract it here.
        // Currently no standard interface for this in the project.
        
        AiRequestLog logEntity = AiRequestLog.builder()
                .traceId(UUID.randomUUID().toString())
                .userId(userId)
                .serviceName(serviceName)
                .modelName(modelName)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .latency(duration)
                .status(status)
                .errorMessage(errorMessage != null && errorMessage.length() > 2000 ? errorMessage.substring(0, 2000) : errorMessage)
                .createTime(LocalDateTime.now())
                .build();

        aiMonitorService.saveLog(logEntity);
    }

    private Long getUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() != null) {
                // Assuming UserPrincipal or similar has getId()
                // Or just try to cast if we know the type. 
                // Let's use reflection or dynamic check to be safe
                Object principal = auth.getPrincipal();
                if (principal.getClass().getSimpleName().equals("UserPrincipal")) {
                    // Try to get ID via reflection or known method
                    try {
                        return (Long) principal.getClass().getMethod("getId").invoke(principal);
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
