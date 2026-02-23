package com.stu.quantitative.service.dto.recommend;

import com.stu.quantitative.entity.StockEntity;

public interface IRecommendedResponseDto {
    StockEntity getStock();
    double getCall();
    double getPut();
}
