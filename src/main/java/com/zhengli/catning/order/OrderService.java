package com.zhengli.catning.order;

import com.zhengli.catning.common.BusinessException;
import com.zhengli.catning.order.dto.CreateOrderRequest;
import com.zhengli.catning.order.dto.OrderItemRequest;
import com.zhengli.catning.order.dto.OrderResponse;
import com.zhengli.catning.product.Product;
import com.zhengli.catning.product.ProductRepository;
import com.zhengli.catning.user.User;
import com.zhengli.catning.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class OrderService {

    private static final DateTimeFormatter ORDER_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse create(String username, CreateOrderRequest request) {
        User customer = getUser(username);
        Map<Long, Integer> requestedQuantities = aggregateItems(request);
        BreakfastOrder order = new BreakfastOrder(
                generateOrderNo(),
                customer,
                request.deliveryAddress().trim(),
                normalizeRemark(request.remark())
        );

        for (Map.Entry<Long, Integer> entry : requestedQuantities.entrySet()) {
            Product product = productRepository.findByIdForUpdate(entry.getKey())
                    .orElseThrow(() ->
                            new BusinessException(HttpStatus.NOT_FOUND, "商品不存在：" + entry.getKey())
                    );
            try {
                product.reserve(entry.getValue());
            } catch (IllegalStateException exception) {
                throw new BusinessException(HttpStatus.CONFLICT, exception.getMessage());
            }
            order.addItem(product, entry.getValue());
        }

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listMine(String username, Pageable pageable) {
        User customer = getUser(username);
        return orderRepository.findByCustomerPhone(customer.getPhone(), pageable)
                .map(OrderResponse::from);
    }

    @Transactional
    public OrderResponse cancel(String username, Long orderId) {
        User customer = getUser(username);
        BreakfastOrder order = getOrderForUpdate(orderId);
        try {
            order.requireOwner(customer);
            order.cancel();
        } catch (IllegalStateException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, exception.getMessage());
        }

        order.getItems().stream()
                .sorted((left, right) -> left.getProductId().compareTo(right.getProductId()))
                .forEach(item -> {
                    Product product = productRepository.findByIdForUpdate(item.getProductId())
                            .orElseThrow(() ->
                                    new BusinessException(
                                            HttpStatus.CONFLICT,
                                            "订单商品已不存在，无法恢复库存"
                                    )
                            );
                    product.release(item.getQuantity());
                });
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderResponse::from);
    }

    @Transactional
    public OrderResponse confirm(Long orderId) {
        BreakfastOrder order = getOrderForUpdate(orderId);
        try {
            order.confirm();
        } catch (IllegalStateException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, exception.getMessage());
        }
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listAvailable(Pageable pageable) {
        return orderRepository.findByStatus(OrderStatus.CONFIRMED, pageable)
                .map(OrderResponse::from);
    }

    @Transactional
    public OrderResponse accept(String username, Long orderId) {
        User courier = getUser(username);
        BreakfastOrder order = getOrderForUpdate(orderId);
        try {
            order.acceptBy(courier);
        } catch (IllegalStateException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, exception.getMessage());
        }
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse complete(String username, Long orderId) {
        User courier = getUser(username);
        BreakfastOrder order = getOrderForUpdate(orderId);
        try {
            order.completeBy(courier);
        } catch (IllegalStateException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, exception.getMessage());
        }
        return OrderResponse.from(order);
    }

    private Map<Long, Integer> aggregateItems(CreateOrderRequest request) {
        Map<Long, Integer> quantities = new TreeMap<>();
        for (OrderItemRequest item : request.items()) {
            try {
                quantities.merge(item.productId(), item.quantity(), Math::addExact);
            } catch (ArithmeticException exception) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "商品数量超出允许范围");
            }
            if (quantities.get(item.productId()) > 50) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "单个商品一次最多预订 50 份"
                );
            }
        }
        return quantities;
    }

    private BreakfastOrder getOrderForUpdate(Long orderId) {
        return orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new BusinessException(HttpStatus.NOT_FOUND, "订单不存在")
                );
    }

    private User getUser(String username) {
        return userRepository.findByPhone(username)
                .orElseThrow(() ->
                        new BusinessException(HttpStatus.UNAUTHORIZED, "登录用户不存在")
                );
    }

    private String generateOrderNo() {
        return "CN" + LocalDateTime.now().format(ORDER_TIME_FORMAT)
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String normalizeRemark(String remark) {
        return remark == null || remark.isBlank() ? null : remark.trim();
    }
}
