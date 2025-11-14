package com.stu.quantitative.dto.tsanghi;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ResponseDataVo {
    private String ticker; // 股票代码
    private LocalDate date; //
    private double open;
    private double close;
    private double high;
    private double low;
    private long volume;
}
