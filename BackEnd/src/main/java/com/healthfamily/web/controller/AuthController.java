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
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final DoctorService doctorService;


    @PostMapping("/register")
    @Audit(action = "REGISTER", resource = "User")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        TokenResponse response = authService.register(request);
        if (response == null) {
            return Result.success("注册成功，请等待管理员审核");
        }
        return Result.success(response);
    }

    @PostMapping("/register-doctor")
    @Audit(action = "REGISTER_DOCTOR", resource = "Doctor")
    public Result<?> registerDoctor(@Valid @RequestBody DoctorRegisterRequest request) {
        // 医生注册需要审核，注册成功但不返回token，需要等待管理员审核通过后才能登录
        doctorService.registerDoctor(request);
        return Result.success("注册成功，请等待管理员审核");
    }

    @PostMapping("/register-doctor-old")
    @Deprecated
    public Result<TokenResponse> registerDoctorOld(@Valid @RequestBody RegisterRequest request) {
        // 保留旧接口以兼容，但建议使用新的register-doctor接口
        return Result.success(authService.registerDoctor(request));
    }

    @PostMapping("/register-admin")
    @Audit(action = "REGISTER_ADMIN", resource = "Admin", sensitivity = SensitivityLevel.HIGH)
    public Result<TokenResponse> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.registerAdmin(request));
    }

    @PostMapping("/login")
    // Login logs are handled by UserLoginLogService internally, but we can add Audit for tracking
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(authService.refresh(request.token()));
    }
    

}

