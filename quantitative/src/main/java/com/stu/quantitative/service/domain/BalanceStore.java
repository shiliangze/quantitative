package com.stu.quantitative.service.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class BalanceStore {
    @Getter
    private final int investCode;
    // 持仓参数因子
    private final double coefficient;
    // 平衡仓总金额
    @Getter
    private final List<StockStore> stocks = new ArrayList<>();
    @Getter
    private final StockPool stockPool;

    @Getter
    @Setter
    private double amount;
    @Getter
    private double putShare, callShare;
    // 平衡仓在总仓位的占比
    @Getter
    private double position;

    public BalanceStore(int investCode, double coefficient, StockPool stockPool) {
        this.investCode = investCode;
        this.coefficient = coefficient;
        this.stockPool = stockPool;
    }

    public void addStock(StockStore stock) {
        this.stocks.add(stock);
    }

    // 执行交易
    // stockId：股票id，direction：交易方向，1：买，-1：卖，price：交易价格，quantity：交易数量
    public void exchange(int stockId, int direction, double price, double quantity) {
        this.stockPool.exchange(direction, price * quantity);
        this.stocks.stream().filter(stockStore -> stockStore.getStockId() == stockId)
                .findFirst().get().exchange(direction, quantity);
    }


    // 平衡仓级别清盘
    public void clearing(double amount, double totalAmount) {
        this.amount = amount;
        this.putShare = (totalAmount - amount) / (amount + this.coefficient * totalAmount);
        this.callShare = 1 / this.putShare;
        this.position = amount / totalAmount;
    }
}
