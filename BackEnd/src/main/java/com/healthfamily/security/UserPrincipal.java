package com.healthfamily.security;

import com.healthfamily.domain.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record UserPrincipal(User user) implements UserDetails {

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public String getRole() {
        return user != null && user.getRole() != null ? user.getRole().name() : null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = getRole();
        if (role == null) return List.of();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return user != null ? user.getPasswordHash() : "";
    }

    @Override
    public String getUsername() {
        if (user == null) return "";
        return user.getPhone() != null ? user.getPhone() : (user.getId() != null ? user.getId().toString() : "");
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user != null && user.getStatus() != null && user.getStatus() == 1;
    }
}

