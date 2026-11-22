package com.megamart.api.controller;

import com.megamart.api.dto.OrderCreateRequest;
import com.megamart.dto.order.OrderResponse;
import com.megamart.service.OrderService;
import com.megamart.service.SupabaseAuthService;
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
    private final SupabaseAuthService authService;

    public OrderController(OrderService orderService, SupabaseAuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderCreateRequest request,
                                                    @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authorization) {
        SupabaseAuthService.CustomerIdentity identity = authService.verifyBearerToken(authorization);
        OrderResponse created = orderService.placeOrder(request.recipientName(), request.phone(),
                request.deliveryAddress(), identity.userId(), request.items());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
