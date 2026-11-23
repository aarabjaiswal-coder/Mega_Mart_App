package com.megamart.repository;

import com.megamart.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findBySupabaseUserId(String supabaseUserId);
}
