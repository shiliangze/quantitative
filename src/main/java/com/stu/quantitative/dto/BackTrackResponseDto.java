package com.stu.quantitative.dto;

import com.stu.quantitative.entity.BalanceEntity;

import java.util.List;

public record BackTrackResponseDto(
        List<BalanceEntity> balances,
        List<BackTrackStockDto> backTrackStocks
) {  // 初始资金
}
