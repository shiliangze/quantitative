package com.stu.quantitative.service.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TradeReportDto {
    private final LocalDate startDate, endDate, middleDate;
    // 历史交易回测交易
    private final List<TradeDateDto> endGame = new ArrayList<>(), backTrade = new ArrayList<>();

    public TradeReportDto(LocalDate startDate, LocalDate middleDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.middleDate = middleDate;
    }

    public void add(TradeDateDto record) {
        if (record.getDate().isAfter(this.middleDate)) {
            backTrade.add(record);
        } else {
            endGame.add(record);
        }
    }

    public void printAll() {
        System.out.printf("历史交易：起始日期：%tF，结束日期：%tF%n", startDate, middleDate);
        endGame.forEach(TradeDateDto::printAll);
        System.out.printf("回测交易：：起始日期：%tF，结束日期：%tF%n", middleDate.plusDays(1), endDate);
        backTrade.forEach(TradeDateDto::printAll);
    }
}
