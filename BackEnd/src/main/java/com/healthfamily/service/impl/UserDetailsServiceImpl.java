package com.healthfamily.service.impl;

import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
/**
 * 用户Details服务Impl实现类
 * <p>
 * 实现平台核心业务服务，负责业务编排、数据聚合及与 AI/规则引擎的协同。
 * </p>
 */
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    /**
     * 执行业务操作
     * @param username 业务参数
     * @return 业务返回结果
     * @throws UsernameNotFoundException 业务异常
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByPhone(username)

                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
    }
}

