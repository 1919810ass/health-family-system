package com.healthfamily.web.controller;

import com.healthfamily.annotation.Audit;
import com.healthfamily.domain.constant.SensitivityLevel;
import com.healthfamily.service.AuthService;
import com.healthfamily.service.DoctorService;
import com.healthfamily.web.dto.LoginRequest;
import com.healthfamily.web.dto.RefreshTokenRequest;
import com.healthfamily.web.dto.RegisterRequest;
import com.healthfamily.web.dto.DoctorRegisterRequest;
import com.healthfamily.web.dto.Result;
import com.healthfamily.web.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
/**
 * 认证控制器
 * <p>
 * 提供相关 REST API，负责请求参数校验、鉴权信息提取，并调用服务层完成业务处理。
 * </p>
 */
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final DoctorService doctorService;


    @PostMapping("/register")
    @Audit(action = "REGISTER", resource = "User")
    /**
     * 注册
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        TokenResponse response = authService.register(request);
        if (response == null) {
            return Result.success("注册成功，请等待管理员审核");
        }
        return Result.success(response);
    }

    @PostMapping("/register-doctor")
    @Audit(action = "REGISTER_DOCTOR", resource = "Doctor")
    /**
     * 注册
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<?> registerDoctor(@Valid @RequestBody DoctorRegisterRequest request) {
        // 医生注册需要审核，注册成功但不返回token，需要等待管理员审核通过后才能登录
        doctorService.registerDoctor(request);
        return Result.success("注册成功，请等待管理员审核");
    }

    @PostMapping("/register-doctor-old")
    @Deprecated
    /**
     * 注册
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<TokenResponse> registerDoctorOld(@Valid @RequestBody RegisterRequest request) {
        // 保留旧接口以兼容，但建议使用新的register-doctor接口
        return Result.success(authService.registerDoctor(request));
    }

    @PostMapping("/register-admin")
    @Audit(action = "REGISTER_ADMIN", resource = "Admin", sensitivity = SensitivityLevel.HIGH)
    /**
     * 注册
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<TokenResponse> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.registerAdmin(request));
    }

    @PostMapping("/login")
    // Login logs are handled by UserLoginLogService internally, but we can add Audit for tracking
    /**
     * 登录
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/refresh")
    /**
     * 执行业务操作
     * @param request 请求体数据
     * @return 业务返回结果
     */
    public Result<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(authService.refresh(request.token()));
    }
    

}

