package com.stu.quantitative.service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 单只股票交易记录
 */
@AllArgsConstructor
@Getter
public class TradeRecordDto {
    private final int stockCode; // 股票ID
    private final int direction; // 买入/卖出
    private final double price; // 交易价格
    private final double quantity;  // 交易数量

    public String report(){
        return String.format("股票ID：%d，%s，数量：%s，价格：%s\n",
                this.stockCode,TradeDirection.fromCode(this.direction).getDescription(),this.quantity,this.price);
    }
}
