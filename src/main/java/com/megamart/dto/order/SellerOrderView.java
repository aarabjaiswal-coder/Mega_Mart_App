package com.megamart.dto.order;

import com.megamart.model.Order;
import com.megamart.model.OrderStatus;
import com.megamart.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SellerOrderView(
        Long orderId,
        String recipientName,
        String customerPhone,
        String deliveryAddress,
        BigDecimal totalAmount,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        Instant createdAt,
        Instant outForDeliveryAt,
        List<Item> items
) {
    public static SellerOrderView from(Order order) {
        return new SellerOrderView(order.getId(), order.getRecipientName(), order.getCustomer().getPhone(),
                order.getDeliveryAddress(), order.getTotalAmount(), order.getStatus(), order.getPaymentStatus(),
                order.getCreatedAt(), order.getOutForDeliveryAt(), order.getItems().stream()
                .map(item -> new Item(item.getProductId(), item.getProductName(), item.getQuantity(), item.getUnitPrice()))
                .toList());
    }

    public record Item(Long productId, String productName, int quantity, BigDecimal unitPrice) { }
}
