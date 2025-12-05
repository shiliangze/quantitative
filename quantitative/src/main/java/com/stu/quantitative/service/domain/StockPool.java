package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.BalanceEntity;
import com.stu.quantitative.entity.StockEntity;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
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
    private final List<StockEntity> stockEntities;
    private final List<BalanceEntity> balances;
    @Getter
    private final List<StockStore> stockStores;
    @Getter
    private final List<BalanceStore> balanceStores;

    public StockPool(List<StockEntity> stockEntities, List<BalanceEntity> balances) {
        this.balances = balances;
        this.stockEntities = stockEntities;
        this.balanceStores = this.balanceToStore(balances, stockEntities);
        this.stockStores = this.balanceStores.stream().flatMap(it -> it.getStocks().stream()).collect(Collectors.toList());
    }

    private List<BalanceStore> balanceToStore(
            List<BalanceEntity> balanceEntities, List<StockEntity> stocks) {

        List<BalanceStore> stores = new ArrayList<>();
        for (BalanceEntity balanceEntity : balanceEntities) {
            // 筛选出与balanceEntity对应的股票
            StockEntity stockEntity = stocks.stream().filter(it -> it.getId() == balanceEntity.getStockId()).findFirst().get();
            StockStore stockStore = new StockStore(stockEntity.getId(), balanceEntity.getId());
            // 是否已经存在该investCode的BalanceStore
            BalanceStore balanceStore = stores.stream().filter(it -> it.getInvestCode() == balanceEntity.getInvestCode()).findFirst().orElse(null);
            if (null == balanceStore) {
                balanceStore = new BalanceStore(balanceEntity.getInvestCode(), 1 - balanceEntity.getShare() * 2, this);
                stores.add(balanceStore);
            }
            balanceStore.addStock(stockStore);
        }
        return stores;
    }


    // 交易中调整现金
    public void exchange(int direction, double delta) {
        // -=因为金额买正卖负，现金操作相反
        this.cash -= direction * delta;
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

//    public StockStore getStockStore(int stockId) {
//        return this.stockStores.stream().filter(stockStore -> stockStore.getStockId() == stockId)
//                .findFirst().get();
//    }

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


