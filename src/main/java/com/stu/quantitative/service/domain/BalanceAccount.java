package com.stu.quantitative.service.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BalanceAccount {
    @Getter
    private final int investCode;
    @Getter
    private double share;
    // 平衡仓总金额
    @Getter
    private final List<StockAccount> stockAccounts = new ArrayList<>();
    @Getter
    private final StockPool stockPool;

    // 当日总市值
    @Getter
    @Setter
    private double amount;
    // 平衡仓在总仓位的占比
    @Getter
    private double position, shareOffset, shareCoefficient;
    ;

    public BalanceAccount(int investCode, double share, StockPool stockPool) {
        this.investCode = investCode;
        this.share = share;
        this.stockPool = stockPool;
    }

    public void addStock(StockAccount stock) {
        this.stockAccounts.add(stock);
    }

    // 计算balance市值
    public void calcAmount() {
        this.amount = this.stockAccounts.stream().mapToDouble(StockAccount::getAmount).sum();
    }

    // 平衡仓级别清盘
    public void clearing(double totalAmount, SettlementRecordDto settlementRecordDto, LocalDate date) {


        // 计算仓位因子
        this.position = amount / totalAmount;
        this.shareOffset = (1.0001-this.position) / (this.share + 0.0001);
        this.shareCoefficient = Math.log10(this.shareOffset +9);
        BalanceRecordDto balanceRecordDto = new BalanceRecordDto(
                this.investCode,
//                totalAmount,
                this.amount,
                this.share,
                this.position,
                this.shareOffset,
                this.shareCoefficient
        );
        settlementRecordDto.clearing(balanceRecordDto);


        this.stockAccounts.forEach(it->it.clearing(this.shareCoefficient,balanceRecordDto));
    }
}
