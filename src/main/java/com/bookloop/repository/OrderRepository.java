package com.bookloop.repository;

import com.bookloop.entity.Order;
import com.bookloop.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.seller WHERE o.buyer.id = :buyerId ORDER BY o.orderDate DESC")
    List<Order> findByBuyerIdOrderByOrderDateDesc(@Param("buyerId") Long buyerId);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i LEFT JOIN FETCH o.items allItems LEFT JOIN FETCH allItems.seller WHERE i.seller.id = :sellerId ORDER BY o.orderDate DESC")
    List<Order> findBySellerIdOrderByOrderDateDesc(@Param("sellerId") Long sellerId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product LEFT JOIN FETCH i.seller LEFT JOIN FETCH o.buyer WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByParentCheckoutGroup(String parentCheckoutGroup);

    long countByStatus(OrderStatus status);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o WHERE (o.seller.id = :sellerId OR EXISTS (SELECT i FROM o.items i WHERE i.seller.id = :sellerId)) AND o.status = :status")
    long countBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE (o.seller.id = :sellerId OR EXISTS (SELECT i FROM o.items i WHERE i.seller.id = :sellerId)) AND o.status IN (:statuses)")
    java.math.BigDecimal sumTotalAmountBySellerIdAndStatusIn(@Param("sellerId") Long sellerId, @Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product LEFT JOIN FETCH o.seller LEFT JOIN FETCH o.buyer WHERE (o.seller.id = :sellerId OR EXISTS (SELECT it FROM o.items it WHERE it.seller.id = :sellerId)) ORDER BY o.orderDate DESC")
    Page<Order> findBySellerIdWithDetails(@Param("sellerId") Long sellerId, Pageable pageable);

    /**
     * Admin: paginated list of all orders with optional filters.
     */
    @Query("""
            SELECT o FROM Order o LEFT JOIN FETCH o.buyer
            WHERE (:keyword IS NULL OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.buyer.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR o.status = :status)
            ORDER BY o.orderDate DESC
            """)
    Page<Order> searchAllForAdmin(
            @Param("keyword") String keyword,
            @Param("status") OrderStatus status,
            Pageable pageable);

    /**
     * Recent orders for dashboard.
     */
    List<Order> findTop5ByOrderByOrderDateDesc();
}
