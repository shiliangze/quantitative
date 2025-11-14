package com.stu.quantitative.dto.alphavantage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyPriceResponseDto {
    @JsonProperty("Time Series (Daily)")
    private Map<String, KlineDto> timeSeries = new HashMap<>();

    @Data
    @NoArgsConstructor
    public static class KlineDto {
        @JsonProperty("1. open")
        private double open;

        @JsonProperty("2. high")
        private double high;

        @JsonProperty("3. low")
        private double low;

        @JsonProperty("4. close")
        private double close;

        @JsonProperty("5. volume")
        private long volume;
    }
}