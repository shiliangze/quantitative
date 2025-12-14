package com.stu.quantitative.service.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StockRecordDto {
    private final String stockName; // 股票代码
    private final int direction; // 交易方向，1：买，-1：卖
    private final double amount;    // 当前股票市值
    private final double close; // 收盘价格
    private final double quantity; // 仓位数量
    private final double hv; // 历史波动率
    private final int asymptote; // 渐进线x轴：买入次数
    private final double asymptoteRate; // 渐进线y轴，(2 / π * arctan(asymptote)) ^ 2)
    private final double putTrend; // 趋势卖出因子：趋势叠加 * 1.618 趋势相反开平方根√
    private final double callTrend; // 趋势买入因子
//    private final double putShare; // 卖出仓位因子
//    private final double callShare; // 买入仓位因子
    private final double putRate; // 卖出指导比例：1 + (hv + putShare + putTrend) / 100;
    private final double callRate; // 买入指导比例：1 - (hv + callShare + callTrend) / 100;
    private final double put; // 卖出价：趋势叠加 * 1.618 趋势相反开平方根√
    private final double call; // 买入价


    public String report() {
        StringBuffer output = new StringBuffer();
        output.append(
                String.format("%s：%s，收盘价：%.3f，持仓量：%.3f，市值：%.3f\n",
                        this.stockName,
                        TradeDirection.fromCode(this.direction).getDescription(),
                        this.close, this.quantity, this.amount)
        );
        output.append(
                String.format("历史波动率：%.3f，渐进线：%d，渐进线y轴：%.3f，趋势卖出因子：%.3f，趋势买入因子：%.3f\n",
                        this.hv,
                        this.asymptote, this.asymptoteRate,
                        this.putTrend, this.callTrend)
        );
        output.append(
                String.format("卖出指导比例：%.3f，买入指导比例：%.3f，卖出价：%.3f，买入价：%.3f\n",
                        this.putRate, this.callRate, this.put, this.call)
        );
        return output.toString();
    }
}
