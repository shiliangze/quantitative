package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.BalanceEntity;
import com.stu.quantitative.entity.CodeConfigEntity;
import com.stu.quantitative.entity.StockEntity;
import com.stu.quantitative.service.domain.report.TradeReportDto;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 账户存储类
 * 生命周期：全局
 * 负责存储：1.股票持仓信息 2.资金信息
 * 计算：根据每次交易，更新账户持仓信息和资金信息
 */
public class StockPool {
    // 最小买入卖出数量3000元
    @Getter
    private final double minAmount = 3000.00;
    @Getter
    private final List<StockAccount> stockAccounts;
    @Getter
    private final List<BalanceAccount> balanceAccounts;
    @Getter
    private final TradeReportDto tradeReportDto = new TradeReportDto();

    // 初始资金
    @Getter
    private double cash = 500000.00;
    // 当前总市值
    private double amount = 0.0;

    public StockPool(List<StockEntity> stockEntities, List<BalanceEntity> balanceEntities) {
        this.stockAccounts = stockEntities.stream()
                .map(stockEntity -> new StockAccount(stockEntity, this)).toList();
        this.balanceAccounts = balanceEntities.stream().map(balanceEntity ->
                new BalanceAccount(balanceEntity, this, this.stockAccounts.stream()
                        .filter(stockAccount -> stockAccount.getStockEntity().getBalanceId() == balanceEntity.getId())
                        .sorted(Comparator.comparingInt(stockAccount -> stockAccount.getStockEntity().getPriority()))
                        .toList(), this.tradeReportDto)).toList();
    }


    // 买入操作的前提条件，现金大于等于最低交易金额
    public boolean buyable() {
        return this.cash >= this.minAmount;
    }


    // 交易方向，1：买，-1：卖
    public void exchange(LocalDate date, int stockId, String stockName, int direction, double price, double quantity) {
        // 资金变化与买卖方向相反
        this.cash -= direction * price * quantity;
        //  交易完成后，更新报告信息
        this.tradeReportDto.exchange(date, stockId, stockName, direction, price, quantity);
    }

    // 盘后清算
    public void clearing(LocalDate date) {
        // 1.
        // 2. 计算总市值
        this.amount = this.balanceAccounts.stream().mapToDouble(it -> it.update(date)).sum();
        // 3. 每个balance盘后清算
        this.balanceAccounts.forEach(it -> it.clearing(this.amount + this.cash, date));
        // report：总仓级别结算
        this.tradeReportDto.clearing(date,this.cash,this.amount,this.amount+this.cash);
    }

    public void report(LocalDate startDate, LocalDate endDate){
        this.tradeReportDto.report(startDate, endDate);
    }
}


