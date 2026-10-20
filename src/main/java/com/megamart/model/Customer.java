package com.megamart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String phone;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Customer() { }

    public Customer(String phone) {
        this.phone = phone;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getPhone() { return phone; }
    public Instant getCreatedAt() { return createdAt; }
}
