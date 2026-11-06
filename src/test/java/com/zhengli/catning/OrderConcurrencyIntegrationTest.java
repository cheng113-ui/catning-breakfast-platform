package com.zhengli.catning;

import com.zhengli.catning.common.BusinessException;
import com.zhengli.catning.order.OrderService;
import com.zhengli.catning.order.dto.CreateOrderRequest;
import com.zhengli.catning.order.dto.OrderItemRequest;
import com.zhengli.catning.product.Product;
import com.zhengli.catning.product.ProductRepository;
import com.zhengli.catning.user.Role;
import com.zhengli.catning.user.User;
import com.zhengli.catning.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OrderConcurrencyIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void twoConcurrentOrdersCannotOversellLastItem() throws Exception {
        User firstCustomer = createCustomer("13700000003");
        User secondCustomer = createCustomer("13700000004");
        Product product = productRepository.save(new Product(
                "最后一份小笼包",
                "并发测试商品",
                new BigDecimal("7.50"),
                1
        ));

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            List<Future<Boolean>> results = new ArrayList<>();
            results.add(executor.submit(
                    () -> placeOrder(firstCustomer, product, ready, start)
            ));
            results.add(executor.submit(
                    () -> placeOrder(secondCustomer, product, ready, start)
            ));

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            long successCount = results.stream()
                    .map(this::getResult)
                    .filter(Boolean::booleanValue)
                    .count();

            assertThat(successCount).isEqualTo(1);
            assertThat(productRepository.findById(product.getId()).orElseThrow().getStock())
                    .isZero();
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean placeOrder(
            User customer,
            Product product,
            CountDownLatch ready,
            CountDownLatch start
    ) throws InterruptedException {
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(product.getId(), 1)),
                "吉林师范大学 2 号宿舍楼",
                "并发测试"
        );
        ready.countDown();
        start.await(5, TimeUnit.SECONDS);
        try {
            orderService.create(customer.getPhone(), request);
            return true;
        } catch (BusinessException exception) {
            return false;
        }
    }

    private User createCustomer(String phone) {
        return userRepository.save(new User(
                "并发测试用户",
                phone,
                passwordEncoder.encode("Password123!"),
                Role.CUSTOMER
        ));
    }

    private boolean getResult(Future<Boolean> future) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new IllegalStateException("并发下单任务执行失败", exception);
        }
    }
}
