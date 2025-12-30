package com.stu.quantitative.service.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BalanceExecutor {
    private final List<StockExecutor> stocks;
    private final BalanceAccount balanceAccount;

    public BalanceExecutor(BalanceAccount balanceAccount, List<StockExecutor> stocks) {
        this.balanceAccount = balanceAccount;
        this.stocks = stocks;
    }

    public LocalDate getStartKLine() {
        return this.stocks.stream().map(StockExecutor::getStartKLine).min(LocalDate::compareTo).orElse(null);
    }

    //所有股票至少有一笔交易的日期
    public LocalDate getStartTradedEnd() {
        return this.stocks.stream().map(StockExecutor::getStartTraded).max(LocalDate::compareTo).orElse(null);
    }

    // 所有股票都至少交易一次：所有股票首次交易日的最后一个
    public LocalDate getEndKline() {
        return this.stocks.stream().map(StockExecutor::getEndKline).max(LocalDate::compareTo).orElse(null);
    }

    // 历史交易
    public void endGameExecute(LocalDate date) {
        // 所有股票全都执行一遍execute
        this.stocks.stream()
                .filter(it -> it.tradeable(date))// 过滤无交易的日期
                .peek(StockExecutor::initEndGame)// 初始化当日的历史交易
                .forEach(StockExecutor::endGameExecute);// 执行交易
    }

    // 回测交易
    public void backTradeExecute(LocalDate date) {
        this.stocks.stream()
                .filter(it -> it.tradeable(date))
                // -2代表该股票因数量不足而无法卖出
                // takeWhile 遇到非-2时，停止执行
                // 当且仅当高优先级的股票无法卖出，递补进行下一股票的交易
                .takeWhile(it->it.backTradeExecute(this.balanceAccount.getAmount())==-2)
                .findAny();
    }
}
