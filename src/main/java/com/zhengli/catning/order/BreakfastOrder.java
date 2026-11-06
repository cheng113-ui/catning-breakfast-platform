package com.zhengli.catning.order;

import com.zhengli.catning.product.Product;
import com.zhengli.catning.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "breakfast_orders")
public class BreakfastOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 40)
    private String orderNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private User courier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress;

    @Column(length = 255)
    private String remark;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    protected BreakfastOrder() {
    }

    public BreakfastOrder(String orderNo, User customer, String deliveryAddress, String remark) {
        this.orderNo = orderNo;
        this.customer = customer;
        this.deliveryAddress = deliveryAddress;
        this.remark = remark;
    }

    @PreUpdate
    void touchUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

    public void addItem(Product product, int quantity) {
        OrderItem item = new OrderItem(
                this,
                product.getId(),
                product.getName(),
                product.getPrice(),
                quantity
        );
        items.add(item);
        totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
    }

    public void confirm() {
        requireStatus(OrderStatus.CREATED, "只有待确认订单可以确认");
        status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        requireStatus(OrderStatus.CREATED, "只有待确认订单可以取消");
        status = OrderStatus.CANCELLED;
    }

    public void acceptBy(User courierUser) {
        requireStatus(OrderStatus.CONFIRMED, "只有已确认订单可以被接单");
        this.courier = courierUser;
        status = OrderStatus.DELIVERING;
    }

    public void completeBy(User courierUser) {
        if (status != OrderStatus.DELIVERING) {
            throw new IllegalStateException("只有配送中的订单可以完成");
        }
        if (courier == null || !courier.getId().equals(courierUser.getId())) {
            throw new IllegalStateException("只能完成自己接取的订单");
        }
        status = OrderStatus.COMPLETED;
    }

    public void requireOwner(User customerUser) {
        if (!customer.getId().equals(customerUser.getId())) {
            throw new IllegalStateException("无权操作该订单");
        }
    }

    private void requireStatus(OrderStatus expected, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public User getCustomer() {
        return customer;
    }

    public User getCourier() {
        return courier;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getRemark() {
        return remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
