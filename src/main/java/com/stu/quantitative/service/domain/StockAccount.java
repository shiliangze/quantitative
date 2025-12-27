package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.entity.StockEntity;
import com.stu.quantitative.service.domain.report.TradeReportDto;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 单一股票信息存储
 * 生命周期：全生命周期
 */
@Slf4j
public class StockAccount {
    // 波动率线
    private final TechnicalAnalysis technicalAnalysis = new TechnicalAnalysis();
    @Getter
    private final StockEntity stockEntity;
    @Getter
    private final StockPool pool;
    // 交易记录
    private final TradeReportDto tradeReportDto;
    // 持仓数量
    @Getter
    private double quantity = 0.0;
    // 持仓总市值，平均持仓市值
//    @Getter
    private double amount = 0.0;
    // 现金转入
    private double income = Double.MIN_VALUE, avIncom = Double.MIN_VALUE, profit = 0.0, profitRate = 0.0;
    @Setter
    private PriceEntity currentKLine; // 当前交易日的k线

    private double price, close = 500000.00;// 交易价格,收盘价格
    private double hv = 1.0; // 当天的历史波动率
    // 渐进率：该参数的目的是为了控制买入仓位，防止初次买入过多
    // 第一次购买的时候，渐进率为0.45，后续每次购买分享率递增，无穷大时逼近渐近线1
    @Getter
    private double asymptote = 0.5;
    // 趋势交易因子
    // 连续单向交易说明形成趋势概率增加，需要拉开连续交易的比例
    private double putTrend = 1.618, callTrend = 1.618;
    private double putRate = -1.00, callRate = -1.00;
    @Getter
    private double call = 500000.00, put = Double.MIN_VALUE;

    @Getter
    private int direction = 0;

    public StockAccount(StockEntity stockEntity, StockPool pool) {
        this.stockEntity = stockEntity;
        this.pool = pool;

        this.tradeReportDto = pool.getTradeReportDto();
        this.price = pool.getCash();
    }

    public boolean tradeable() {
        return null != this.currentKLine;
    }

    @Setter
    private LocalDate ipo; // ipo日期
    // 当前日期,最近一次有交易的日期
    private LocalDate today, exchangeDate;

    // 执行交易
    //direction：交易方向，1：买，-1：卖，price：交易价格，quantity：交易数量
    public void exchange(LocalDate date, int direction, double price, double quantity) {
        this.direction = direction;
        this.price = price;
        // 1.从资金池中扣除买入金额
        this.pool.exchange(date, this.stockEntity.getId(), this.stockEntity.getName(), direction, price, quantity);
        // 2.增加响应持仓数量
        this.quantity += direction * quantity;
        this.income += direction * price * quantity;
        // 3. 更新渐进率
        if (1 == direction) {
            this.asymptote++;
        }
    }

    // 更新收盘价，并计算市值
    public double update(LocalDate today) {
        this.today = today;
        if (null != this.currentKLine) {
            this.close = this.currentKLine.getClose();
        }
        // 2. 市值必须放在update中，因为清盘之前，balance会计算仓位信息
        this.amount = this.quantity * this.close;
        // 设置
        long delta = ChronoUnit.DAYS.between(this.ipo, today);
        this.profit = this.amount - this.income;
        this.avIncom = (delta * this.avIncom + this.income) / (delta + 1);
        //  计算年化收益率
        this.profitRate = Math.pow(1 + this.profit / this.avIncom, 365.0 / delta) - 1;
        return this.amount;
    }

    public boolean sellable() {
        return this.amount >= this.pool.getMinAmount();
    }

    public void clearing(LocalDate date, double shareCoefficient, int direction) {
        //  非交易日不做清算
        if (!this.tradeable()) {
            return;
        }
        //  如果balance发生了交易，并且当前股票未发生交易
        //  则balance更新所有股票的当前价，重新计算目标价格
        if (0 != direction && 0 == this.direction) {
            this.price = this.close;
        }
        // 1. 计算当前股票的历史波动率
        this.technicalAnalysis.add(this.close);
        this.hv = this.technicalAnalysis.hvol(250);
        // 4. 计算趋势因子
        this.trend();
        // 5. 生成阈值
        this.threshold(shareCoefficient);
        // 股票清盘任务
        if (0 == this.stockEntity.getPriority()) {
            this.tradeReportDto.clearing(
                    date, stockEntity.getId(), stockEntity.getName(), this.stockEntity.getBalanceId(),
                    this.direction, this.amount, this.close, this.quantity,
                    this.hv, this.asymptote, this.putTrend, this.callTrend,
                    this.putRate, this.callRate, this.put, this.call,
                    this.profit, this.profitRate);
        }
        // 最后一步，清理数据
        this.direction = 0; //交易方向清零
    }

    // 计算趋势因子
    private void trend() {
//        this.direction = 0 或 2 全部跳过
        if (1 == this.direction) {
            // tradeFlag = 1 买入交易
            // 买入步长增加卖出步长开平方
            this.callTrend *= 1.618;
            this.putTrend = Math.sqrt(this.putTrend);
        } else if (-1 == this.direction) {
            this.putTrend *= 1.618;
            this.callTrend = Math.sqrt(this.callTrend);
        }
    }

    // 计算阈值
    private void threshold(double shareCoefficient) {
        // 4. 计算买入阈值
        this.callRate = shareCoefficient - (this.hv + this.callTrend / 100) / 2;
        this.putRate = shareCoefficient + (this.hv + this.putTrend / 100) / 2;
        this.put = this.price * putRate;
        this.call = this.price * callRate;
    }
}
