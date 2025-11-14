package com.stu.quantitative.service;

import com.stu.quantitative.dto.alphavantage.DailyPriceResponseDto;
import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.entity.StockEntity;
import com.stu.quantitative.jpa.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    public List<PriceEntity> findByTickerOrderByDate(String ticker) {
        StockEntity stock = this.stockService.findAll().stream().filter(it -> it.getTicker().equals(ticker)).findFirst().get();
        return this.priceRepository.findByStockIdOrderByDate(stock.getId());
    }


    // k线信息入库
    @Transactional
    public void kLineSync(DailyPriceResponseDto dailyPriceResponseDto, StockEntity stock) {
        dailyPriceResponseDto.getTimeSeries().entrySet().stream()
                .forEach(entry -> {
                    LocalDate tradeDate = LocalDate.parse(entry.getKey(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    //  如果查询到重复数据，不进行入库
                    if (!this.priceRepository.existsByStockIdAndDate(stock.getId(), tradeDate)) {
                        PriceEntity priceEntity = new PriceEntity();
                        priceEntity.setStockId(stock.getId());
                        priceEntity.setDate(tradeDate);
                        priceEntity.setOpen(entry.getValue().getOpen());
                        priceEntity.setClose(entry.getValue().getClose());
                        priceEntity.setHigh(entry.getValue().getHigh());
                        priceEntity.setLow(entry.getValue().getLow());
                        priceEntity.setVolume(entry.getValue().getVolume());
                        this.priceRepository.save(priceEntity);
                    }
                });
        this.priceRepository.flush();
    }

}