package com.stu.quantitative.service.domain;

import lombok.Getter;

/**
 * 单一股票信息存储
 * 生命周期：全生命周期
 */
public class StockStore {
    @Getter
    private final int balanceId;
    @Getter
    private final int stockId;
    // 持仓数量
    @Getter
    private  double quantity = 0.0;
    // 持仓总市值
    @Getter
    private double amount = 0.0;
    // 当前价格
    private double price = 0.0;

    public StockStore(int stockId, int balanceId) {
        this.stockId = stockId;
        this.balanceId = balanceId;
    }

    public void exchange(int direction,double quantity){
        this.quantity += direction * quantity;
    }
    //  更新收盘价，计算最新持仓市值
    public void update(double price){
        this.price = price;
        this.amount = this.quantity * price;
    }
}
