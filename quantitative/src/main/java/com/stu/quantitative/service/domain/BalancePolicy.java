package com.stu.quantitative.service.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BalancePolicy {
    private final List<StockPolicy> stocks;
    private final BalanceAccount balanceAccount;
    public BalancePolicy(BalanceAccount balanceAccount, List<StockPolicy> stocks) {
        this.balanceAccount = balanceAccount;
        this.stocks = stocks;
    }

    public LocalDate getStartKLine() {
        return this.stocks.stream().map(StockPolicy::getStartKLine).min(LocalDate::compareTo).orElse(null);
    }

    //所有股票至少有一笔交易的日期
    public LocalDate getStartTradedEnd() {
        return this.stocks.stream().map(StockPolicy::getStartTraded).max(LocalDate::compareTo).orElse(null);
    }

    // 所有股票都至少交易一次：所有股票首次交易日的最后一个
    public LocalDate getEndKline() {
        return this.stocks.stream().map(StockPolicy::getEndKline).max(LocalDate::compareTo).orElse(null);
    }

    // 历史交易
    public void endGameExecute(LocalDate date) {
        this.balanceAccount.endGameExecute(date);
        // 所有股票全都执行一遍execute
        this.stocks.stream()
                .filter(it -> it.tradeable(date))// 过滤无交易的日期
                .peek(StockPolicy::initEndGame)// 初始化当日的历史交易
                .forEach(StockPolicy::endGameExecute);// 执行交易
    }

    // 回测交易
    public void backTradeExechte(LocalDate date) {
        this.stocks.stream()
                .filter(it -> it.tradeable(date))// 过滤无交易的日期
                .anyMatch(it->it.backTradeExecute() !=0);
    }
}
