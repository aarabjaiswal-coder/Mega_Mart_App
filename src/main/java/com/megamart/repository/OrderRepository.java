package com.megamart.repository;

import com.megamart.model.Order;
import com.megamart.model.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Order> findWithItemsById(Long id);

    @Query("select distinct o from CustomerOrder o left join fetch o.customer left join fetch o.items where o.id = :id")
    Optional<Order> findDetailsById(@Param("id") Long id);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByStatus(OrderStatus status);

    @EntityGraph(attributePaths = "customer")
    List<Order> findByCustomer_PhoneOrderByCreatedAtDesc(String phone, Pageable pageable);
}
