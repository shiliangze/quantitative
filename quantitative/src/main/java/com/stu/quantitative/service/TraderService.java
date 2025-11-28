package com.stu.quantitative.service;

import com.stu.quantitative.entity.*;
import com.stu.quantitative.service.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


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
        List<CodeConfigEntity> investNameCodes = this.codeConfigService.findAllBySku("investName");
        List<StockEntity> stocks = this.stockService.findAll();
        List<TradedEntity> tradeds = this.tradedService.findAllByOrderByDate().stream().filter(it ->
                stocks.stream().anyMatch(stock -> stock.getId() == it.getStockId())
        ).toList();
        List<BalanceEntity> balances = this.balanceService.findAllByPlanCode(planCode);

        StockPool pool = new StockPool(stocks, balances);
        //  pool.getStocks():当前计划下的所有股票，而不是所有股票stocks
        List<StockPolicy> stockPolicies = pool.getStocks().stream().map(StockPolicy::new).toList();
        //  生成balancePolicy数组
        List<BalancePolicy> balancePolicies = pool.getBalanceStores().stream().map(it -> {
                    int investCode = it.getInvestCode();
                    // 找到对应investCode的所有股票id
                    List<Integer> stockIds = balances.stream().filter(balance -> balance.getInvestCode() == investCode).map(BalanceEntity::getStockId).toList();
                    // 找到对应investCode的所有股票id
//TODO 从这里开始，并且记得把代码上传，！！！
                    List<StockPolicy> stockPoliciesForBalance = stockPolicies.stream().filter(stock -> stockIds.contains(stock.getStock().getId())).toList();
                    Optional<BalanceAccount> balanceAccount = balances.stream().filter(balance -> balance.getStocks().stream().anyMatch(stock -> stock.getId() == stockId)).findFirst();
                    return createNewInstance(it, investNameCodes, stockPolicies);
                }
        ).toList();


        Map<String, List<PriceEntity>> kLines = balances.stream().map(BalanceAccount::getStocks).flatMap(List::stream).distinct().collect(Collectors.toMap(
                StockEntity::getTicker,  // key: 元素本身（it）
                it -> this.priceService.findByTickerOrderByDate(it.getTicker())  // value: 映射结果
        ));


//        Executor executor = new Executor(tradeds, balances, kLines);
//        executor.execute();
    }




}