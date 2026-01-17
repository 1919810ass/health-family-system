package com.healthfamily.service.impl;

import com.healthfamily.domain.repository.UserRepository;
import com.healthfamily.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByPhone(username)

                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
    }
}

