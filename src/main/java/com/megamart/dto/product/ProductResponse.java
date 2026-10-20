package com.megamart.dto.product;

import com.megamart.model.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String category,
        BigDecimal price,
        String weight,
        String heightPreference,
        int stockQuantity,
        boolean inStock,
        String imageUrl
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getSku(), product.getName(), product.getCategory(),
                product.getPrice(), product.getWeight(), product.getHeightPreference(), product.getStockQuantity(),
                product.isInStock(), product.getImageUrl());
    }
}
