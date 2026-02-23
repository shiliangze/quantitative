package com.stu.quantitative.service;

import com.stu.quantitative.entity.*;
import com.stu.quantitative.jpa.BalanceRepository;
import com.stu.quantitative.service.dto.balance.BalanceResponseData;
import com.stu.quantitative.service.dto.balance.BalanceResponseDto;
import com.stu.quantitative.service.dto.balance.StockResponseData;
import com.stu.quantitative.service.dto.balance.SummaryResponseData;
import com.stu.quantitative.service.dto.recommend.IRecommendedResponseDto;
import com.stu.quantitative.service.dto.recommend.RecommendedRequestDto;
import com.stu.quantitative.service.dto.recommend.RecommendedResponseDto;
import com.stu.quantitative.service.dto.recommend.ReplacementResponseDto;
import com.stu.quantitative.service.dto.traceBack.TraceBackExecutor;
import com.stu.quantitative.service.dto.traceBack.TraceBackRequestDto;
import com.stu.quantitative.service.dto.traceBack.TraceBackResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

// 数据展示服务

@Service
public class DisplayService {
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private TradedService tradedService;
    @Autowired
    private PriceService priceService;
    @Autowired
    private StockService stockService;
    @Autowired
    private MarketService marketService;

    // 获取balance对象
    public BalanceResponseDto findAllBalance() {
        List<BalanceEntity> balances = this.balanceRepository.findAll();
        List<StockResponseData> stocks = this.findAllStockDtos();
        Map<Integer, Double> amounts = balances.stream()
                .collect(Collectors.groupingBy(
                        BalanceEntity::getMarketId,
                        Collectors.summingDouble(balance ->
                                stocks.stream()
                                        .filter(stock -> stock.balanceId() == balance.getId())
                                        .mapToDouble(StockResponseData::amount)
                                        .sum()
                        )
                ));

        List<BalanceResponseData> balanceDatas = this.balanceRepository.findAll().stream().map(it -> {
            List<StockResponseData> balanceStocks = stocks.stream().filter(k -> k.balanceId() == it.getId()).toList();
            double balanceAmount = balanceStocks.stream()
                    .map(StockResponseData::amount)
                    .reduce(0.0, Double::sum);
            return new BalanceResponseData(
                    it.getId(),
                    it.getMarketId(),
                    it.getName(),
                    it.getNote(),
                    it.getShare(),
                    balanceAmount,
                    balanceAmount / amounts.get(it.getMarketId()),
                    balanceStocks.stream()
                            .map(StockResponseData::lastDate)
                            .max(LocalDate::compareTo)
                            .orElse(null),
                    balanceStocks
            );
        }).filter(it -> it.amount() > 0.5).toList();

        List<TradedEntity> tradeds = this.tradedService.findAll();

        List<SummaryResponseData> summaries = this.marketService.findAll().stream().map(market -> {
            List<Integer> stockIdOfMarket = balanceDatas.stream()
                    .filter(it -> it.marketId() == market.getId())
                    .flatMap(it->it.stocks().stream())
                    .map(StockResponseData::stockId).toList();
            List<TradedEntity> tradedOfMarket = tradeds.stream().filter(it -> stockIdOfMarket.contains(it.getStockId())).toList();
            double summaryCash = market.getCash() - tradedOfMarket.stream().mapToDouble(it->it.getDirection() * it.getPrice() * it.getQuantity()).sum();
            double summaryAsset = balanceDatas.stream()
                    .filter(it->it.marketId() == market.getId())
                    .mapToDouble(BalanceResponseData::amount).sum();
            return new SummaryResponseData(
                    market.getId(),
                    market.getCash(),
                    summaryAsset,
                    summaryCash,
                    summaryAsset+summaryCash,
                    summaryAsset/(summaryAsset+summaryCash),
                    summaryAsset+summaryCash - market.getCash()
            );
        }).toList();

        return new BalanceResponseDto(balanceDatas,summaries);
    }

