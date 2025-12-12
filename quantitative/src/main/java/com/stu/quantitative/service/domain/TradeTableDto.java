package com.stu.quantitative.service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 某一天的股票交易集合
 */
@Getter
public class TradeTableDto {
    private final LocalDate date;   // 交易日期
    private final List<TradeRecordDto> tradeRecords = new ArrayList<>(); // 交易记录

    private final List<SettlementRecordDto> settlementRecordDtos = new ArrayList<>(); // 结算信息

    public TradeTableDto(LocalDate date, int investCode, int stockCode, int direction, String price, String quantity) {
        this.date = date;
        this.exchange(investCode, stockCode, direction, price, quantity);
    }

    public void exchange(int investCode, int stockCode, int direction, String price, String quantity) {
        this.tradeRecords.add(new TradeRecordDto(investCode, stockCode, direction, price, quantity));
    }

    public void  pool(String total,String cash,String amount){
        this.settlementRecordDtos.add(new SettlementRecordDto(
                "pool", total, cash, amount));
    }
    public void  balance(double amount){
        this.settlementRecordDtos.add(new SettlementRecordDto(date, amount));
    }    public void  clearing(double amount){
        this.settlementRecordDtos.add(new SettlementRecordDto(date, amount));
    }
    public void  stock(double amount){
        this.settlementRecordDtos.add(new SettlementRecordDto(date, amount));
    }

}
