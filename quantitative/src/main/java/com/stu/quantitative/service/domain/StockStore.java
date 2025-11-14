package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.BalanceEntity;

import java.util.List;

public class StockStore {
    private final List<BalanceEntity> balances;

    public StockStore(List<BalanceEntity> balances) {
        this.balances = balances;
    }
}
