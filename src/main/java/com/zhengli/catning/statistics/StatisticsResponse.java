package com.zhengli.catning.statistics;

import java.math.BigDecimal;
import java.util.List;

public record StatisticsResponse(
        BigDecimal totalSales,
        long completedOrders,
        long activeProducts,
        List<TopProductResponse> topProducts
) {
}
