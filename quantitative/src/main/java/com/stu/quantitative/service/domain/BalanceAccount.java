package com.stu.quantitative.service.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BalanceAccount {
    @Getter
    private final int investCode;
    @Getter
    private final double share;
    // 持仓参数因子
    private final double coefficient;
    // 平衡仓总金额
    @Getter
    private final List<StockAccount> stockAccounts = new ArrayList<>();
    @Getter
    private final StockPool stockPool;

    // 当日总市值
    @Getter
    @Setter
    private double amount;
    @Getter
    private double putShare, callShare;
    // 平衡仓在总仓位的占比
    @Getter
    private double position;

    public BalanceAccount(int investCode, double share, StockPool stockPool) {
        this.investCode = investCode;
        this.share = share;
        this.coefficient = 1 - share * 2;
        this.stockPool = stockPool;
    }

    public void addStock(StockAccount stock) {
        this.stockAccounts.add(stock);
    }

    // 计算balance市值
    public void  calcAmount() {
        this.amount = this.stockAccounts.stream().mapToDouble(StockAccount::getAmount).sum();
    }
    // 平衡仓级别清盘
    public void clearing(double totalAmount, TradeReportDto tradeReportDto, LocalDate date) {
        // 计算仓位因子
        this.putShare = (totalAmount - this.amount) / (amount + this.coefficient * totalAmount);
        this.callShare = 1 / this.putShare;
        this.position = amount / totalAmount;
        // TODO 1.1 打印账户持仓信息和资金信息
        tradeReportDto.clearing(date);
        this.stockAccounts.forEach(it->it.clearing(this.callShare,this.putShare,tradeReportDto));
    }
}
