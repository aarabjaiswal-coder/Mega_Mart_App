package com.megamart.repository;

import com.megamart.model.Customer;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhone(String phone);

    @Modifying
    @Query(value = "INSERT INTO customers (phone, created_at) VALUES (:phone, CURRENT_TIMESTAMP) " +
            "ON CONFLICT (phone) DO NOTHING", nativeQuery = true)
    void insertIfAbsent(@Param("phone") String phone);
}