    // 为每个balanceDto添加股票信息
    private List<StockResponseData> findAllStockDtos() {
        Map<Integer, Double> residuals = this.tradedService.groupToResidual();
        List<PriceEntity> latestPrices = this.priceService.findLatestPricesForAllStocks();
        return this.stockService.findAll().stream().map(it -> {
            double quantity = residuals.getOrDefault(it.getId(), 0.0);
            PriceEntity price = latestPrices.stream().filter(k -> k.getStockId() == it.getId()).findFirst().get();
            return new StockResponseData(
                    it.getId(),
                    it.getBalanceId(),
                    it.getTicker(),
                    it.getName(),
                    it.getIpo(),
                    price.getClose(),
                    quantity,
                    price.getDate()
            );
        }).toList();
    }

    public List<IRecommendedResponseDto> threshold(RecommendedRequestDto balanceRequest) {
        BalanceEntity balance = this.balanceRepository.findById(balanceRequest.balanceId()).get();
        MarketEntity market = this.marketService.findById(balance.getMarketId());
        List<StockEntity> stocks = this.stockService.findAll().stream().filter(stock ->
                        // 1. 筛选出该balance下的股票
                        // 2. 筛选出优先级>=0的股票
                        stock.getBalanceId() == balance.getId() && stock.getPriority() >= 0)// 按priority从大到小排序
                .toList();
        boolean hasStandardAndReplacements =
                stocks.stream().anyMatch(it -> it.getPriority() == 127) &&
                        stocks.stream().anyMatch(it -> it.getPriority() > 0 && it.getPriority() < 127);

        List<IRecommendedResponseDto> result = new ArrayList<>();
        if (hasStandardAndReplacements) {
            StockEntity standard = stocks.stream().filter(it -> it.getPriority() == 127).findFirst().get();
            RecommendedResponseDto standResult = this.buildThreshold(balance, standard, balanceRequest);
            result.add(standResult);
            List<StockEntity> replacements = stocks.stream().filter(it -> it.getPriority() < 127 && it.getPriority() > 0).toList();
            result.addAll(replacements.stream().map(it -> this.buildReplacementThreshold(it, standard, standResult)).toList());
            List<StockEntity> others = stocks.stream().filter(it -> it.getPriority() == 0).toList();
            result.addAll(others.stream().map(k -> this.buildThreshold(balance, k, balanceRequest)).toList());
        } else {
            result.addAll(stocks.stream().map(k -> this.buildThreshold(balance, k, balanceRequest)).toList());
        }
        return result;
    }

    private RecommendedResponseDto buildThreshold(BalanceEntity balanceEntity, StockEntity stockEntity, RecommendedRequestDto balanceRequest) {
        List<PriceEntity> prices = this.priceService.findByStockIdOrderByDateDesc(stockEntity.getId(), 60).reversed();
        RecommendedResponseDto recommendedResponseDto = new RecommendedResponseDto(stockEntity,balanceRequest, balanceEntity.getShare(), prices);
        this.tradedService.findAllByStockIdOrderByDate(stockEntity.getId()).forEach(recommendedResponseDto::next);
        return recommendedResponseDto.build();
    }

    private ReplacementResponseDto buildReplacementThreshold(StockEntity stockEntity, StockEntity standard, RecommendedResponseDto standardResult) {
        List<PriceEntity> prices = this.priceService.findByStockIdOrderByDateDesc(stockEntity.getId(), 60).reversed();
        List<PriceEntity> standards = this.priceService.findByStockIdOrderByDateDesc(standard.getId(), 60).reversed();
        return new ReplacementResponseDto(stockEntity, prices, standards, standardResult).build();
    }

    public TraceBackResponseDto traceBack(TraceBackRequestDto traceBackRequestDto) {
        // 1. 获取当前股票
        StockEntity stock = this.stockService.findById(traceBackRequestDto.stockId());
        // 2. 获取当前股票的历史价格
        List<PriceEntity> prices = this.priceService.findByStockIdOrderByDate(stock.getId(), traceBackRequestDto.startDate());
        BalanceEntity balanceEntity = this.balanceRepository.findById(stock.getBalanceId()).get();
        MarketEntity market = this.marketService.findById(balanceEntity.getMarketId());
        TraceBackExecutor traceBackExecutor = new TraceBackExecutor(traceBackRequestDto, balanceEntity, market);
        prices.forEach(traceBackExecutor::next);
        return new TraceBackResponseDto(stock.getId(), stock.getName(), traceBackExecutor.build());
    }
}
