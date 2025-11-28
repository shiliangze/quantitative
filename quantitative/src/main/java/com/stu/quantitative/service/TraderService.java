package com.stu.quantitative.service;

import com.stu.quantitative.entity.BalanceEntity;
import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.entity.StockEntity;
import com.stu.quantitative.entity.TradedEntity;
import com.stu.quantitative.service.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class TraderService {
    @Autowired
    private PriceService priceService;

    @Autowired
    private TradedService tradedService;

    @Autowired
    private StockService stockService;

    @Autowired
    private BalanceService balanceService;


    public void backTrack(int planCode) {
        List<StockEntity> stocks = this.stockService.findAll();
        List<TradedEntity> tradeds = this.tradedService.findAllByOrderByDate().stream().filter(it ->
                stocks.stream().anyMatch(stock -> stock.getId() == it.getStockId())
        ).toList();
        List<BalanceAccount> balances = this.balanceService.findAccountAllByPlanCode(planCode);

        Map<String, List<PriceEntity>> kLines = balances.stream().map(BalanceAccount::getStocks).flatMap(List::stream).distinct().collect(Collectors.toMap(
                StockEntity::getTicker,  // key: 元素本身（it）
                it -> this.priceService.findByTickerOrderByDate(it.getTicker())  // value: 映射结果
        ));
        Policy policy = new Policy(tradeds, balances, kLines);
        policy.execute();
    }

    /**
     * 执行回测交易
     *
     * @param planCode
     */
    public void execute(int planCode) {
        List<StockEntity> stocks = this.stockService.findAll();
        List<TradedEntity> tradeds = this.tradedService.findAllByOrderByDate().stream().filter(it ->
                stocks.stream().anyMatch(stock -> stock.getId() == it.getStockId())
        ).toList();
        List<BalanceEntity> balances = this.balanceService.findAllByPlanCode(planCode);
        StockPool pool = new StockPool(stocks, balances);
        //  pool.getStocks():当前计划下的所有股票，而不是所有股票stocks
        List<StockPolicy> stockPolicies = pool.getStocks().stream().map(StockPolicy::new).toList();

        List<BalancePolicy> balancePolicies = pool.getBalanceStores().stream().map(BalancePolicy::new).toList();
        //TODO 从这里开始，并且记得把代码上传，！！！

        Map<String, List<PriceEntity>> kLines = balances.stream().map(BalanceAccount::getStocks).flatMap(List::stream).distinct().collect(Collectors.toMap(
                StockEntity::getTicker,  // key: 元素本身（it）
                it -> this.priceService.findByTickerOrderByDate(it.getTicker())  // value: 映射结果
        ));


//        Executor executor = new Executor(tradeds, balances, kLines);
//        executor.execute();
    }


}