package com.stu.quantitative.service.domain;

public class SettlementRecordDto {
    private final String type;   // 结算日期
    private final double amount;
    private final String total;   // 结算金额
    private final String cash;   // 结算金额


    public SettlementRecordDto(String type, double amount, String total, String cash) {
        this.type = type;
        this.amount = amount;
        this.total = total;
        this.cash = cash;
    }
}
