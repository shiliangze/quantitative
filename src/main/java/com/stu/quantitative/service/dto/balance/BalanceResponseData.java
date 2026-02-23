package com.stu.quantitative.service.dto.balance;

import java.time.LocalDate;
import java.util.List;

// 获取结余
public record BalanceResponseData(
        int balanceId,
        int marketId,
        String name,
        String note,
        double share,
        double amount,
        double percentage,
        LocalDate lastDate,
        List<StockResponseData> stocks
) {
}


