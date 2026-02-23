package com.stu.quantitative.service.dto.balance;

import java.util.List;

public record BalanceResponseDto(List<BalanceResponseData> balances, List<SummaryResponseData> summaries) {

}
