package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.BalanceEntity;
import com.stu.quantitative.entity.CodeConfigEntity;
import com.stu.quantitative.entity.StockEntity;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    // 全量stock
    @Getter
    private final List<StockEntity> stockEntities;
    private final List<BalanceEntity> balances;
    private final List<CodeConfigEntity> investNameCodes;
    @Getter
    private final List<BalanceAccount> balanceAccounts;
    private final TradeReportDto tradeReportDto = new TradeReportDto();

    // 初始资金
    @Getter
    private double cash = 500000.00;
    // 当前总市值
    private double amount = 0.0;

    public StockPool(
            List<StockEntity> stockEntities,
            List<BalanceEntity> balances,
            List<CodeConfigEntity> investNameCodes
    ) {
        this.balances = balances;
        this.stockEntities = stockEntities;
        this.balanceAccounts = this.balanceToStore(balances, stockEntities);
        this.investNameCodes = investNameCodes;
    }

    private List<BalanceAccount> balanceToStore(
            List<BalanceEntity> balanceEntities, List<StockEntity> stocks) {

        List<BalanceAccount> stores = new ArrayList<>();
        for (BalanceEntity balanceEntity : balanceEntities) {
            // 筛选出与balanceEntity对应的股票
            StockEntity stockEntity = stocks.stream().filter(it -> it.getId() == balanceEntity.getStockId()).findFirst().get();
            StockAccount stockAccount = new StockAccount(stockEntity, this, balanceEntity.getInvestCode());
            // 是否已经存在该investCode的BalanceStore
            BalanceAccount balanceAccount = stores.stream().filter(it -> it.getInvestCode() == balanceEntity.getInvestCode()).findFirst().orElse(null);
            if (null == balanceAccount) {
                balanceAccount = new BalanceAccount(balanceEntity.getInvestCode(), balanceEntity.getShare(), this);
                stores.add(balanceAccount);
            }
            balanceAccount.addStock(stockAccount);
        }
        return stores;
    }


    // 买入操作的前提条件，现金大于等于最低交易金额
    public boolean buyable() {
        return this.cash >= this.minAmount;
    }


    // 交易方向，1：买，-1：卖
    public void exchange(
            LocalDate date, int investCode, int stockCode,
            int direction, double price, double quantity) {
        // 资金变化与买卖方向相反
        this.cash -= direction * price * quantity;
        //  交易完成后，更新报告信息
        this.tradeReportDto.exchange(
                date,
                investCode,
                stockCode,
                direction,
                String.format("%.3f", price),
                String.format("%.3f", quantity)
        );
    }


    public void clearing(LocalDate date) {
        // 1. 分别计算每个balance的市值
        this.balanceAccounts.forEach(BalanceAccount::calcAmount);
        // 2. 计算总市值
        this.amount = this.balanceAccounts.stream().mapToDouble(BalanceAccount::getAmount).sum();
        // 3. 每个balance盘后清算
        this.balanceAccounts.forEach(it -> it.clearing(this.amount + this.cash,tradeReportDto,date));
    }

    // TODO 1.1 打印账户持仓信息和资金信息
    public void report(int direction) {
        TradeDirection.fromCode(direction).getDescription();
    }

    public enum TradeDirection {
        BUY(1, "买入"),
        NO_TRADE(0, "无交易"),
        SELL(-1, "卖出"),
        PAYOUT(2, "派息");


        private final int code;
        private final String description;

        TradeDirection(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        // 根据code获取枚举
        public static TradeDirection fromCode(int code) {
            return Arrays.stream(TradeDirection.values()).filter(it -> it.code == code).findFirst().orElse(NO_TRADE);
        }
    }

}


