package com.megamart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_category_active", columnList = "category, active"),
        @Index(name = "idx_products_name", columnList = "name")
})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String sku;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 80)
    private String weight;

    @Column(length = 80)
    private String heightPreference;

    @Column(nullable = false)
    private int stockQuantity;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 2048)
    private String imageUrl;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Product() { }

    public Product(String sku, String name, String category, BigDecimal price, String weight,
                   String heightPreference, int stockQuantity, String imageUrl) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.price = price;
        this.weight = weight;
        this.heightPreference = heightPreference;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String sku, String name, String category, BigDecimal price, String weight,
                       String heightPreference, int stockQuantity, String imageUrl) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.price = price;
        this.weight = weight;
        this.heightPreference = heightPreference;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.updatedAt = Instant.now();
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public String getWeight() { return weight; }
    public String getHeightPreference() { return heightPreference; }
    public int getStockQuantity() { return stockQuantity; }
    public boolean isInStock() { return stockQuantity > 0; }
    public boolean isActive() { return active; }
    public String getImageUrl() { return imageUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
