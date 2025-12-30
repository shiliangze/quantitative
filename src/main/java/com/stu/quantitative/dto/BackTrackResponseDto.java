package com.stu.quantitative.dto;

import java.util.List;

public record BackTrackResponseDto(
        List<BackTrackStockDto> backTrackStocks
) {  // 初始资金
}
