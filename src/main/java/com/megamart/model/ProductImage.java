package com.megamart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "product_images")
public class ProductImage {
    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "content_type", nullable = false, length = 64)
    private String contentType;

    @Column(name = "image_data", nullable = false, columnDefinition = "bytea")
    private byte[] imageData;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductImage() { }

    public ProductImage(Long productId, String contentType, byte[] imageData) {
        this.productId = productId;
        this.contentType = contentType;
        this.imageData = imageData;
        this.updatedAt = Instant.now();
    }

    public Long getProductId() { return productId; }
    public String getContentType() { return contentType; }
    public byte[] getImageData() { return imageData; }
}
