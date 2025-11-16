package com.stu.quantitative.service.domain;

import com.stu.quantitative.dto.alphavantage.DailyPriceResponseDto;
import com.stu.quantitative.entity.CodeConfigEntity;
import com.stu.quantitative.entity.StockEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
public class Alltick {
    // 配置WebClient以支持处理大型响应数据
    private final WebClient webClient;

    private final String token;
    private  final String ticker;
//

    public Alltick(String token, String ticker) {
        this.token = token;
        this.ticker = ticker;
//        // 创建一个支持更大缓冲区的ExchangeStrategies
//        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
//                .codecs(configurer -> configurer.defaultCodecs()
//                        // 设置10MB的缓冲区大小，解决大数据量响应问题
//                        .maxInMemorySize(10 * 1024 * 1024))
//                .build();

        // 使用配置好的ExchangeStrategies初始化WebClient
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl("https://quote.alltick.co/quote-stock-b-api/kline")
                .build();
    }

    public String request(StockEntity stock,int quantity) {
        String requestData = String.format("{\"trace\": \"%s\",\"data\": {\"code\": \"%s\",\"kline_type\": 8,\"kline_timestamp_end\": 0,\"query_kline_num\": 10,\"adjust_type\": 0}}",
                UUID.randomUUID(),this.ticker,quantity);
        log.info(requestData);
        log.info(URLEncoder.encode(requestData, StandardCharsets.UTF_8));
        return this.webClient
                .get() // GET 请求
                .uri(uriBuilder -> uriBuilder
                        .queryParam("token", token)
                        .queryParam("query", URLEncoder.encode(requestData, StandardCharsets.UTF_8))
                        .build())
                .retrieve()
                .onStatus(httpStatus -> !httpStatus.is2xxSuccessful(),
                        clientResponse -> {
                            log.error("API请求失败，状态码: {}", clientResponse.statusCode().value());
                            return clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(
                                            new RuntimeException("API请求失败: " + errorBody)));
                        })
                .bodyToMono(String.class)
                .block();
    }
}
