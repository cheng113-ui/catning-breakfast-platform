package com.zhengli.catning.order.dto;

import com.zhengli.catning.order.BreakfastOrder;
import com.zhengli.catning.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNo,
        String customerName,
        String courierName,
        OrderStatus status,
        BigDecimal totalAmount,
        String deliveryAddress,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponse> items
) {

    public static OrderResponse from(BreakfastOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getCustomer().getName(),
                order.getCourier() == null ? null : order.getCourier().getName(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                order.getRemark(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}
