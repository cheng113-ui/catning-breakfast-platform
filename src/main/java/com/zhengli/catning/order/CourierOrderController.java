package com.zhengli.catning.order;

import com.zhengli.catning.order.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "配送")
@RestController
@RequestMapping("/api/courier/orders")
@PreAuthorize("hasRole('COURIER')")
public class CourierOrderController {

    private final OrderService orderService;

    public CourierOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "查看可接订单")
    @GetMapping("/available")
    public Page<OrderResponse> available(
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return orderService.listAvailable(pageable);
    }

    @Operation(summary = "接取订单")
    @PostMapping("/{id}/accept")
    public OrderResponse accept(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        return orderService.accept(userDetails.getUsername(), id);
    }

    @Operation(summary = "完成配送")
    @PostMapping("/{id}/complete")
    public OrderResponse complete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        return orderService.complete(userDetails.getUsername(), id);
    }
}
