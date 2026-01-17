package com.healthfamily.service.impl;

import com.healthfamily.common.exception.BusinessException;
import com.healthfamily.domain.constant.AuditResult;
import com.healthfamily.domain.constant.SensitivityLevel;
import com.healthfamily.domain.constant.UserRole;
import com.healthfamily.domain.entity.User;
import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.domain.repository.SystemSettingRepository;
import com.healthfamily.domain.entity.SystemSetting;
import com.healthfamily.security.JwtUtil;
import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.AuditLogService;
import com.healthfamily.service.AuthService;
import com.healthfamily.service.UserLoginLogService;
import com.healthfamily.web.dto.LoginRequest;
import com.healthfamily.web.dto.RegisterRequest;
import com.healthfamily.web.dto.TokenResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    @Value("${app.securityDisabled:false}")
    private boolean securityDisabled;
    
    private final UserLoginLogService userLoginLogService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public TokenResponse register(RegisterRequest request) {
        userRepository.findByPhone(request.phone())
                .ifPresent(user -> {
                    throw new BusinessException(40001, "手机号已注册");
                });

        User user = User.builder()
                .phone(request.phone())
                .nickname(request.nickname())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.MEMBER)
                .status(2) // 2: PENDING/待审核 (原为1: 启用)
                .build();

        User saved = userRepository.save(user);
        // 不再生成Token，因为需要审核
        // 为了兼容前端，这里可以返回null或者一个特定的标识，或者抛出异常让Controller处理
        // 但接口定义返回TokenResponse，所以我们返回一个空的或者包含特定标记的TokenResponse
        // 更推荐的做法是修改接口返回类型，或者在这里抛出一个 "Registered pending audit" 的非错误异常
        // 简单起见，我们返回null，Controller层会处理
        return null; 
    }

    @Override
    @Transactional
    public TokenResponse registerAdmin(RegisterRequest request) {
        userRepository.findByPhone(request.phone())
                .ifPresent(user -> {
                    throw new BusinessException(40001, "手机号已注册");
                });

        User user = User.builder()
                .phone(request.phone())
                .nickname(request.nickname())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.ADMIN)
                .status(1)
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getId(), saved.getRole().name());
        return TokenResponse.bearer(token);
    }

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        if (securityDisabled) {
            // ... (securityDisabled logic remains same)
            User user = userRepository.findByPhone(request.phone()).orElseGet(() -> {
                User u = User.builder()
                        .phone(request.phone())
                        .nickname(request.phone())
                        .passwordHash(passwordEncoder.encode(request.password()))
                        .role(UserRole.MEMBER)
                        .status(1)
                        .build();
                return userRepository.save(u);
            });
            String token = jwtUtil.generateToken(user.getId(), user.getRole().name());
            recordLoginLog(user, request, true);
            return TokenResponse.bearer(token);
        }

        // 1. Check account lock
        User user = userRepository.findByPhone(request.phone()).orElse(null);
        
        // 2. Check account status
        if (user != null) {
            if (user.getStatus() != null) {
                if (user.getStatus() == 2) {
                    throw new BusinessException(40105, "账号正在审核中，请耐心等待管理员审核");
                }
                if (user.getStatus() == 3) {
                    String reason = user.getAuditReason() != null ? user.getAuditReason() : "未说明原因";
                    throw new BusinessException(40106, "账号审核未通过: " + reason);
                }
                if (user.getStatus() == 0) {
                    throw new BusinessException(40107, "账号已被禁用，请联系管理员");
                }
            }
        }

        if (user != null && user.getLockExpiresAt() != null) {
            if (user.getLockExpiresAt().isAfter(LocalDateTime.now())) {
                recordLoginLog(user, request, false);
                throw new BusinessException(40103, "账号已被锁定，请稍后再试");
            } else {
                // Unlock
                user.setLockExpiresAt(null);
                user.setFailedAttempts(0);
                userRepository.save(user);
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.phone(), request.password())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            // Re-fetch user to be sure
            user = userRepository.findById(principal.getUserId())
                    .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
            
            // Reset lock info on success
            user.setLastLoginAt(LocalDateTime.now());
            user.setFailedAttempts(0);
            user.setLockExpiresAt(null);
            userRepository.save(user);

            String token = jwtUtil.generateToken(user.getId(), user.getRole().name());
            
            // 记录登录日志
            recordLoginLog(user, request, true);
            
            return TokenResponse.bearer(token);
        } catch (BadCredentialsException e) {
            // Handle login failure
            if (user != null) {
                handleLoginFailure(user);
            }
            
            // 记录失败的登录日志
            recordLoginLog(user, request, false);
            
            throw new BusinessException(40101, "手机号或密码错误", e);
        }
    }

    private void handleLoginFailure(User user) {
        int maxRetries = 5;
        int lockMinutes = 30;
        
        // Fetch from SystemSetting
        try {
            String retriesStr = systemSettingRepository.findByKey("security.login.max_retries")
                    .map(SystemSetting::getValue).orElse("5");
            // Handle JSON string format if stored as JSON string "5" vs 5
            maxRetries = Integer.parseInt(retriesStr.replace("\"", ""));
            
            String lockStr = systemSettingRepository.findByKey("security.login.lock_minutes")
                    .map(SystemSetting::getValue).orElse("30");
            lockMinutes = Integer.parseInt(lockStr.replace("\"", ""));
        } catch (Exception e) {
            // Use defaults
        }

        int currentFails = user.getFailedAttempts() == null ? 0 : user.getFailedAttempts();
        currentFails++;
        user.setFailedAttempts(currentFails);
        
        if (currentFails >= maxRetries) {
            user.setLockExpiresAt(LocalDateTime.now().plusMinutes(lockMinutes));
        }
        
        userRepository.save(user);
    }

    @Override
    @Transactional
    public TokenResponse registerDoctor(RegisterRequest request) {
        userRepository.findByPhone(request.phone())
                .ifPresent(user -> { throw new BusinessException(40001, "手机号已注册"); });

        User user = User.builder()
                .phone(request.phone())
                .nickname(request.nickname())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.DOCTOR)
                .status(2) // 2: Pending Audit
                .build();

        User saved = userRepository.save(user);
        
        // Return null or specific response to indicate pending audit
        return null;
    }

    private void recordLoginLog(User user, LoginRequest request, boolean success) {
        try {
            HttpServletRequest httpRequest = null;
            var requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                httpRequest = servletRequestAttributes.getRequest();
            }
            
            String ipAddress = getIpAddress(httpRequest);
            String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : null;
            String status = success ? "SUCCESS" : "FAILED";
            String role = user != null ? user.getRole().name() : "UNKNOWN";
            String username = user != null ? user.getPhone() : request.phone();
            Long userId = user != null ? user.getId() : null;
            
            userLoginLogService.saveLoginLog(userId, username, role, ipAddress, userAgent, status, "APP");
            
            // Record to Audit Log as well for unified security view
            if (user != null) {
                auditLogService.recordLog(
                    user,
                    "LOGIN",
                    "Auth",
                    SensitivityLevel.MEDIUM,
                    success ? AuditResult.SUCCESS : AuditResult.FAILURE,
                    ipAddress,
                    userAgent,
                    null
                );
            }
        } catch (Exception e) {
            // 记录登录日志失败不影响登录流程
        }
    }
    
    private String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP地址的情况，取第一个非unknown的IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    @Override
    @Transactional
    public TokenResponse refresh(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(40102, "token无效或已过期");
        }
        var claims = jwtUtil.parseToken(token);
        String subject = claims.getSubject();
        if (!StringUtils.hasText(subject)) {
            throw new BusinessException(40103, "token缺少用户标识");
        }
        Long userId;
        try {
            userId = Long.parseLong(subject);
        } catch (NumberFormatException ex) {
            throw new BusinessException(40104, "token用户标识格式错误", ex);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(40401, "用户不存在"));
        String role = jwtUtil.extractRole(claims);
        if (!StringUtils.hasText(role)) {
            role = user.getRole().name();
        }
        String newToken = jwtUtil.generateToken(user.getId(), role);
        return TokenResponse.bearer(newToken);
    }
}