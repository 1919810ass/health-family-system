package com.healthfamily.service;

import com.healthfamily.web.dto.LoginRequest;
import com.healthfamily.web.dto.RegisterRequest;
/**
 * 认证服务接口
 * <p>
 * 定义业务服务能力边界，供控制器层调用并由实现类落地。
 * </p>
 */
import com.healthfamily.web.dto.TokenResponse;


public interface AuthService {

    TokenResponse register(RegisterRequest request);

    TokenResponse registerDoctor(RegisterRequest request);

    TokenResponse registerAdmin(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(String token);
    

}

