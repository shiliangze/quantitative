package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.entity.StockEntity;
import com.stu.quantitative.service.domain.report.TradeReportDto;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

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
    @Getter
    private final int investCode;
    //优先级
    @Getter
    private final int priority;
    // 交易记录
    private final TradeReportDto tradeReportDto;
    // 持仓数量
    @Getter
    private double quantity = 0.0;
    // 持仓总市值
    @Getter
    private double amount = 0.0;
//    @Setter
//    private LocalDate ipo; // ipo日期
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

    public StockAccount(StockEntity stockEntity, StockPool pool, int investCode, int priority, TradeReportDto tradeReportDto) {
        this.stockEntity = stockEntity;
        this.pool = pool;
        this.investCode = investCode;
        // 优先级
        this.priority = priority;
        this.tradeReportDto = tradeReportDto;
        this.price = pool.getCash();
    }

    public boolean tradeable() {
        return null != this.currentKLine;
    }

    // 执行交易
    //direction：交易方向，1：买，-1：卖，price：交易价格，quantity：交易数量
    public void exchange(LocalDate date, int direction, double price, double quantity) {
        this.direction = direction;
        this.price = price;
        // 1.从资金池中扣除买入金额
        this.pool.exchange(date, this.stockEntity.getId(), this.stockEntity.getName(), direction, price, quantity);
        // 2.增加响应持仓数量
        this.quantity += direction * quantity;
        // 3. 更新渐进率
        if (1 == direction) {
            this.asymptote++;
        }
    }

    // 当日无交易时，更新收盘价，计算最新持仓市值
    public void update(PriceEntity price) {
        this.close = price.getClose();
        // 2. 市值必须放在update中，因为清盘之前，balance会计算仓位信息
        this.amount = this.quantity * this.close;
    }

    // TODO 3.0 未来该方法需要改成balance级别的，如果第一级仓位（TLT）不足，就卖二级仓位（IEF），
    //  显示清算结果的时候，也依次显示，只显示当前能交易的一级股票，其他优先级的股票信息不予显示
    public boolean sellable() {
        return this.amount >= this.pool.getMinAmount();
    }

    public void clearing(LocalDate date, double shareCoefficient) {
        //  非交易日不做清算
        if (!this.tradeable()) {
            return;
        }
        // 1. 计算当前股票的历史波动率
        this.technicalAnalysis.add(this.close);
        this.hv = this.technicalAnalysis.hvol(250);
        // 4. 计算趋势因子
        this.trend();
        // 5. 生成阈值
        this.threshold(shareCoefficient);
        // 股票清盘任务
        if (this.priority == 0) {
            this.tradeReportDto.clearing(
                    date, stockEntity.getId(), stockEntity.getName(), this.investCode,
                    this.direction, this.amount, this.close, this.quantity,
                    this.hv, this.asymptote, this.putTrend, this.callTrend,
                    this.putRate, this.callRate, this.put, this.call);
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
