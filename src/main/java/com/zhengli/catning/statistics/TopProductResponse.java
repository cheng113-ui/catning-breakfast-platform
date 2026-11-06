package com.zhengli.catning.statistics;

import java.math.BigDecimal;

public record TopProductResponse(
        String productName,
        long quantity,
        BigDecimal salesAmount
) {
}
