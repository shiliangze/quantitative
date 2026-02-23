package com.stu.quantitative.service.dto.recommend;

import com.stu.quantitative.service.dto.balance.SummaryResponseData;

public record RecommendedRequestDto(
        int balanceId,
        double balanceAmount,
        SummaryResponseData summary){
}
