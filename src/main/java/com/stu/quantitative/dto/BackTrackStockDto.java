package com.stu.quantitative.dto;

import java.time.LocalDate;

public record BackTrackStockDto(
        LocalDate date,
        int stockId,
        String stockName, // 股票代码
        int balanceId,
        double amount, // 当前股票市值
        double close, // 收盘价格
        double quantity, // 仓位数量
        double hv, // 历史波动率
        double asymptote, // 渐进线x轴：买入次数
        double putTrend, // 趋势卖出因子：趋势叠加 * 1.618 趋势相反开平方根√
        double callTrend, // 趋势买入因子
        double putRate,// 卖出指导比例：1 + (hv + putShare + putTrend) / 100;
        double callRate, // 买入指导比例：1 - (hv + callShare + callTrend) / 100;
        double put,// 卖出价：趋势叠加 * 1.618 趋势相反开平方根√
        double call, // 买入价
        double profit, // 持仓盈亏
        double profitRate,
        int direction, // 买入/卖出
        double tradePrice, // 交易价格
        double tradeQuantity  // 交易数量)// 持仓年化收益率
) {  // 初始资金
}
