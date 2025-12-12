package com.stu.quantitative.service.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TradeReportDto {
    private final List<TradeTableDto> tables = new ArrayList<>();

    private TradeTableDto currentTable = null;

    public void exchange(LocalDate date, int investCode, int stockCode, int direction, String price, String quantity) {
        if(null!=currentTable && currentTable.getDate().equals(date)){
            //日期相同，说明当天已有交易记录，只需要添加新的交易即可，不需要初始化
            this.currentTable.exchange(investCode, stockCode, direction, price, quantity);
        }
        else{
            //日期不同，说明当天无交易记录，需要初始化新的交易记录
            this.currentTable = new TradeTableDto(date, investCode, stockCode, direction, price, quantity);
            this.tables.add(this.currentTable);
        }
    }
    // 总仓日志
    public void clearing(LocalDate date,String total,String cash,String amount){
        if(null!=currentTable && currentTable.getDate().equals(date)){
            //  当日有交易记录，则记录相关清算信息
            this.currentTable.pool(amount);
        }
    }
    // balance日志
    public void clearing(LocalDate date,String amount,String share,,,){
        if(null!=currentTable && currentTable.getDate().equals(date)){
            //  当日有交易记录，则记录相关清算信息

        }
        // 无交易，不记录清算信息
    }
}
