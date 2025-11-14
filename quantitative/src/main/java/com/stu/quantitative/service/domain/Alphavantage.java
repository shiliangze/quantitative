package com.stu.quantitative.service.domain;

import com.stu.quantitative.dto.alphavantage.DailyPriceResponseDto;
import com.stu.quantitative.entity.StockEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class Alphavantage {
    // 配置WebClient以支持处理大型响应数据
    private final WebClient webClient;

    private final String token;


    public Alphavantage(String token) {
        this.token = token;
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

    public DailyPriceResponseDto request(StockEntity stock) {
        return this.webClient
                .get() // GET 请求
                .uri(uriBuilder -> uriBuilder
                        .queryParam("function", "TIME_SERIES_DAILY")
                        .queryParam("symbol", stock.getTicker())
                        .queryParam("outputsize", "full")
                        .queryParam("apikey", token)
                        .build())
                .retrieve()
                .onStatus(httpStatus -> !httpStatus.is2xxSuccessful(),
                        clientResponse -> {
                            log.error("API请求失败，状态码: {}", clientResponse.statusCode().value());
                            return clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(
                                            new RuntimeException("API请求失败: " + errorBody)));
                        })
                .bodyToMono(DailyPriceResponseDto.class)
                .block();
    }
}
