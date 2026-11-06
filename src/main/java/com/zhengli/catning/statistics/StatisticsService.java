package com.zhengli.catning.statistics;

import com.zhengli.catning.order.OrderRepository;
import com.zhengli.catning.order.OrderStatus;
import com.zhengli.catning.product.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class StatisticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public StatisticsService(
            OrderRepository orderRepository,
            ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public StatisticsResponse overview() {
        BigDecimal totalSales = orderRepository.sumTotalAmountByStatus(OrderStatus.COMPLETED);
        return new StatisticsResponse(
                totalSales == null ? BigDecimal.ZERO : totalSales,
                orderRepository.countByStatus(OrderStatus.COMPLETED),
                productRepository.countByActiveTrue(),
                orderRepository.findTopProducts(
                        OrderStatus.COMPLETED,
                        PageRequest.of(0, 5)
                )
        );
    }
}
