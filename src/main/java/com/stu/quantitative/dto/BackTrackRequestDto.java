package com.stu.quantitative.dto;

public record BackTrackRequestDto(
        int planCode,   // 0. 美股网格 1.A股网格，
        int tradeType,  // 0：历史交易 1：回测交易
        int startType,  // 0：IPO 1：首次交易
        double cash // 启动资金
) {  // 初始资金
}
