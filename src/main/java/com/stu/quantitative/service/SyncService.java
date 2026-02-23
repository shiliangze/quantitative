package com.stu.quantitative.service;

import com.stu.quantitative.dto.alphavantage.KlineResponseDto;
import com.stu.quantitative.entity.CodeConfigEntity;
import com.stu.quantitative.entity.ExchangeEntity;
import com.stu.quantitative.entity.StockEntity;
import com.stu.quantitative.service.domain.Alphavantage;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    private List<CodeConfigEntity> tokens;

    @PostConstruct
    public void init() {
        this.tokens = this.codeConfigService.findAllBySku("token");
    }

    public List<StockEntity> sync(int balanceId){
        List<StockEntity> allStocks = this.stockService.findByBalanceId(balanceId);
        String token = this.tokens.stream().filter(it -> it.getCode() == 0).findFirst().get().getValue();
        allStocks.forEach(it->{
            KlineResponseDto kLines = this.requestKLines(it,token);
            // k线信息入库
            this.priceService.kLineSync(kLines.getKLineData(), it);
        });
        return allStocks;
    }

    @Transactional
    @SneakyThrows
    private KlineResponseDto requestKLines(StockEntity stock, String token) {
        // 获取交易所名称并拼接ticker
        ExchangeEntity exchange = this.exchangeService.findByCodeAndSource(stock.getExchange(), 0).orElse(null);
        String ticker = null == exchange ? stock.getTicker() : String.format("%s.%s", stock.getTicker(), exchange.getValue());
        KlineResponseDto klines = new Alphavantage(token, ticker).request( );
        Thread.sleep(10000L);
        return klines;
    }
}