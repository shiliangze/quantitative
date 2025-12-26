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
    private final List<BalanceExecutor> balanceExecutors;
    private final List<TradedEntity> endgames;
    // 最初交易日，最后交易日
    private final LocalDate startDate, middleDate, endDate;
    private final StockPool stockPool;

    // 初始资金50万
    private double cash = 500000.00;

    public Executor(List<TradedEntity> tradeds, List<BalanceExecutor> balances, StockPool pool) {
        this.stockPool = pool;
        this.endgames = tradeds;
        this.balanceExecutors = balances;
        this.startDate = balances.stream().map(BalanceExecutor::getStartKLine).min(LocalDate::compareTo).orElse(null);
        // 回测交易起始日

        this.endDate = balances.stream().map(BalanceExecutor::getEndKline).max(LocalDate::compareTo).orElse(null);
        this.middleDate = this.endDate;
//        this.middleDate = startDate;
//        this.middleDate = balances.stream().map(BalanceExecutor::getStartTradedEnd).max(LocalDate::compareTo).orElse(null);
    }

    public void execute() {
        // 拆分残局交易和回测交易
        // 开始残局交易
//        startDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
//            //  执行残局交易
//            balanceExecutors.forEach(it -> it.endGameExecute(date));
//            // 执行清盘任务
//            this.stockPool.clearing(date);
//
//        });
        startDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
            // 循环调用每个账户的execute方法
            balanceExecutors.forEach(it -> it.backTradeExecute(date));
            // 执行清盘任务
            this.stockPool.clearing(date);
        });
        //  终日清盘数据
        // 执行清盘任务
        // 打印账户持仓信息和资金信息
        this.stockPool.report(startDate, middleDate, endDate);
    }

}
// TODO  0.0  调整表结构：
//  1. stock表新增balanceId字段，关联balance表
//  2. balance表取消investcode，直接使用id字段替代原来的investcode
//  3. balance表每一条记录代表一个balance，而不是以前的stock
//  4. balance表中添加name字段，并取消codeconfig中的invest映射
// TODO 1.0  添加佣金模块
// TODO 2.0 根据输入执行回测或历史交易
