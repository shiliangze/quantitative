package com.stu.quantitative.service.domain;


import com.stu.quantitative.dto.BackTrackRequestDto;
import com.stu.quantitative.dto.BackTrackResponseDto;
import com.stu.quantitative.dto.BackTrackStockDto;
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
    private final LocalDate startDate, endDate;
    private final StockPool stockPool;
    private final int tradeType;

    // 初始资金50万
    private final double cash;

    public Executor(List<TradedEntity> tradeds, List<BalanceExecutor> balances, StockPool pool, BackTrackRequestDto backTrackRequestDto) {
        this.stockPool = pool;
        this.endgames = tradeds;
        this.balanceExecutors = balances;
        this.cash = backTrackRequestDto.cash();
        this.tradeType = backTrackRequestDto.tradeType();
        this.startDate = balances.stream().map(BalanceExecutor::getStartKLine).min(LocalDate::compareTo).orElse(null);
        // 回测交易起始日
        this.endDate = balances.stream().map(BalanceExecutor::getEndKline).max(LocalDate::compareTo).orElse(null);
    }

    public void execute() {
        BackTrackResponseDto backTrackResponseDto = new BackTrackResponseDto(
        switch (this.tradeType){
            case 0 -> endGameExecute();
            case 1 -> backTradeExecute();
            default -> throw new IllegalArgumentException("tradeType must be 0 or 1");
        });
        //  终日清盘数据
        // 执行清盘任务
        // 打印账户持仓信息和资金信息
        this.stockPool.report(startDate, endDate);
    }

    private List<BackTrackStockDto> endGameExecute(){
        // 拆分残局交易和回测交易
        // 开始残局交易
        startDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
            //  执行残局交易
            balanceExecutors.forEach(it -> it.endGameExecute(date));
            // 执行清盘任务
            this.stockPool.clearing(date);
        });
        return null;
    }
    private List<BackTrackStockDto> backTradeExecute(){
        // 拆分残局交易和回测交易
        // 开始回测交易
        startDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
            //  执行回测交易
            balanceExecutors.forEach(it -> it.backTradeExecute(date));
            // 执行清盘任务
            this.stockPool.clearing(date);
        });
        return null;
    }
}
// TODO  0.0  setipo

// TODO 1.0  添加佣金模块
// TODO 2.0 根据输入执行回测或历史交易
