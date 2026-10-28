package com.megamart.api.controller;

import com.megamart.api.ApiPageRequest;
import com.megamart.api.dto.CashCollectedRequest;
import com.megamart.api.dto.OrderStatusRequest;
import com.megamart.api.dto.PageResponse;
import com.megamart.dto.order.SellerOrderView;
import com.megamart.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public PageResponse<SellerOrderView> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return PageResponse.from(orderService.sellerOrders(
                ApiPageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/{id}")
    public SellerOrderView get(@PathVariable Long id) {
        return orderService.sellerOrder(id);
    }

    @PatchMapping("/{id}/status")
    public SellerOrderView updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
        orderService.updateStatus(id, request.status());
        return orderService.sellerOrder(id);
    }

    @PatchMapping("/{id}/cash-collected")
    public SellerOrderView markCashCollected(@PathVariable Long id,
                                             @Valid @RequestBody CashCollectedRequest request) {
        orderService.markCashCollected(id);
        return orderService.sellerOrder(id);
    }
}
