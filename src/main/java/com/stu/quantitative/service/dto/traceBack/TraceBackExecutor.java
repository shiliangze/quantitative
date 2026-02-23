package com.stu.quantitative.service.dto.traceBack;

import com.stu.quantitative.entity.BalanceEntity;
import com.stu.quantitative.entity.MarketEntity;
import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.service.domain.TechnicalAnalysis;

import java.util.ArrayList;
import java.util.List;

public class TraceBackExecutor {
    private final TraceBackRequestDto traceBackRequestDto;
    private final MarketEntity market;
    private final TechnicalAnalysis technicalAnalysis = new TechnicalAnalysis();
    private final BalanceEntity balanceEntity;

    private double hv = 0.05,asymptote = 0.5;;
    private double cash, inventory=0.0,quantity,price=-1.0,putTrend = 1.618,callTrend = 1.618, putRate,callRate,put = Double.MIN_NORMAL, call=Double.MAX_VALUE;
    private int direction = 0;
    private int hymen =0;
    private List<TraceBackResponseData> traceBackResponses = new ArrayList<>();

    public TraceBackExecutor(TraceBackRequestDto traceBackRequestDto, BalanceEntity balanceEntity, MarketEntity market) {
        this.traceBackRequestDto = traceBackRequestDto;
        this.balanceEntity = balanceEntity;
        this.market = market;
        this.cash = market.getCash();
    }

    // 预期仓位(balanceEntity.getShare())，数值必须在（0，0.5）之间
    public void next(PriceEntity priceEntity) {
        // 计算波动率
        this.technicalAnalysis.add(priceEntity.getClose());
        this.hv = this.technicalAnalysis.hvol(60);

        //double cell = this.cash / 3.5; // 基本网格单位是现金总数的3.5分支1，即，当半仓的时候，网格宽度为总金额的七分之一
        double position = this.inventory * priceEntity.getOpen() / this.cash; // 当前仓位
        position = Math.max(position,0.001);// 避免取对数无穷大
        // 构造一个初等函数，作为单位网格的系数，
        //  1.持仓越高，系数越低 2.当仓位=0，系数=2 3.当仓位等于share，系数=1，当仓位=1，系数=0
        //  公式如下： 2 * tanh(ln(3) * ln(position) / (2 * ln(share)))
        double offset = 2 * Math.tanh(Math.log(3) * Math.log(position) / (2 * Math.log(balanceEntity.getShare())));
        this.price = this.hymen == 0 ? priceEntity.getOpen() : this.price;
        double putCell = this.price * this.inventory / (15 * balanceEntity.getShare());
        double callCell = this.cash / 15;
        if (priceEntity.getHigh() > this.put && putCell > market.getBoardLot() && this.inventory * this.put > market.getBoardLot()) {
            //为防止回购等除权情况发生，卖出价取目标价与当日最低价中的最大值
            this.put = Math.max(this.put, priceEntity.getLow());
            putCell = Math.min(putCell, this.inventory * this.put);
            // 卖出操作：1.最高价大于卖出价,2.卖出单元 > 大于最低单元，3.当前存量 > 最小单元
            this.cash += putCell; // 现金增加=卖出单元
            this.quantity = putCell / this.put;
            this.inventory -= this.quantity; // 数量减少=卖出单元/预期价格
            // 卖出交易
            // 卖出步长增加买入步长开平方
            this.putTrend *= 1.618;
            this.callTrend = Math.sqrt(this.callTrend);
            this.price = this.put;
            this.direction = -1;
        } else if (priceEntity.getLow() < this.call && callCell > market.getBoardLot() && this.cash > market.getBoardLot()) {
            // 买入操作：1.最低价低于买入价,2.买入单元大于最低单元，3.现金大于最小单元
            this.asymptote++; // 乘以渐进率，（asymptote 每次调用，意味着执行了一次买入交易，自增1）
            this.call = Math.min(this.call, priceEntity.getHigh());
            callCell =  this.hymen == 0 ? traceBackRequestDto.startPosition()/100 * this.cash : Math.min(callCell, this.cash);
            this.cash -= callCell; // 现金减少=买入单元
            this.quantity = callCell / this.call;
            this.inventory += this.quantity; // 数量增加=买入单元/预期价格
            // 买入交易
            // 买入步长增加卖出步长开平方
            this.callTrend *= 1.618;
            this.putTrend = Math.sqrt(this.putTrend);
            this.price = this.call;
            this.direction = 1;
        }
        // 计算预期价格
        this.callRate = 1 - (this.hv + this.callTrend * offset / 100) / 2;
        this.putRate = 1 + (this.hv + this.putTrend * (2-offset) / 100) / 2;
        this.put = this.price * putRate;
        this.call = this.price * callRate;

        if(this.direction !=0){
            traceBackResponses.add(new TraceBackResponseData(
                    priceEntity.getStockId(),
                    priceEntity.getDate(),
                    this.hv,
                    this.direction,
                    this.price,
                    this.quantity,
                    offset,
                    this.callTrend,
                    this.putTrend,
                    this.callRate,
                    this.putRate,
                    this.call,
                    this.put,
                    this.cash,
                    this.inventory));
        }
        this.direction = 0;
        hymen++;
    }

    public List<TraceBackResponseData> build() {
        return traceBackResponses;
    }

}
