package com.megamart.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 80) String sku,
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 100) String category,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @Size(max = 80) String weight,
        @Size(max = 80) String heightPreference,
        @NotNull @Min(0) Integer stockQuantity,
        @Size(max = 2048) String imageUrl
) { }
