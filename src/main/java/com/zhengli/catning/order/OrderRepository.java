package com.zhengli.catning.order;

import com.zhengli.catning.statistics.TopProductResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<BreakfastOrder, Long> {

    @EntityGraph(attributePaths = {"items", "courier"})
    Page<BreakfastOrder> findByCustomerPhone(String phone, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "courier", "customer"})
    Page<BreakfastOrder> findByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "courier", "customer"})
    Page<BreakfastOrder> findAll(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from BreakfastOrder o where o.id = :id")
    Optional<BreakfastOrder> findByIdForUpdate(@Param("id") Long id);

    long countByStatus(OrderStatus status);

    @Query("select coalesce(sum(o.totalAmount), 0) from BreakfastOrder o where o.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") OrderStatus status);

    @Query("""
            select new com.zhengli.catning.statistics.TopProductResponse(
                i.productName,
                sum(i.quantity),
                sum(i.unitPrice * i.quantity)
            )
            from OrderItem i
            where i.order.status = :status
            group by i.productName
            order by sum(i.quantity) desc
            """)
    List<TopProductResponse> findTopProducts(
            @Param("status") OrderStatus status,
            Pageable pageable
    );
}
