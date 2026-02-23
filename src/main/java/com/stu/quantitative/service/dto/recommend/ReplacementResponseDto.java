package com.stu.quantitative.service.dto.recommend;

import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.entity.StockEntity;
import lombok.Getter;

import java.util.List;

// 替换股阈值策略
@Getter
public class ReplacementResponseDto implements IRecommendedResponseDto {
    private StockEntity stock;
    private double call, put,callCell,putCell,rate;

    public ReplacementResponseDto(StockEntity stockEntity, List<PriceEntity> prices, List<PriceEntity> standards, RecommendedResponseDto recommendedResponseDto) {
        this.stock = stockEntity;
        // 计算出前60天的价格均值的比值，作为指数平均的初始值，以免第一天的权重过大
        double r0 = prices.stream().mapToDouble(PriceEntity::getClose).average().getAsDouble()/standards.stream().mapToDouble(PriceEntity::getClose).average().getAsDouble();
        this.rate = prices.stream().map(price ->
                // 1. 如果standard中有对应日期，则计算价格比值，否则返回负数，下一步统一滤除
                standards.stream().filter(it -> it.getDate().equals(price.getDate()))
                        .findFirst().map(it -> price.getClose() / it.getClose()).orElse(-1.0))
                .filter(it -> it > 0.0)
                .reduce(r0,(y, x) ->  y * 0.9 +   x * 0.1);
        this.call = recommendedResponseDto.getCall() * this.rate;
        this.put = recommendedResponseDto.getPut() * this.rate;
        this.callCell = recommendedResponseDto.getCallCell() / this.rate;
        this.putCell = recommendedResponseDto.getPutCell() / this.rate;
    }

    public ReplacementResponseDto build() {
        return this;
    }
}