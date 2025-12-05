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
        List<TradedEntity> tradeds = this.tradedService.findAllByOrderByDate();
        // TODO 因为直接赋值到stockpolicy，所以无需再这一步过滤
                //.stream().filter(it -> stocks.stream().anyMatch(stock -> stock.getId() == it.getStockId())).toList();
        List<BalanceEntity> balances = this.balanceService.findAllByPlanCode(planCode);

        StockPool pool = new StockPool(stocks, balances);
        //  生成balancePolicy数组
        List<BalancePolicy> balancePolicies = pool.getBalanceStores().stream().map(it -> {
                    int investCode = it.getInvestCode();
                    // 找到对应investCode的所有股票id
                    List<Integer> stockIds = balances.stream().filter(balance -> balance.getInvestCode() == investCode).map(BalanceEntity::getStockId).toList();
                    // 找到对应investCode的所有股票id
                    List<StockPolicy> stockPoliciesForBalance = pool.getStockEntities().stream()
                            .filter(stock -> stockIds.contains(stock.getId()))
                            .map(stock -> {
                                List<TradedEntity> traders = this.tradedService.findAllByStockIdOrderByDate(stock.getId());
                                List<PriceEntity> klines = this.priceService.findByTickerOrderByDate(stock.getTicker());
                                return new StockPolicy(stock, it, klines,traders);
                            }).toList();
                    String investName = investNameCodes.stream().filter(code -> code.getCode() == investCode).map(CodeConfigEntity::getValue).findFirst().get();
                    double shareRate = balances.stream().filter(balance -> balance.getInvestCode() == investCode).findFirst().get().getShare();
                    // 调试通过后，全部放到构造器里面
                    return new BalancePolicy(
                            investName,
                            shareRate,
                            stockPoliciesForBalance
                    );
                }
        ).toList();

        //
//        Map<String, List<PriceEntity>> kLines = balancePolicies.stream().map(BalancePolicy::getStocks).flatMap(List::stream).distinct().collect(Collectors.toMap(
//                it -> it.getStock().getTicker(),  // key: 元素本身（it）
//                it -> this.priceService.findByTickerOrderByDate(it.getStock().getTicker())  // value: 映射结果
//        ));


        Executor executor = new Executor(tradeds, balancePolicies);
        executor.execute();
    }


}