package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.PriceEntity;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public class TradeDateDto {
    private final LocalDate date;
    private final Map<String, Optional<PriceEntity>> prices;
    private final List<TradeRecordDto> trades = new ArrayList<>();
    private final List<ShareRecordDto> shares = new ArrayList<>();

    private double cash,amount;

    public TradeDateDto(LocalDate date, Map<String, Optional<PriceEntity>> prices) {
        this.date = date;
        this.prices = prices;
    }

    public void addTrade(TradeRecordDto tradeRecordDto) {
        this.trades.add(tradeRecordDto);
    }

    public void addShare(ShareRecordDto shareRecordDto) {
        this.shares.add(shareRecordDto);
    }

    public void printAll() {
        System.out.printf("交易日期：%tF%n", this.date);
        this.trades.forEach(TradeRecordDto::printAll);
        this.shares.forEach(ShareRecordDto::printAll);
        System.out.println(String.format("持仓市值总和：%.3f，现金流：%.3f：总市值：%.3f", this.amount - this.cash, this.cash, this.amount));
    }
}
