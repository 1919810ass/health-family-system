package com.healthfamily.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfamily.annotation.Audit;
import com.healthfamily.domain.constant.AuditResult;
import com.healthfamily.domain.entity.User;
import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.healthfamily.annotation.Audit)")
    public Object auditAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Audit audit = method.getAnnotation(Audit.class);

        long startTime = System.currentTimeMillis();
        Object result = null;
        AuditResult auditResult = AuditResult.SUCCESS;
        String errorMessage = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            auditResult = AuditResult.FAILURE;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            try {
                long duration = System.currentTimeMillis() - startTime;
                User user = getCurrentUser();
                String ip = getIpAddress();
                String userAgent = getUserAgent();

                Map<String, Object> extra = new HashMap<>();
                extra.put("duration_ms", duration);
                extra.put("method", method.getName());
                extra.put("args", truncateArgs(joinPoint.getArgs()));
                if (errorMessage != null) {
                    extra.put("error", errorMessage);
                }

                String extraJson = objectMapper.writeValueAsString(extra);

                if (user != null) {
                    auditLogService.recordLog(
                            user,
                            audit.action(),
                            audit.resource().isEmpty() ? method.getName() : audit.resource(),
                            audit.sensitivity(),
                            auditResult,
                            ip,
                            userAgent,
                            extraJson
                    );
                }
            } catch (Exception e) {
                log.error("Error recording audit log", e);
            }
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).user();
        }
        return null;
    }

    private String getIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0];
            }
            return request.getRemoteAddr();
        }
        return null;
    }

    private String getUserAgent() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest().getHeader("User-Agent");
        }
        return null;
    }

    private Object[] truncateArgs(Object[] args) {
        if (args == null) return null;
        Object[] truncated = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String && ((String) args[i]).length() > 100) {
                truncated[i] = ((String) args[i]).substring(0, 100) + "...";
            } else {
                truncated[i] = args[i];
            }
        }
        return truncated;
    }
}
