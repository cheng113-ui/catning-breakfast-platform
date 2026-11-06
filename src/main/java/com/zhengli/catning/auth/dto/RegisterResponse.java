package com.zhengli.catning.auth.dto;

import com.zhengli.catning.user.Role;

public record RegisterResponse(
        Long id,
        String name,
        String phone,
        Role role
) {
}
