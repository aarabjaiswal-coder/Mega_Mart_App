package com.megamart.dto.order;

import com.megamart.model.Order;
import com.megamart.model.OrderStatus;
import com.megamart.model.PaymentMode;
import com.megamart.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String recipientName,
        String phone,
        String deliveryAddress,
        BigDecimal totalAmount,
        OrderStatus orderStatus,
        PaymentMode paymentMode,
        PaymentStatus paymentStatus,
        Instant createdAt,
        List<Item> items,
        String message
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getRecipientName(), order.getCustomer().getPhone(),
                order.getDeliveryAddress(), order.getTotalAmount(), order.getStatus(), order.getPaymentMode(),
                order.getPaymentStatus(), order.getCreatedAt(), order.getItems().stream()
                .map(item -> new Item(item.getProductId(), item.getProductName(), item.getQuantity(), item.getUnitPrice()))
                .toList(), "Order placed successfully. Payment is Cash on Delivery.");
    }

    public record Item(Long productId, String productName, int quantity, BigDecimal unitPrice) { }
}
