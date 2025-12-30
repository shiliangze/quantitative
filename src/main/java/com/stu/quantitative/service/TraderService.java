package com.stu.quantitative.service;

import com.stu.quantitative.dto.BackTrackRequestDto;
import com.stu.quantitative.entity.*;
import com.stu.quantitative.service.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class TraderService {
    @Autowired
    private CodeConfigService codeConfigService;

    @Autowired
    private PriceService priceService;

    @Autowired
    private TradedService tradedService;

    @Autowired
    private StockService stockService;

    @Autowired
    private BalanceService balanceService;


    /**
     * 执行回测交易
     *
     * @param backTrackRequestDto
     */
    public void execute(BackTrackRequestDto backTrackRequestDto) {
        List<StockEntity> stocks = this.stockService.findAll();
        List<TradedEntity> tradeds = this.tradedService.findAllByOrderByDate();
        List<BalanceEntity> balances = this.balanceService.findAllByPlanCode(backTrackRequestDto.planCode());

        // 生成stockPool
        StockPool pool = new StockPool(stocks, balances);
        //  生成balanceExecutor数组
        List<BalanceExecutor> balanceExecutors = pool.getBalanceAccounts().stream().map(it -> {
                    // 找到对应investCode的所有股票id
                    List<StockExecutor> stockExecutorsForBalance = it.getStockAccounts().stream()
                            .map(stock -> {
                                List<TradedEntity> traders = this.tradedService.findAllByStockIdOrderByDate(stock.getStockEntity().getId());
                                List<PriceEntity> klines = this.priceService.findByStockIdOrderByDate(stock.getStockEntity().getId());
                                return new StockExecutor(stock,it.getBalanceEntity().getShare(), klines,  traders);
                            }).toList();
                    // 调试通过后，全部放到构造器里面
                    return new BalanceExecutor(it,stockExecutorsForBalance);
                }
        ).toList();

        Executor executor = new Executor(tradeds, balanceExecutors,pool, backTrackRequestDto);
        executor.execute();
    }
}