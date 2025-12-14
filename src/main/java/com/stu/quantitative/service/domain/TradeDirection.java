package com.stu.quantitative.service.domain;

import java.util.Arrays;

public enum TradeDirection {
    BUY(1, "买入"),
    NO_TRADE(0, "无交易"),
    SELL(-1, "卖出"),
    PAYOUT(2, "派息");


    private final int code;
    private final String description;

    TradeDirection(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    // 根据code获取枚举
    public static TradeDirection fromCode(int code) {
        return Arrays.stream(TradeDirection.values()).filter(it -> it.code == code).findFirst().orElse(NO_TRADE);
    }
}