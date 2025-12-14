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

    public TradeTableDto(LocalDate date, int investCode, int stockCode, int direction, double price, double quantity) {
        this.date = date;
        this.exchange(investCode, stockCode, direction, price, quantity);
    }

    public void exchange(int investCode, int stockCode, int direction, double price, double quantity) {
        this.tradeRecords.add(new TradeRecordDto( stockCode, direction, price, quantity));
    }

    public void  clearing(SettlementRecordDto settlementRecordDto){
        this.settlementRecordDtos.add(settlementRecordDto);
    }

    public void  report(){
        StringBuffer output = new StringBuffer();
        output.append(
                String.format("==================交易日期：%s=======================\n", this.date)
        );
        output.append(
                String.format("交易详情\n")
        );
        this.tradeRecords.forEach(it -> {
            output.append(it.report());
        });
        output.append(
                String.format("清算详情\n")
        );
        this.settlementRecordDtos.forEach(it -> {
            output.append(it.report());
        });
        System.out.println(output.toString());
    }

}
