package com.stu.quantitative.service;

import com.stu.quantitative.dto.alphavantage.DailyPriceResponseDto;
import com.stu.quantitative.entity.CodeConfigEntity;
import com.stu.quantitative.entity.ExchangeEntity;
import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.entity.StockEntity;
import com.stu.quantitative.service.domain.Alphavantage;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SyncService {
    @Autowired
    private PriceService priceService;
    @Autowired
    private StockService stockService;
    @Autowired
    private CodeConfigService codeConfigService;
    @Autowired
    private ExchangeService exchangeService;

    private int cursor = -1;

    private List<CodeConfigEntity> tokens;

    @PostConstruct
    public void init() {
        this.tokens = this.codeConfigService.findAllBySku("token");
    }

    @Scheduled(fixedDelay = 1000 * 60 * 2)
    @Transactional
    @SneakyThrows
    public void sync() {
        StockEntity stock = this.nextStock();
        if (this.priceService.findTopByStockIdOrderByDate(stock.getId()).get().getDate().plusDays(100).isAfter(LocalDate.now())) {
            // 未超过100天，增量同步
            String token = this.tokens.stream().filter(it -> it.getCode()==0).findFirst().get().getValue();
            ExchangeEntity exchange = this.exchangeService.findByCodeAndSource(stock.getExchange(),0).orElse(null);
            String ticker = null == exchange ? stock.getTicker() : String.format("%s.%s", stock.getTicker(), exchange.getValue());
            // 超过100天，全量同步
            DailyPriceResponseDto dailyPriceResponseDto = new Alphavantage(token,ticker).request(stock,"compact");
            log.info(dailyPriceResponseDto.toString());
            // k线信息入库
//            this.priceService.kLineSync(dailyPriceResponseDto, stock);
        } else {
            String token = this.tokens.stream().filter(it -> it.getCode()==0).findFirst().get().getValue();
            ExchangeEntity exchange = this.exchangeService.findByCodeAndSource(stock.getExchange(),0).orElse(null);
            String ticker = null == exchange ? stock.getTicker() : String.format("%s.%s", stock.getTicker(), exchange.getValue());
            // 超过100天，全量同步
            DailyPriceResponseDto dailyPriceResponseDto = new Alphavantage(token,ticker).request(stock,"full");
            // k线信息入库
            this.priceService.kLineSync(dailyPriceResponseDto, stock);
        }
    }

    // 轮询最下一条stock
    private StockEntity nextStock() {
        List<StockEntity> allStocks = this.stockService.findAll();
        if (this.cursor < 0) {
            // 获取stock表的最后一条记录的id
            int stockId = this.priceService.findTopByOrderByIdDesc()
                    .map(PriceEntity::getStockId) // 如果Optional有值，返回priceEntity.getStockId()
                    .orElse(-1); // 如果Optional为空，返回-1
            log.info("获取的stockId: {}", stockId);
            // 用stockId定位allStocks中cursor的位置
            StockEntity entity = allStocks.stream().filter(s -> s.getId() == stockId).findFirst().orElse(null);
            this.cursor = allStocks.indexOf(entity);    //如果不存在或查找失败，返回-1，下一步+1正好=0，从头开始
        }
        this.cursor = (this.cursor + 1) % allStocks.size();
        log.info("计算的cursor值: {}", this.cursor);
        return allStocks.get(this.cursor);
    }
}