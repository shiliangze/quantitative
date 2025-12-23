package com.stu.quantitative.service.domain;

import java.time.LocalDate;


public record BalanceRecord(
        LocalDate date,
        int investCode,  // 投资组合代码
        String investName, // 投资组合名称
        double amount, // 平衡仓资产值
        double share, // 平衡仓预期份额：默认0.5
        double position, // 当前实际份额 total / amount
        double shareOffset, // 偏移量：(1.0001-position) / (share + 0.0001)
        double shareCoefficient // 预期份额算子：Math.log10(this.shareOffset +9)
) {
    public void report() {
        System.out.printf("%s：市值：%.3f，预期份额：%.3f，当前份额：%.3f，偏移量：%.3f，预期份额算子：%.3f%n",
                investName, amount, share, position, shareOffset, shareCoefficient);
    }
}
