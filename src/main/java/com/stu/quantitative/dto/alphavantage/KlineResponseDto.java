package com.stu.quantitative.dto.alphavantage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stu.quantitative.entity.PriceEntity;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

// TODO 能否改成record类型

@Data
public class KlineResponseDto {
    @JsonProperty("Meta Data")
    private Object metaData;
    @JsonProperty("Time Series (Daily)")
    private Map<LocalDate, PriceEntity> kLineData;
}
