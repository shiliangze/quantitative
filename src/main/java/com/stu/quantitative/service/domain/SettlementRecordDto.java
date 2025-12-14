package com.stu.quantitative.service.domain;

import java.util.ArrayList;
import java.util.List;

// 总仓结算记录
public class SettlementRecordDto {
    private final List<BalanceRecordDto> balanceRecords = new ArrayList<>();

    private final double amount;
    // 总仓参数
    private final double total;   // 资产总值：现金+持仓价值
    private final double cash;   // 现金

    public SettlementRecordDto(double cash, double amount) {
        this.amount = amount;
        this.cash = cash;
        this.total = cash+ amount;
    }

    public void clearing(BalanceRecordDto balanceRecordDto) {
        this.balanceRecords.add(balanceRecordDto);
    }

    public String report() {
        StringBuffer output = new StringBuffer();
        this.balanceRecords.forEach(it -> {
            output.append(it.report());
        });
        output.append(
                String.format("总资产：%f，现金：%f，持仓资产：%f\n", this.total, this.cash, this.amount)
        );
        return output.toString();
    }
}
