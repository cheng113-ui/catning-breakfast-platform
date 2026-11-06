package com.zhengli.catning.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull(message = "商品 ID 不能为空")
        @Positive(message = "商品 ID 必须大于 0")
        Long productId,

        @Positive(message = "商品数量必须大于 0")
        @Max(value = 50, message = "单个商品一次最多预订 50 份")
        int quantity
) {
}
