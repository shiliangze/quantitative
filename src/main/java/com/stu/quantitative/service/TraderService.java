package com.stu.quantitative.service;

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
     * @param planCode
     */
    public void execute(int planCode) {
        List<CodeConfigEntity> investNameCodes = this.codeConfigService.findAllBySku("investName");
        List<StockEntity> stocks = this.stockService.findAll();
        List<TradedEntity> tradeds = this.tradedService.findAllByOrderByDate();
        List<BalanceEntity> balances = this.balanceService.findAllByPlanCode(planCode);

        // 生成stockPool
        StockPool pool = new StockPool(stocks, balances,investNameCodes);
        //  生成balanceExecutor数组
        List<BalanceExecutor> balanceExecutors = pool.getBalanceAccounts().stream().map(it -> {
                    // 找到对应investCode的所有股票id
                    List<StockExecutor> stockExecutorsForBalance = it.getStockAccounts().stream()
                            .map(stock -> {
                                List<TradedEntity> traders = this.tradedService.findAllByStockIdOrderByDate(stock.getStockEntity().getId());
                                List<PriceEntity> klines = this.priceService.findByTickerOrderByDate(stock.getStockEntity().getTicker());
                                return new StockExecutor(stock, it, klines,traders);
                            }).toList();
                    //String investName = investNameCodes.stream().filter(code -> code.getCode() == investCode).map(CodeConfigEntity::getValue).findFirst().get();
                    // 调试通过后，全部放到构造器里面
                    return new BalanceExecutor(it,stockExecutorsForBalance);
                }
        ).toList();

        Executor executor = new Executor(tradeds, balanceExecutors,pool);
        executor.execute();
    }


}