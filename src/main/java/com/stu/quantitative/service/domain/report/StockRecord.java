package com.stu.quantitative.service.domain.report;

import com.stu.quantitative.service.domain.TradeDirection;

import java.time.LocalDate;

public record StockRecord(
        LocalDate date,
        int stockId,
        String stockName, // 股票代码
        int investCode,
        int direction, // 交易方向，1：买，-1：卖
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
        double call // 买入价
) {
    public void report() {
        System.out.printf("%s：%s， 市值：%.2f，收盘价：%.3f，仓位数量：%.2f，历史波动率：%.4f，渐进线x轴：%.3f，趋势卖出因子：%.4f，趋势买入因子：%.4f，卖出指导比例：%.4f，买入指导比例：%.4f，卖出价：%.3f，买入价：%.3f%n",
                 stockName, TradeDirection.fromCode(direction).getDescription(),  amount, close, quantity,
                hv, asymptote, putTrend, callTrend, putRate, callRate, put, call);
    }
}
