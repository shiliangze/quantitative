package com.stu.quantitative.service.domain;

import lombok.Getter;
import lombok.Setter;

public class BalanceStore {
    @Getter
    private final int investCode;
    // 持仓参数因子
    private final double coefficient;
    // 平衡仓总金额
    @Getter
    @Setter
    private double amount;
    @Getter
    private double putShare, callShare;
    // 平衡仓在总仓位的占比
    @Getter
    private double position;

    public BalanceStore(int investCode, double coefficient) {
        this.investCode = investCode;
        this.coefficient = coefficient;
    }


    public void clearing(double amount, double totalAmount) {
        this.amount = amount;
        this.putShare = (totalAmount - amount) / (amount + this.coefficient * totalAmount);
        this.callShare = 1 - this.putShare;
        this.position = amount / totalAmount;
    }
}
