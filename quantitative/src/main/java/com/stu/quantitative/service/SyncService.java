package com.stu.quantitative.service;

import com.stu.quantitative.dto.alphavantage.DailyPriceResponseDto;
import com.stu.quantitative.entity.CodeConfigEntity;
import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.entity.StockEntity;
import com.stu.quantitative.service.domain.Alphavantage;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
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

    private int cursor = -1;

    // 配置WebClient以支持处理大型响应数据
    private final WebClient webClient;

    private List<CodeConfigEntity> tokens;


    public SyncService() {
        // 创建一个支持更大缓冲区的ExchangeStrategies
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                        // 设置10MB的缓冲区大小，解决大数据量响应问题
                        .maxInMemorySize(10 * 1024 * 1024))
                .build();

        // 使用配置好的ExchangeStrategies初始化WebClient
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl("https://www.alphavantage.co/query")
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

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
            // 超过100天，全量同步
            DailyPriceResponseDto dailyPriceResponseDto = new Alphavantage(token).request(stock);
            // k线信息入库
            this.priceService.kLineSync(dailyPriceResponseDto, stock);
        } else {
            String token = this.tokens.stream().filter(it -> it.getCode()==0).findFirst().get().getValue();
            // 超过100天，全量同步
            DailyPriceResponseDto dailyPriceResponseDto = new Alphavantage(token).request(stock);
            // k线信息入库
            this.priceService.kLineSync(dailyPriceResponseDto, stock);
        }
//
//        // WebClient请求k线信息
//        DailyPriceResponseDto dailyPriceResponseDto = this.webClient
//                .get() // GET 请求
//                .uri(uriBuilder -> uriBuilder
//                        .queryParam("function", "TIME_SERIES_DAILY")
//                        .queryParam("symbol", stock.getTicker())
//                        .queryParam("outputsize", "full")
//                        .queryParam("apikey", "2PA20MY1GHSPZJVP")
//                        .build())
//                .retrieve()
//                .onStatus(httpStatus -> !httpStatus.is2xxSuccessful(),
//                        clientResponse -> {
//                            log.error("API请求失败，状态码: {}", clientResponse.statusCode().value());
//                            return clientResponse.bodyToMono(String.class)
//                                    .flatMap(errorBody -> Mono.error(
//                                            new RuntimeException("API请求失败: " + errorBody)));
//                        })
//                .bodyToMono(DailyPriceResponseDto.class)
//                .block();
//// k线信息入库
//        this.priceService.kLineSync(dailyPriceResponseDto, stock);
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


    private void applyAlphaVantage() {

    }
}