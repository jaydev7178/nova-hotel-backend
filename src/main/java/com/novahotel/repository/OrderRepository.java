package com.novahotel.repository;

import com.novahotel.entity.Order;
import com.novahotel.entity.User;
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
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ")
    Page<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status ")
    Page<Order> findByStatusOrderByCreatedAtDesc(@Param("status") Order.OrderStatus status, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status ")
    Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(
            @Param("userId") Long userId, 
            @Param("status") Order.OrderStatus status, 
            Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ")
    Page<Order> findByStatusInOrderByCreatedAtDesc(@Param("statuses") List<Order.OrderStatus> statuses, Pageable pageable);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") Order.OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.status = :status ")
    List<Order> findByUserAndStatusOrderByCreatedAtDesc(@Param("user") User user, @Param("status") Order.OrderStatus status);

    @Query("SELECT o FROM Order o ")
    Page<Order> findAllOrders(Pageable pageable);

}

