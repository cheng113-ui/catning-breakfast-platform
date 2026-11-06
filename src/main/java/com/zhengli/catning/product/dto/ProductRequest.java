package com.zhengli.catning.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "商品名称不能为空")
        @Size(max = 100, message = "商品名称不能超过 100 个字符")
        String name,

        @Size(max = 500, message = "商品描述不能超过 500 个字符")
        String description,

        @NotNull(message = "商品价格不能为空")
        @DecimalMin(value = "0.01", message = "商品价格必须大于 0")
        @Digits(integer = 8, fraction = 2, message = "商品价格最多保留 2 位小数")
        BigDecimal price,

        @PositiveOrZero(message = "库存不能为负数")
        int stock
) {
}
