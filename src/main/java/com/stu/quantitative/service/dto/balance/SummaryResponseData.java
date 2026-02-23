package com.stu.quantitative.service.dto.balance;

public record SummaryResponseData(
        int marketId, // 市场ID
        double seed, // 初始资金
        double asset, // 股票市值
        double cash, // 现金
        double total, // 总资产
        double position, // 持仓率
        double profit // 浮动盈亏
) {
}
