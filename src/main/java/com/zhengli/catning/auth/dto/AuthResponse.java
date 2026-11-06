package com.zhengli.catning.auth.dto;

import com.zhengli.catning.user.Role;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        Long userId,
        String name,
        Role role
) {
}
