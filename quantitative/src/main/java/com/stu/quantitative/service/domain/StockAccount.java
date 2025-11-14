package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.entity.StockEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


//  单股对象
@Slf4j
@Data
public class StockAccount {
    //  预期仓位
    private final BalanceAccount balanceAccount;
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


    public StockAccount(BalanceAccount balanceAccount, StockEntity stock) {
        this.stock = stock;
        this.balanceAccount = balanceAccount;
    }

    /*
        输入日期，进行该日期的交易
        1. 获取当天的K线数据，如果不存在K线，直接跳出
        2. 设置当前价格currentPrice，该价格用于运算预期价格
        3. 执行交易
     */
    public int endGameExecute(TradeDateDto record) {
        this.kLine = record.getPrices().get(this.stock.getTicker()).orElse(null);
        if (this.kLine == null) {
            log.warn("{} 没有K线数据", this.stock.getTicker());
            return 0;
        }
        // 2.1 计算当前股票的历史波动率
        this.technicalAnalysis.add(this.kLine);
        this.hv = Math.sqrt(this.technicalAnalysis.hvol(250) * 100);
        // 3. 执行交易
        //  根据日期查找是否存在历史交易
        this.policy.getEndgames().stream().filter(it ->
                        it.getDate().equals(record.getDate()) && it.getStockId() == this.stock.getId()).findAny()
                .ifPresent(endgame -> {
                    // 设置交易价
                    this.exchangePrice = endgame.getPrice();
                    this.exchangeQuantity = endgame.getQuantity();
                    // 执行量化判交易
                    // 1.从资金池中扣除买入金额
                    this.policy.exchange(-1 * endgame.getDirection() * this.exchangePrice * this.exchangeQuantity);
                    // 2.增加响应持仓数量
                    this.shareQuantity += endgame.getDirection() * this.exchangeQuantity;
                    // 3.设置交易方向标志，供外部访问
                    this.direction = endgame.getDirection();
                });
        // 2.2 计算当前股票的持仓市值
        this.shareAmount = this.kLine.getClose() * this.shareQuantity;
        return this.direction;
    }

    public int backTradeExechte(TradeDateDto record) {
        this.kLine = record.getPrices().get(this.stock.getTicker()).orElse(null);
        if (this.kLine == null) {
            log.warn("{} 没有K线数据", this.stock.getTicker());
            return 0;
        }
        // 2.1 计算当前股票账户的历史波动率
        this.technicalAnalysis.add(this.kLine);
        this.hv = Math.sqrt(this.technicalAnalysis.hvol(250) * 100);
        // 3. 执行交易
        if (this.kLine.getLow() <= this.call) { // 如果当天的最低价小于call，则执行买入操作
            this.buy();
        } else if (this.kLine.getHigh() >= this.put) { // 如果当天的最高价大于等于put，则执行卖出操作
            this.sale();
        }
        // 2.2 计算当前股票的持仓市值
        this.shareAmount = this.kLine.getClose() * this.shareQuantity;
        return this.direction;
    }

    // 盘后清算
    public void clearing(TradeDateDto record, int direction) {
        if (this.kLine == null) {
            return ;
        }
        if (direction > 0) {
            ++this.buyCount;
        } else if (direction < 0) {
            --this.buyCount;
        }
        if(0==this.direction && 1==Math.abs(direction)){
            // 如果当前股票无交易，且，平衡仓有交易，以收盘价作为交易价
            this.exchangePrice = this.kLine.getClose();
        }
        // 2.4 计算趋势因子
        this.trend();
        // 2.5 计算当前平衡仓股票的仓位
        this.balanceAccount.share();
        // 2.6 计算阈值与目标价
        this.threshold();
        this.addTradeToLog(record);
    }

    // 是否已上市
    public boolean tradeable(TradeDateDto record) {
        return null != record.getPrices().get(this.stock.getTicker()).orElse(null);
    }


    // 买入
    private void buy() {
        // 考虑到除权等操作，call高于最高价，则按最高价作为交易价
        this.exchangePrice = Math.min(this.call, this.kLine.getHigh());
        // 如果资金池小于最低金额，直接跳过，不进行买入操作
        if (this.policy.getCash() < this.minAmount) {
            return;
        }
        double shareRate = Math.pow(2 / Math.PI * Math.atan(++this.buyCount), 2);
        double callAmount = Math.min(policy.getCash() * this.balanceAccount.getExpectedShareRate() * shareRate / 5, this.policy.getCash());
        this.exchangeQuantity = callAmount / this.exchangePrice;
        // 1.从资金池中扣除买入金额
        this.policy.exchange(-1 * callAmount);
        // 2.增加响应持仓数量
        this.shareQuantity += this.exchangeQuantity;
        this.direction = 1;
    }

    // 卖出
    private void sale() {
        // 卖出交易价不得低于最低价
        this.exchangePrice = Math.max(this.put, this.kLine.getLow());
        // 如果剩余仓位不足，直接跳过
        if (this.shareAmount < this.minAmount) {
            return;
        }
        // 本次卖出金额，仓位20%与最小卖出金额，两者取大
        double putAmount = Math.max(this.shareAmount / 5, this.minAmount);
        this.exchangeQuantity = putAmount / this.exchangePrice;
        // 1.从资金池中加入买入金额
        this.policy.exchange(putAmount);
        // 2.减少响应持仓数量
        this.shareQuantity -= this.exchangeQuantity;
        this.direction = -1;
    }

    // 计算趋势因子
    private void trend() {
//        this.direction = 0 或 2 全部跳过
        if (1==this.direction) {
            // tradeFlag = 1 买入交易
            // 买入步长增加卖出步长开平方
            this.callTrend *= 1.618;
            this.putTrend = Math.sqrt(this.putTrend);
        } else if (-1==this.direction) {
            this.putTrend *= 1.618;
            this.callTrend = Math.sqrt(this.callTrend);
        }
    }

    // 计算阈值
    private void threshold() {
        // 4. 计算买入阈值
        this.callRate = 1.00 - (this.hv + this.balanceAccount.getCallShare() + this.callTrend) / 100;
        this.putRate = 1.00 + (this.hv + this.balanceAccount.getPutShare() + this.putTrend) / 100;
        this.put = this.exchangePrice * putRate;
        this.call = this.exchangePrice * callRate;
    }


    private void addTradeToLog(TradeDateDto record) {
        if (this.direction == 0) {
//            如果当天无真实交易，直接退出
            return;
        }
        TradeRecordDto tradeRecordDto = new TradeRecordDto(this.stock.getName(), this.direction, this.exchangePrice, this.exchangeQuantity, this.hv, this.balanceAccount.getCallShare(), this.balanceAccount.getPutShare(), this.callTrend, this.putTrend, this.callRate, this.putRate);
        record.addTrade(tradeRecordDto);
        // 计算完毕后立即归位
        this.direction = 0;
    }

    public void addShareToLog(TradeDateDto record) {
        double callQuantity = Math.max(policy.getCash() * this.balanceAccount.getExpectedShareRate() / 5, this.minAmount) / this.call;
        double putQuantity = Math.max(this.shareAmount / 5, this.minAmount) / this.put;
        ShareRecordDto shareRecordDto = new ShareRecordDto(this.getStock().getName(), this.kLine.getClose(), this.shareQuantity, this.shareAmount, this.balanceAccount.getPosition(), this.call, this.put, callQuantity, putQuantity);
        record.addShare(shareRecordDto);
    }
}