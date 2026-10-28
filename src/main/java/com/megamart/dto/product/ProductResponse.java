package com.megamart.dto.product;

import com.megamart.model.Product;

import java.math.BigDecimal;
import java.util.Locale;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String category,
        BigDecimal price,
        BigDecimal offerPrice,
        String weight,
        String heightPreference,
        int stockQuantity,
        boolean inStock,
        String imageUrl
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getSku(), product.getName(), product.getCategory(),
                product.getPrice(), product.getOfferPrice(), product.getWeight(), product.getHeightPreference(), product.getStockQuantity(),
                product.isInStock(), product.getImageUrl());
    }

    public String illustrationId() {
        String value = category == null ? "" : category.toLowerCase(Locale.ROOT);
        if (value.contains("fruit") || value.contains("produce")) return "fruit";
        if (value.contains("vegetable")) return "vegetables";
        if (value.contains("dairy") || value.contains("milk") || value.contains("cheese")) return "dairy";
        if (value.contains("bakery") || value.contains("bread")) return "bakery";
        if (value.contains("meat") || value.contains("seafood") || value.contains("fish")) return "seafood";
        if (value.contains("drink") || value.contains("beverage")) return "drinks";
        if (value.contains("snack")) return "snacks";
        if (value.contains("home") || value.contains("house") || value.contains("clean")) return "home";
        return "pantry";
    }
}
