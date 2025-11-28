package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.entity.StockEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


//  单股交易对象
@Slf4j
@Data
public class StockPolicy {
    //  预期仓位
    private final TechnicalAnalysis technicalAnalysis = new TechnicalAnalysis();
    private final StockEntity stock;
    // 最小买入卖出数量3000元
    private final double minAmount = 3000.00;

    // 投资品类：黄金，债券，宽基指数，加密货币，高分红

    // policy对象，用于查询现金和市值，计算仓位
    private Policy policy;
    private TradeRecordDto tradeRecord;
    // 买卖向向，0：无交易，1：买，-1：卖
    private int direction = 0;
    //   当天的K线数据
    private PriceEntity kLine;
    private int buyCount = 0;
    // 成交价
    private double exchangePrice, exchangeQuantity = -1.00;
    // 初日波动率
    private double hv = 1.618;

    //        private double price = -1.00;
    // 初始持仓数量 = 0
    private double shareQuantity = 0;
    // 初始持仓金额 = 0
    private double shareAmount = 0.00;

    // 目标买入卖出点
    private double put = -1.0, call = 9999999.00;
    // 趋势交易因子
    // 连续单向交易说明形成趋势概率增加，需要拉开连续交易的比例
    private double putTrend = 1.618, callTrend = 1.618;
    private double putRate = -1.00, callRate = -1.00;


    public StockPolicy( StockEntity stock) {
        this.stock = stock;
    }

    /*
        输入日期，进行该日期的交易
        1. 获取当天的K线数据，如果不存在K线，直接跳出
        2. 设置当前价格currentPrice，该价格用于运算预期价格
        3. 执行交易
     */
    public int endGameExecute(PriceEntity price) {
        this.kLine = price;
        if (this.kLine == null) {
            log.warn("{} 没有K线数据", this.stock.getTicker());
            return 0;
        }
        // 2.1 计算当前股票的历史波动率
        this.technicalAnalysis.add(this.kLine);
        this.hv = Math.sqrt(this.technicalAnalysis.hvol(250) * 100);
        log.info("{} 历史波动率：{}", this.stock.getTicker(), this.hv);
        return 0;
    }

    /**
     * 回测执行交易
     * @return
     */
    public int backTradeExechte(PriceEntity price) {
        this.kLine = price;
        if (this.kLine == null) {
            // 回测交易如果没有交易数据，可能是停牌等原因
            log.warn("{} 没有K线数据", this.stock.getTicker());
            return 0;
        }
        // 2.1 计算当前股票账户的历史波动率
        this.technicalAnalysis.add(this.kLine);
        this.hv = Math.sqrt(this.technicalAnalysis.hvol(250) * 100);
        log.info("{} 历史波动率：{}", this.stock.getTicker(), this.hv);

        return 0;
    }

}