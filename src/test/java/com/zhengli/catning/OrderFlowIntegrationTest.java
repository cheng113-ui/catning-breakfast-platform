package com.zhengli.catning;

import com.zhengli.catning.order.OrderService;
import com.zhengli.catning.order.OrderStatus;
import com.zhengli.catning.order.dto.CreateOrderRequest;
import com.zhengli.catning.order.dto.OrderItemRequest;
import com.zhengli.catning.order.dto.OrderResponse;
import com.zhengli.catning.product.Product;
import com.zhengli.catning.product.ProductRepository;
import com.zhengli.catning.user.Role;
import com.zhengli.catning.user.User;
import com.zhengli.catning.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderFlowIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User customer;
    private User courier;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = userRepository.save(new User(
                "订单测试用户",
                "13700000002",
                passwordEncoder.encode("Password123!"),
                Role.CUSTOMER
        ));
        courier = userRepository.save(new User(
                "配送测试用户",
                "13900000002",
                passwordEncoder.encode("Courier123!"),
                Role.COURIER
        ));
        product = productRepository.save(new Product(
                "测试豆浆",
                "集成测试商品",
                new BigDecimal("2.50"),
                10
        ));
    }

    @Test
    void orderCanMoveFromCreatedToCompleted() {
        OrderResponse created = orderService.create(
                customer.getPhone(),
                request(2)
        );

        assertThat(created.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(created.totalAmount()).isEqualByComparingTo("5.00");
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock())
                .isEqualTo(8);

        OrderResponse confirmed = orderService.confirm(created.id());
        assertThat(confirmed.status()).isEqualTo(OrderStatus.CONFIRMED);

        OrderResponse accepted = orderService.accept(courier.getPhone(), created.id());
        assertThat(accepted.status()).isEqualTo(OrderStatus.DELIVERING);
        assertThat(accepted.courierName()).isEqualTo("配送测试用户");

        OrderResponse completed = orderService.complete(courier.getPhone(), created.id());
        assertThat(completed.status()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void cancellingCreatedOrderRestoresStock() {
        OrderResponse created = orderService.create(
                customer.getPhone(),
                request(3)
        );
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock())
                .isEqualTo(7);

        OrderResponse cancelled = orderService.cancel(customer.getPhone(), created.id());

        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock())
                .isEqualTo(10);
    }

    private CreateOrderRequest request(int quantity) {
        return new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), quantity)),
                "吉林师范大学 1 号宿舍楼",
                "少糖"
        );
    }
}
