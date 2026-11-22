package com.megamart.service;

import com.megamart.dto.warehouse.StockSyncRequest;
import com.megamart.dto.warehouse.StockSyncResponse;
import com.megamart.exception.BusinessException;
import com.megamart.exception.WarehouseSyncException;
import com.megamart.model.Product;
import com.megamart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
public class WarehouseSyncService {
    private final ProductRepository productRepository;
    private final String configuredApiKey;

    public WarehouseSyncService(ProductRepository productRepository,
                                @Value("${app.warehouse.api-key:}") String configuredApiKey) {
        this.productRepository = productRepository;
        this.configuredApiKey = configuredApiKey == null ? "" : configuredApiKey;
    }

    @Transactional
    public StockSyncResponse sync(String suppliedApiKey, StockSyncRequest request) {
        if (configuredApiKey.isBlank()) {
            throw new WarehouseSyncException(WarehouseSyncException.Reason.NOT_CONFIGURED,
                    "Warehouse synchronization is not configured on this server.");
        }
        if (suppliedApiKey == null || !MessageDigest.isEqual(
                configuredApiKey.getBytes(StandardCharsets.UTF_8),
                suppliedApiKey.getBytes(StandardCharsets.UTF_8))) {
            throw new WarehouseSyncException(WarehouseSyncException.Reason.INVALID_KEY, "Invalid warehouse API key.");
        }
        if (request == null || request.updates() == null || request.updates().isEmpty()
                || request.updates().size() > 500) {
            throw new BusinessException("Provide between 1 and 500 stock updates.");
        }

        Map<String, Integer> updates = new TreeMap<>();
        for (StockSyncRequest.StockUpdate update : request.updates()) {
            if (update == null || update.sku() == null || update.sku().isBlank()
                    || update.sku().trim().length() > 80
                    || update.stockQuantity() == null || update.stockQuantity() < 0) {
                throw new BusinessException("Each update needs a SKU and non-negative stock quantity.");
            }
            String sku = update.sku().trim().toUpperCase(Locale.ROOT);
            if (updates.putIfAbsent(sku, update.stockQuantity()) != null) {
                throw new BusinessException("Each SKU may appear only once in a batch.");
            }
        }

        int updatedCount = 0;
        List<String> unknownSkus = new ArrayList<>();
        for (Map.Entry<String, Integer> update : updates.entrySet()) {
            Product product = productRepository.findBySkuForUpdate(update.getKey()).orElse(null);
            if (product == null) {
                unknownSkus.add(update.getKey());
                continue;
            }
            product.setStockQuantity(update.getValue());
            updatedCount++;
        }
        return new StockSyncResponse(updatedCount, List.copyOf(unknownSkus));
    }
}
