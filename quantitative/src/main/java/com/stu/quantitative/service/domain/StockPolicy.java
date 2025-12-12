package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.entity.TradedEntity;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;


//  单股交易对象
@Slf4j
@Data
public class StockPolicy {
    //  预期仓位
    @Getter
    private final StockAccount stockAccount;
    private final BalanceAccount balanceAccount;
    // klines和tradeds都是已排序的
    private final List<PriceEntity> klines;
    private final List<TradedEntity> tradeds;

    /**
     * 以下为：周期为一天的变量
     */
    // 当前日期
    private LocalDate date;
    // 当前日期的K线
    private PriceEntity currentKLine;
    // 当前日期的成交记录
    private TradedEntity currentTraded;


    public StockPolicy(StockAccount stockAccount, BalanceAccount balanceAccount,
                       List<PriceEntity> klines, List<TradedEntity> tradeds) {
        this.stockAccount = stockAccount;
        this.balanceAccount = balanceAccount;
        this.klines = klines;
        this.tradeds = tradeds;
    }

    public LocalDate getStartKLine() {
        return this.klines.getFirst().getDate();
    }

    public LocalDate getStartTraded() {
        return this.tradeds.getFirst().getDate();
    }

    public LocalDate getEndKline() {
        return this.klines.getLast().getDate();
    }


    /**
     * 1.初始化当前日期的K线和成交记录
     * 2.判断是否交易日或停牌日
     *
     * @param date
     * @return
     */
    public boolean tradeable(LocalDate date) {
        this.date = date;
        this.currentKLine = this.klines.stream().filter(it -> it.getDate().equals(date)).findFirst().orElse(null);
        return null != this.currentKLine;
    }

    //  初始化当日交易
    public void initEndGame() {
        this.currentTraded = this.tradeds.stream().filter(it -> it.getDate().equals(date)).findFirst().orElse(null);
    }

    /*
        输入日期，进行该日期的交易
        1. 获取当天的K线数据，如果不存在K线，直接跳出
        2. 设置当前价格currentPrice，该价格用于运算预期价格
        3. 执行交易
     */
    public void endGameExecute() {
        // 执行交易
        //  根据日期查找是否存在历史交易
        if (null != this.currentTraded) {
            //当日有交易，用交易加计算市值
            this.stockAccount.exchange(
                    this.date,
                    this.currentTraded.getDirection(),
                    this.currentTraded.getPrice(),
                    this.currentTraded.getQuantity()
            );
        }
        // 更新当日收盘价，计算最新持仓市值
        // 因为所有k线日无论是否有交易，都会执行endGameExecute，所以该方法放在endGameExecute内
        this.stockAccount.update(this.currentKLine);
    }

    /**
     * 回测执行交易
     *
     * @return
     */
    public int backTradeExecute() {
        //  TODO  2.0 历史波动率已经放到盘后计算了，第二天用前日计算得出的历史波动率
        //      注意首日交易问题，设置好初始值，保证首日交易正常

        // 3. 执行交易
        if (this.currentKLine.getLow() <= this.stockAccount.getCall()) {
            this.buy();// 如果当天的最低价小于call，则执行买入操作
        } else if (this.currentKLine.getHigh() >= this.stockAccount.getPut()) {
            this.sale();// 如果当天的最高价大于等于put，则执行卖出操作
        }
        return this.stockAccount.getDirection();
    }

    // 买入
    private void buy() {
        if (!this.stockAccount.getPool().buyable()) {
            return;// 如果资金池不足，直接跳过
        }
        // 考虑到除权等操作，call高于最高价，则按最高价作为交易价
        double exchangePrice = Math.min(this.stockAccount.getCall(), this.currentKLine.getHigh());
        // 1.计算交易金额
        double callAmount = this.stockAccount.getPool().getCash() / 5 //购买金额，初始值，现金/5
                * this.balanceAccount.getShare() // 乘以平衡仓的预期份额
                * Math.pow(2 / Math.PI * Math.atan(this.stockAccount.generateAsymptoteRate()), 2); // 乘以渐进率，（asymptote 每次调用，意味着执行了一次买入交易，自增1）
        callAmount = Math.max(callAmount, this.stockAccount.getPool().getMinAmount()); // 交易金额不得低于最小交易金额数

        // 交易数量：购买金额/交易价
        double quantity = callAmount / exchangePrice;
        // 执行交易记账
        this.stockAccount.exchange(this.date,1, exchangePrice, quantity);
    }

    // 卖出
    private void sale() {
        // 如果剩余仓位不足，直接跳过
        if (!this.stockAccount.sellable()) {
            return;
        }
        // 考虑到除权等操作，卖出交易价不得低于最低价
        double exchangePrice = Math.max(this.stockAccount.getPut(), this.currentKLine.getLow());
        // 本次卖出金额，平衡仓位20%与最小卖出金额，两者取大
        double putAmount = Math.max(this.balanceAccount.getAmount() / 5, this.stockAccount.getPool().getMinAmount());
        double quantity  = putAmount / exchangePrice;
        // 1.从资金池中加入买入金额
        this.stockAccount.exchange(this.date,-1, exchangePrice, quantity);
    }
}