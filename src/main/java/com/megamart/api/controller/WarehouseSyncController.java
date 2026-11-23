package com.megamart.api.controller;

import com.megamart.dto.warehouse.StockSyncRequest;
import com.megamart.dto.warehouse.StockSyncResponse;
import com.megamart.service.WarehouseSyncService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseSyncController {
    private final WarehouseSyncService warehouseSyncService;

    public WarehouseSyncController(WarehouseSyncService warehouseSyncService) {
        this.warehouseSyncService = warehouseSyncService;
    }

    @PostMapping("/sync-stock")
    public StockSyncResponse syncStock(
            @RequestHeader(value = "X-Warehouse-API-Key", required = false) String apiKey,
            @Valid @RequestBody StockSyncRequest request) {
        return warehouseSyncService.sync(apiKey, request);
    }
}
