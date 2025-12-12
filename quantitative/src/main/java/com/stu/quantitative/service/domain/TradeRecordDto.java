package com.stu.quantitative.service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 单只股票交易记录
 */
@AllArgsConstructor
@Getter
public class TradeRecordDto {
    private final int investCode; //  balance 标志
    private final int stockCode; // 股票ID
    private final int direction; // 买入/卖出
    private final String price; // 交易价格
    private final String quantity;  // 交易数量
}
