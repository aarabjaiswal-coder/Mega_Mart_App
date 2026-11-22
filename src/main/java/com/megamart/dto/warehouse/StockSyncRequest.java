package com.megamart.dto.warehouse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StockSyncRequest(@NotEmpty @Size(max = 500) List<@NotNull @Valid StockUpdate> updates) {
    public record StockUpdate(
            @NotBlank @Size(max = 80) String sku,
            @NotNull @Min(0) Integer stockQuantity
    ) { }
}
