package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.StockEntity;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BalancePolicy {
    private final double expectedShareRate;
    // 仓位系数
    private final double coefficient;
    private final String investName;
    private final List<StockEntity> stocks;
    private final List<StockAccount> stockAccounts = null;


    private Policy policy;
    private LocalDate currentDate;
    // 总仓位
    private double shareAmount = 0.0;
    // 买入卖出仓位因子
    private double callShare = 0.00, putShare = 0.00;
    // 仓位占比
    private double position = 0.00;

    public BalancePolicy(String investName, double expectedShareRate, List<StockEntity> stocks){
        this.expectedShareRate = expectedShareRate;
        this.coefficient = 1 - this.expectedShareRate * 2;
        this.investName = investName;
        this.stocks =stocks;
//        this.stockAccounts = stocks.stream().map(it->new StockAccount(this,it)).toList();
    }

    // 残局交易
    public void endGameExecute(TradeDateDto record){
        this.currentDate = record.getDate();
        // 所有股票全都执行一遍execute
        // execute 返回交易方向，如果有两笔交易，正负抵消，则等价为无交易
        int direction = this.stockAccounts.stream()
                // 过滤已上市股票
                .filter(it->it.tradeable(record))
                // 执行交易
                .mapToInt(it->it.endGameExecute(record)).sum();
        // 2. 执行盘后清算任务
        // 2.1 计算当天仓位总数
        this.shareAmount = this.stockAccounts.stream().mapToDouble(StockAccount::getShareAmount).sum();
        // 2.2 计算仓位占比
        this.position = this.shareAmount / this.policy.totalAmount();
        // 2.3 对每个股票进行盘后清算
        this.stockAccounts.forEach(it -> it.clearing(record,direction));
    }
    // 回测交易
    public void backTradeExechte(TradeDateDto record){
        this.currentDate = record.getDate();
        // 1.执行盘中交易任务
        // 规则：主产品如果没上市，自动选择第二产品，直到主产品上市，随后开始替换副产品
        // 1.1 已上市，并有足够仓位的股票
        int direction = this.stockAccounts.stream()
                // 过滤未上市的股票
                .filter(it->it.tradeable(record))
                // 过滤后的股票，选择第一优先的股票进行操作
                //规则，买入，资金足够就买入第一优先级，卖出，依次判断是否仓位足够，卖出最高优先级后短路退出
                .mapToInt(it->it.backTradeExechte(record))
                // 发生交易即短路退出，一天交易只操作一只股票
                .filter(it -> it != 0).findFirst().orElse(0);

        // 1.2 判断该股票是否有足够仓位
        // 2. 执行盘后清算任务
        // 2.1 计算当天仓位总数
        this.shareAmount = this.stockAccounts.stream().filter(it->it.tradeable(record))
                .mapToDouble(StockAccount::getShareAmount).sum();
        // 2.2 计算仓位占比
        this.position = this.shareAmount / this.policy.totalAmount();
        // 2.3 对每个股票进行盘后清算
        this.stockAccounts.stream().filter(it->it.tradeable(record))
                .forEach(it -> it.clearing(record,direction));
    }

    public void injectPolicy(Policy policy){
        this.policy = policy;
        this.stockAccounts.forEach(it -> it.setPolicy(policy));
    }

    // 获取实时仓位总数
    public double totalAmount() {
        return this.stockAccounts.stream()
                .mapToDouble(StockAccount::getShareAmount).sum();
    }

    public void share() {
        // 卖出仓位因子 = 现金/仓位金额
        this.putShare = (this.policy.totalAmount() - this.shareAmount) / (this.shareAmount + this.coefficient * this.policy.totalAmount());
        this.callShare = 1 / this.putShare;
    }

    public void addShareToLog(TradeDateDto record){
        this.stockAccounts.stream().filter(it->it.tradeable(record))
                .forEach(it->it.addShareToLog(record));
    }
}
