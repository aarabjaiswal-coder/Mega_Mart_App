package com.megamart.dto.warehouse;

import java.util.List;

public record StockSyncResponse(int updatedProductsCount, List<String> unknownSkus) { }
