package com.megamart.dto.order;

import java.math.BigDecimal;
import java.util.List;

public record CartSummaryResponse(
        int itemCount,
        BigDecimal subtotal,
        List<Item> items,
        String message,
        boolean success
) {
    public CartSummaryResponse {
        items = List.copyOf(items);
    }

    public record Item(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) { }
}
