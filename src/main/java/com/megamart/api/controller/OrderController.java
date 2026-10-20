package com.megamart.api.controller;

import com.megamart.api.dto.OrderCreateRequest;
import com.megamart.dto.order.OrderResponse;
import com.megamart.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse created = orderService.placeOrder(request.recipientName(), request.phone(),
                request.deliveryAddress(), request.items());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
