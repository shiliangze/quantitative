package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.BalanceEntity;
import com.stu.quantitative.entity.StockEntity;
import lombok.Getter;

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

    // 总现金数量
    @Getter
    private double cash = 500000;
    // 最小买入卖出数量3000元
    @Getter
    private final double minAmount = 3000.00;
    // 股票持仓信息，key：股票id，value：持仓数量
    @Getter
    private final List<StockEntity> stocks;
    private final List<BalanceEntity> balances;
    private final List<StockStore> stockStores;
    @Getter
    private final List<BalanceStore> balanceStores;

    public StockPool(List<StockEntity> stocks, List<BalanceEntity> balances) {
        this.balances = balances;
        this.balanceStores = this.balanceToInfo(balances);
        //  筛选出planCode下的股票
        this.stocks = stocks.stream()
                .filter(it -> balances.stream()
                        .anyMatch(b -> b.getStockId() == it.getId()))
                .collect(Collectors.toList());

        this.stockStores = this.stocks.stream().map(it -> {
            BalanceEntity balance = balances.stream().filter(b -> b.getStockId() == it.getId()).findFirst().get();
            return new StockStore(it.getId(), balance.getId());
        }).collect(Collectors.toList());
    }

    private List<BalanceStore> balanceToInfo(List<BalanceEntity> balanceEntities) {
        Set<Integer> investCodes = balanceEntities.stream().map(BalanceEntity::getInvestCode).collect(Collectors.toSet());
        return investCodes.stream().map(it -> {
            double share = balanceEntities.stream().filter(be -> be.getInvestCode() == it).findFirst().get().getShare();
            return new BalanceStore(it, 1 - share * 2);
        }).toList();
    }


    //  TODO



    // 执行交易
    // stockId：股票id，direction：交易方向，1：买，-1：卖，price：交易价格，quantity：交易数量
    public void exchange(int stockId, int direction, double price, double quantity) {
        this.cash -= direction * price * quantity;
        this.stockStores.stream().filter(stockStore -> stockStore.getStockId() == stockId)
                .findFirst().get().exchange(direction, quantity);
    }

    // 实时计算总市值
    public double totalAmount() {
        return this.stockStores.stream().mapToDouble(StockStore::getAmount).sum();
    }

    // 买入操作的前提条件，现金大于等于最低交易金额
    public boolean buyable() {
        return this.cash >= this.minAmount;
    }

    public boolean sellable(int stockId) {
        return this.stockStores.stream().filter(stockStore -> stockStore.getStockId() == stockId)
                .findFirst().get().getAmount() >= this.minAmount;
    }

    public StockStore getStockStore(int stockId) {
        return this.stockStores.stream().filter(stockStore -> stockStore.getStockId() == stockId)
                .findFirst().get();
    }

    public BalanceStore getBalanceStoreByStockId(int stockId) {
        int balanceId = this.stockStores.stream().filter(stockStore -> stockStore.getStockId() == stockId)
                .findFirst().get().getBalanceId();
        return this.balanceStores.stream().filter(it -> it.getInvestCode() == balanceId).findFirst().get();
    }


    public void blanceClearing(int balanceId) {
        // balanceAmount：平衡仓总市值
//        当前balance仓的总市值
        double balanceAmount = this.stockStores.stream()
                .filter(stockStore -> stockStore.getBalanceId() == balanceId)
                .mapToDouble(StockStore::getAmount).sum();

        BalanceStore balanceStore = this.balanceStores.stream().filter(it -> it.getInvestCode() == balanceId).findFirst().get();
        balanceStore.clearing(balanceAmount, this.totalAmount());
    }
}


