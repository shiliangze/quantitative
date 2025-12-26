package com.stu.quantitative.service.domain;

import com.stu.quantitative.service.domain.report.TradeReportDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

@Slf4j
public class BalanceAccount {
    @Getter
    private final int investCode;
    private final String investName;
    @Getter
    private double share;
    // 平衡仓总金额
    @Getter
    private final List<StockAccount> stockAccounts ;
    @Getter
    private final StockPool stockPool;
    private final TradeReportDto tradeReportDto;

    // 当日总市值
    @Getter
    private double amount;
    // 平衡仓在总仓位的占比
    @Getter
    private double position, shareOffset, shareCoefficient;

    public BalanceAccount(int investCode,String investName, double share, StockPool stockPool,List<StockAccount> stockAccounts,TradeReportDto tradeReportDto) {
        this.investCode = investCode;
        this.investName = investName;
        this.share = share;
        this.stockPool = stockPool;
        this.stockAccounts = stockAccounts;
        this.tradeReportDto = tradeReportDto;
    }

     // 检查是否可交易
    public boolean tradeable(){
        return this.stockAccounts.stream().anyMatch(StockAccount::tradeable);
    }

    // 计算balance市值
    public double update(LocalDate today) {
        this.amount = this.stockAccounts.stream().mapToDouble(it -> it.update(today)).sum();
        return amount;
    }

    // 平衡仓级别清盘
    public void clearing(double totalAmount, LocalDate date) {
        if(!this.tradeable()){return;}
        // 计算仓位因子
        this.position = amount / totalAmount;
        this.shareOffset = (1.0001-this.position) / (this.share + 0.0001);
        this.shareCoefficient = Math.log10(this.shareOffset +9);
        int direction = this.stockAccounts.stream()
                .filter(it -> it.getDirection() == 1 || it.getDirection() == -1)
                .findFirst().map(StockAccount::getDirection).orElse(0);
        // 盘后清算每个股票
        this.stockAccounts.forEach(it->it.clearing(date,this.shareCoefficient,direction));
        // 平衡仓清盘任务
        this.tradeReportDto.clearing(date, this.investCode, this.investName, this.amount, this.share,
                this.position, this.shareOffset, this.shareCoefficient);

    }
}
