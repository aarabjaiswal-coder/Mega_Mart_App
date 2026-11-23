package com.megamart.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "CustomerOrder")
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_customer_created", columnList = "customer_id, created_at"),
        @Index(name = "idx_orders_created", columnList = "created_at")
})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 1000)
    private String deliveryAddress;

    @Column(name = "recipient_name", nullable = false, length = 120)
    private String recipientName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentMode paymentMode = PaymentMode.COD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(name = "out_for_delivery_at")
    private Instant outForDeliveryAt;

    protected Order() { }

    public Order(Customer customer, String recipientName, String deliveryAddress) {
        this.customer = customer;
        this.recipientName = recipientName;
        this.deliveryAddress = deliveryAddress;
        this.totalAmount = BigDecimal.ZERO;
        this.status = OrderStatus.PLACED;
        this.paymentMode = PaymentMode.COD;
        this.paymentStatus = PaymentStatus.UNPAID;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
        if (status == OrderStatus.OUT_FOR_DELIVERY) {
            this.outForDeliveryAt = this.updatedAt;
        }
    }

    public void markCashCollected() {
        this.paymentStatus = PaymentStatus.PAID;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getRecipientName() { return recipientName; }
    public OrderStatus getStatus() { return status; }
    public PaymentMode getPaymentMode() { return paymentMode; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public List<OrderItem> getItems() { return items; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getOutForDeliveryAt() { return outForDeliveryAt; }
}
