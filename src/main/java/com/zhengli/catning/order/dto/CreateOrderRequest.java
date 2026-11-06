package com.zhengli.catning.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "订单至少包含一个商品")
        @Size(max = 20, message = "单个订单最多包含 20 种商品")
        List<@Valid OrderItemRequest> items,

        @NotBlank(message = "配送地址不能为空")
        @Size(max = 255, message = "配送地址不能超过 255 个字符")
        String deliveryAddress,

        @Size(max = 255, message = "备注不能超过 255 个字符")
        String remark
) {
}
