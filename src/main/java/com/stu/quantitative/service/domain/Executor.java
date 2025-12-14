package com.stu.quantitative.service.domain;


import com.stu.quantitative.entity.TradedEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

import java.util.List;


@Slf4j
@Data
public class Executor {
    //  初始化平衡仓
    private final List<BalancePolicy> balancePolicies;
    private final List<TradedEntity> endgames;
    // 最初交易日，最后交易日
    private final LocalDate startDate, middleDate, endDate;
    private final StockPool stockPool;

    // 初始资金50万
    private double cash = 500000.00;

    public Executor(List<TradedEntity> tradeds, List<BalancePolicy> balances, StockPool pool) {
        this.stockPool = pool;
        this.endgames = tradeds;
        this.balancePolicies = balances;
        this.startDate = balances.stream().map(BalancePolicy::getStartKLine).min(LocalDate::compareTo).orElse(null);
        // 回测交易起始日
        this.middleDate = balances.stream().map(BalancePolicy::getStartTradedEnd).max(LocalDate::compareTo).orElse(null);
//        this.middleDate = startDate;
        this.endDate = balances.stream().map(BalancePolicy::getEndKline).max(LocalDate::compareTo).orElse(null);
    }

    public void execute() {
        // 拆分残局交易和回测交易
        // 开始残局交易
        startDate.datesUntil(middleDate.plusDays(1)).forEach(date -> {
            //  执行残局交易
            balancePolicies.forEach(it -> it.endGameExecute(date));
            // 执行清盘任务
            this.stockPool.clearing(date);
        });
        middleDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
            // 循环调用每个账户的execute方法
            balancePolicies.forEach(it -> it.backTradeExechte(date));
            // 执行清盘任务
            this.stockPool.clearing(date);
        });
        // 打印账户持仓信息和资金信息
        this.stockPool.report();
    }

}