package com.healthfamily.aspect;

import com.healthfamily.domain.entity.AiRequestLog;
import com.healthfamily.service.AiMonitorService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AiMonitorAspect {

    private final AiMonitorService aiMonitorService;

    @PostConstruct
    public void init() {
        log.info("============== AI MONITOR ASPECT LOADED SUCCESSFULLY ==============");
    }

    @Pointcut("execution(* com.healthfamily.service.impl.*Ai*Impl.*(..)) " +
            "|| execution(* com.healthfamily.service.impl.HealthInferenceServiceImpl.*(..)) " +
            "|| execution(* com.healthfamily.service.AiAssistantService.*(..)) " +
            "|| execution(* com.healthfamily.ai..*.*(..))")
    public void aiServicePointcut() {}

    @Around("aiServicePointcut()")
    public Object monitorAiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.info("🔔 AI Monitor Aspect Triggered: {}.{}", className, methodName);

        long start = System.currentTimeMillis();
        // Capture userId in main thread
        Long userId = getUserId();
        final AtomicLong inputTokens = new AtomicLong(0);
        final AtomicLong outputTokens = new AtomicLong(0);
        
        try {
            Object result = joinPoint.proceed();

            if (result instanceof Flux) {
                return ((Flux<?>) result)
                        .doOnNext(item -> {
                            // Accumulate tokens from each chunk in the stream
                            extractTokensFromResult(item, inputTokens, outputTokens);
                        })
                        .doFinally(signalType -> {
                            long duration = System.currentTimeMillis() - start;
                            boolean isSuccess = signalType == reactor.core.publisher.SignalType.ON_COMPLETE;
                            String errorMessage = isSuccess ? null : "Stream Error: " + signalType.name();
                            log.info("🔔 AI Monitor Flux Finished: status={}", isSuccess ? "SUCCESS" : "FAIL");
                            recordLog(joinPoint, duration, isSuccess ? "SUCCESS" : "FAIL", errorMessage, null, userId, inputTokens, outputTokens);
                        });
            } else if (result instanceof Mono) {
                return ((Mono<?>) result)
                        .doOnSuccess(res -> {
                            // res is the actual object, we can extract tokens here
                            extractTokensFromResult(res, inputTokens, outputTokens);
                        })
                        .doFinally(signalType -> {
                            long duration = System.currentTimeMillis() - start;
                            boolean isSuccess = signalType == reactor.core.publisher.SignalType.ON_COMPLETE || signalType == reactor.core.publisher.SignalType.ON_NEXT;
                            String errorMessage = isSuccess ? null : "Mono Error: " + signalType.name();
                            log.info("🔔 AI Monitor Mono Finished: status={}", isSuccess ? "SUCCESS" : "FAIL");
                            recordLog(joinPoint, duration, isSuccess ? "SUCCESS" : "FAIL", errorMessage, null, userId, inputTokens, outputTokens);
                        });
            } else {
                // Synchronous call
                long duration = System.currentTimeMillis() - start;
                extractTokensFromResult(result, inputTokens, outputTokens);
                log.info("🔔 AI Monitor Sync Finished: duration={}", duration);
                recordLog(joinPoint, duration, "SUCCESS", null, result, userId, inputTokens, outputTokens);
                return result;
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.error("🔔 AI Monitor Error: {}", e.getMessage());
            recordLog(joinPoint, duration, "FAIL", e.getMessage(), null, userId, new AtomicLong(0), new AtomicLong(0));
            throw e;
        }
    }

    private void recordLog(ProceedingJoinPoint joinPoint, long duration, String status, String errorMessage, Object result, Long explicitUserId, AtomicLong inputTokens, AtomicLong outputTokens) {
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
        
        // Dynamically get model name
        String modelName = getModelName(joinPoint); 

        AiRequestLog logEntity = AiRequestLog.builder()
                .traceId(UUID.randomUUID().toString())
                .userId(userId)
                .serviceName(serviceName)
                .modelName(modelName)
                .inputTokens(inputTokens.get())
                .outputTokens(outputTokens.get())
                .latency(duration)
                .status(status)
                .errorMessage(errorMessage != null && errorMessage.length() > 2000 ? errorMessage.substring(0, 2000) : errorMessage)
                .createTime(LocalDateTime.now())
                .build();

        aiMonitorService.saveLog(logEntity);
    }

    private void extractTokensFromResult(Object result, AtomicLong inputTokens, AtomicLong outputTokens) {
        if (result == null) return;
        try {
            java.lang.reflect.Method getTokenUsage = result.getClass().getMethod("getTokenUsage");
            Object tokenUsage = getTokenUsage.invoke(result);
            if (tokenUsage instanceof Map) {
                Map<String, Number> usageMap = (Map<String, Number>) tokenUsage;
                inputTokens.set(usageMap.getOrDefault("inputTokens", 0).longValue());
                outputTokens.set(usageMap.getOrDefault("outputTokens", 0).longValue());
            }
        } catch (Exception e) {
            // Method not found or other reflection error, ignore
        }
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

    private String getModelName(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < parameterAnnotations.length; i++) {
                for (Annotation annotation : parameterAnnotations[i]) {
                    if (annotation instanceof ModelName) {
                        if (args[i] instanceof String) {
                            return (String) args[i];
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not determine model name from annotation", e);
        }
        return "default-model"; // Fallback
    }
}
