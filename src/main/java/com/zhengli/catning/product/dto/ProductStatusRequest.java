package com.zhengli.catning.product.dto;

import jakarta.validation.constraints.NotNull;

public record ProductStatusRequest(
        @NotNull(message = "商品状态不能为空")
        Boolean active
) {
}
