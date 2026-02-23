package com.stu.quantitative.service.dto.recommend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.entity.StockEntity;
import com.stu.quantitative.entity.TradedEntity;
import com.stu.quantitative.service.domain.TechnicalAnalysis;
import com.stu.quantitative.service.dto.balance.SummaryResponseData;
import lombok.Getter;

import java.util.List;

// 指导价
@Getter
public class RecommendedResponseDto implements IRecommendedResponseDto {
    @JsonIgnore
    private final TechnicalAnalysis technicalAnalysis = new TechnicalAnalysis();
    @JsonIgnore
    private final RecommendedRequestDto recommendedRequest;
    @JsonIgnore
    private final  double  balanceShare;
    private StockEntity stock;
    private double offset;

    private double hv;
    private double  callTrend = 1.618,putTrend = 1.618;
    private double baseline,call,put,putCell,callCell;

    public RecommendedResponseDto(StockEntity stockEntity,RecommendedRequestDto  recommendedRequest, double balanceShare, List<PriceEntity> prices) {
        this.stock = stockEntity;
        this.recommendedRequest = recommendedRequest;
        this.balanceShare = balanceShare;
        prices.forEach(price -> technicalAnalysis.add(price.getClose()));
        this.hv = technicalAnalysis.hvol(60);
        //  先设置基准价为最后一天的收盘价，如果有交易记录，则用交易价覆盖，如果没有，则直接使用该价格
        this.baseline = prices.getLast().getClose();
        double position = recommendedRequest.balanceAmount() / recommendedRequest.summary().total();
        this.offset = 2 * Math.tanh(Math.log(3) * Math.log(position) / (2 * Math.log(balanceShare)));
    }

    public void next(TradedEntity tradedEntity){
        if(tradedEntity.getDirection() == 1 && tradedEntity.getFeature() == 0){
            // 买入操作，并且不是派息再投资
            this.callTrend *= 1.618;
            this.putTrend = Math.sqrt(this.putTrend);
        }
        else if(tradedEntity.getDirection() == -1 ){
            // 卖出操作
            this.putTrend *= 1.618;
            this.callTrend = Math.sqrt(this.callTrend);
        }
        this.baseline = tradedEntity.getPrice();
    }

    public RecommendedResponseDto build(){
        double callRate = 1 - (this.hv + this.callTrend * offset / 100) / 2;
        double putRate = 1 + (this.hv + this.putTrend * (2-offset) / 100) / 2;
        this.put = this.baseline * putRate;
        this.call = this.baseline * callRate;
        this.putCell = this.recommendedRequest.balanceAmount()/ put / (15 * this.balanceShare);
        this.callCell = this.recommendedRequest.summary().cash() / call / 15;
        return this;
    }
}