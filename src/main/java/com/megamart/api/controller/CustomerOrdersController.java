package com.megamart.api.controller;

import com.megamart.dto.order.OrderResponse;
import com.megamart.service.OrderService;
import com.megamart.service.SupabaseAuthService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/orders")
public class CustomerOrdersController {
    private final SupabaseAuthService authService;
    private final OrderService orderService;

    public CustomerOrdersController(SupabaseAuthService authService, OrderService orderService) {
        this.authService = authService;
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> mine(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        SupabaseAuthService.CustomerIdentity identity = authService.verifyBearerToken(authorization);
        return orderService.customerOrdersForAccount(identity.userId(), PageRequest.of(0, 10));
    }
}
