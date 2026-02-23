package com.stu.quantitative.service;

import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.entity.StockEntity;
import com.stu.quantitative.jpa.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PriceService {
    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private StockService stockService;


    public Optional<PriceEntity> findTopByOrderByIdDesc() {
        return priceRepository.findTopByOrderByIdDesc();
    }

    public Optional<PriceEntity> findTopByStockIdOrderByDate(int stockId) {
        return priceRepository.findTopByStockIdOrderByDateDesc(stockId);
    }

    public Boolean existsByStockId(int stockId) {
        return this.priceRepository.existsByStockId(stockId);
    }

    public List<PriceEntity> findByStockIdOrderByDate(int stockId) {
        return this.priceRepository.findByStockIdOrderByDate(stockId);
    }
    public List<PriceEntity> findByStockIdOrderByDate(int stockId, LocalDate startDate) {
        return this.priceRepository.findByStockIdOrderByDate(stockId).stream().filter(it -> it.getDate().isAfter(startDate)).toList();
    }

    public List<PriceEntity> findByTickerOrderByDate(String ticker) {
        StockEntity stock = this.stockService.findAll().stream().filter(it -> it.getTicker().equals(ticker)).findFirst().get();
        return this.priceRepository.findByStockIdOrderByDate(stock.getId());
    }

    // 获取当前所有股票的最新价格
    public List<PriceEntity> findLatestPricesForAllStocks() {
        return this.priceRepository.findLatestPricesForAllStocks();
    }

    // k线信息入库
    @Transactional
    public void kLineSync(Map<LocalDate, PriceEntity> klines, StockEntity stock) {
        // 查找该股票的最后一天的记录
        LocalDate lastKLineDate = this.findTopByStockIdOrderByDate(stock.getId()).map(PriceEntity::getDate).orElse(LocalDate.MIN);
        klines.entrySet().stream()
                .filter(entry -> entry.getKey().isAfter(lastKLineDate))
                .forEach(entry -> {
                    entry.getValue().setDate(entry.getKey());
                    entry.getValue().setStockId(stock.getId());
                    this.priceRepository.save(entry.getValue());
                });
        this.priceRepository.flush();
    }

    public List<PriceEntity> findByStockIdOrderByDateDesc(int stockId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return this.priceRepository.findByStockIdOrderByDateDesc(stockId, pageable);
    }
}