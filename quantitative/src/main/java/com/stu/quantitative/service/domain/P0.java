package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.entity.TradedEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
public class P0 {
    //  初始化平衡仓
    private final List<B0> balanceAccounts;
    private final List<TradedEntity> endgames;
    // 最初交易日，最后交易日
    private final LocalDate startDate, middleDate, endDate;
    private final Map<String, List<PriceEntity>> kLines;

    // 初始资金50万
    private double cash = 500000.00;
    private Tr0 tradeReport;

    public P0(List<TradedEntity> tradeds, List<B0> balances, Map<String, List<PriceEntity>> kLines) {
        this.endgames = tradeds;
        this.balanceAccounts = balances;
        this.kLines = kLines;
        // 对每个balanceAccount注入policy引用
        this.balanceAccounts.forEach(it -> it.injectPolicy(this));
        Collection<List<PriceEntity>> prices = kLines.values();
        // 取所有balanceAccoount中最早的一天作为startDate
        //balanceAccount的kLine是已经排序的，所以直接取第一个元素即可，在比较所有投资品的第一个元素的交易日，取最小值即可
        this.startDate = prices.stream().map(it -> it.getFirst().getDate()).min(LocalDate::compareTo).orElseThrow();
        // 中间日为残局的最后一天
        this.middleDate = this.trackBackMiddleDate();      // 回测交易
//        this.middleDate = this.endgames.getLast().getDate().plusDays(1);       // 实盘交易
        this.endDate = prices.stream().map(it -> it.getLast().getDate()).max(LocalDate::compareTo).orElseThrow();
    }


    public void execute() {
        this.tradeReport = new Tr0(startDate, middleDate, endDate);
        // 拆分残局交易和回测交易
        // 开始残局交易
        startDate.datesUntil(middleDate.plusDays(1)).forEach(date -> {
            Td0 record = new Td0(date, getAllKLinesByDate(date));
            // 循环调用每个账户的execute方法
            balanceAccounts.forEach(it -> it.endGameExecute(record));
            if (!record.getTrades().isEmpty() || date.equals(endDate)) {
                // report跳过无交易日，最后一天强行执行
                this.balanceAccounts.forEach(it -> {
                    it.addShareToLog(record);
                });
                record.setCash(this.cash);
                record.setAmount(this.totalAmount());
                this.tradeReport.add(record);
            }
        });
        // 开始回测交易
        middleDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
            Td0 record = new Td0(date, getAllKLinesByDate(date));
            // 循环调用每个账户的execute方法
            balanceAccounts.forEach(it -> it.backTradeExechte(record));
            if (!record.getTrades().isEmpty() || date.equals(endDate)) {
                // report跳过无交易日，最后一天强行执行
                this.balanceAccounts.forEach(it -> {
                    it.addShareToLog(record);
                });
                record.setCash(this.cash);
                record.setAmount(this.totalAmount());
                this.tradeReport.add(record);
            }
        });
        this.tradeReport.printAll();
    }


    public void exchange(double amount) {
        this.cash += amount;
    }

    // 获取账户总市值：所有股票市值+现金市值
    public double totalAmount() {
        return this.balanceAccounts.stream()
                .mapToDouble(B0::totalAmount).sum()
                + this.cash;
    }

    //  回测交易的中间日：所有股票至少发生一笔交易的日期
    private LocalDate trackBackMiddleDate(){
        // 从所有PriceEntity中获取最早的日期
        List<Integer> stockIds =this.kLines.values().stream()
                .filter(list -> !list.isEmpty()) // 过滤空列表
                .map(List::getFirst) // 提取每个列表中的第一个元素
                .map(PriceEntity::getStockId) // 获取stockId
                .toList(); // 收集结果到List

        return  this.endgames.stream()
                // 按stockId分组
                .collect(Collectors.groupingBy(TradedEntity::getStockId))
                // 处理每个分组
                .values().stream()
                // 过滤股票k线以外的所有股票
                .filter(list -> list.stream().anyMatch(it -> stockIds.contains(it.getStockId())))
                // 对每个stockId分组，找到最早的日期
                .map(stockTrades -> stockTrades.stream()
                        .map(TradedEntity::getDate)
                        .findFirst().orElse(null)
                        )
                // 在所有最早日期中找到最晚的日期
                .max(LocalDate::compareTo)
                // 如果没有数据则返回null
                .orElse(null);
    }


    // 获取当天的所有股票K线
    private Map<String, Optional<PriceEntity>> getAllKLinesByDate(LocalDate date) {
        return kLines.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,  // key是股票代码
                        entry -> entry.getValue().stream()
                                .filter(it -> it.getDate().equals(date))
                                .findAny()  // 保持Optional类型
                ));
    }

}