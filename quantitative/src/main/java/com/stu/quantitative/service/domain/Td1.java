package com.stu.quantitative.service.domain;


/**
 *
 */
public record Td1(
        String investment, int direction, double price, double quantity, double hv, double callShare,
        double putShare, double callTrend, double putTrend, double callRate, double putRate) {
    public void printAll() {
        System.out.printf("%s::交易方向：%s，价格：%.3f，数量：%.3f，波动：%.3f，买入持仓：%.3f，卖出持仓：%.3f，买入趋势：%.3f，卖出趋势：%.3f，买入跌幅：%.3f，卖出涨幅：%.3f%n",
                this.investment, this.direction > 0 ? "买入" : "卖出", this.price, this.quantity, this.hv, this.callShare, this.putShare, this.callTrend, this.putTrend, this.callRate, this.putRate);
    }
}
