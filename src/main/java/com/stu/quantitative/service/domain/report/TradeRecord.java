package com.stu.quantitative.service.domain.report;

import com.stu.quantitative.service.domain.TradeDirection;

import java.time.LocalDate;

/**
 * 单只股票交易记录
 */
public record TradeRecord(
        LocalDate date,
        int stockId, // 股票ID
        String stockName, // 股票ID
        int direction, // 买入/卖出
        double price, // 交易价格
        double quantity  // 交易数量)
) {
    public void report() {
        System.out.printf("股票名称：%s，%s，价格：%.3f，数量：%.3f%n",
                stockName, TradeDirection.fromCode(direction).getDescription(), price, quantity);
    }
}
