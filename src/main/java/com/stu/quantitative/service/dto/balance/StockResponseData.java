package com.stu.quantitative.service.dto.balance;


import java.time.LocalDate;

// 获取结余
public record StockResponseData(
        int stockId,
        int balanceId,
        String ticker,
        String name,
        LocalDate ipo,
        double price,
        double quantity,
        double amount,
        LocalDate lastDate
) {
    // 自定义构造方法，自动计算amount
    public StockResponseData(int stockId, int balanceId, String ticker, String name, LocalDate ipo, double price, double quantity, LocalDate lastDate) {
        this(stockId, balanceId, ticker, name, ipo, price, quantity, price * quantity, lastDate);
    }
}

