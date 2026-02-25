package com.healthfamily.web.interceptor;

import com.healthfamily.common.exception.MaintenanceException;
import com.healthfamily.domain.constant.UserRole;
import com.healthfamily.security.UserPrincipal;
import com.healthfamily.service.OpsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class MaintenanceInterceptor implements HandlerInterceptor {

    private final OpsService opsService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (opsService.getMaintenanceMode()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = false;
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
                if ("ADMIN".equals(principal.getRole())) {
                    isAdmin = true;
                }
            }
            
            // If not admin, block request
            if (!isAdmin) {
                throw new MaintenanceException("系统正在维护中，请稍后再试");
            }
        }
        return true;
    }
}
