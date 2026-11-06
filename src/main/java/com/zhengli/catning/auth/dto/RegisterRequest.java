package com.zhengli.catning.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "姓名不能为空")
        @Size(max = 50, message = "姓名不能超过 50 个字符")
        String name,

        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "请输入 11 位手机号")
        String phone,

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 64, message = "密码长度应为 8-64 个字符")
        String password
) {
}
