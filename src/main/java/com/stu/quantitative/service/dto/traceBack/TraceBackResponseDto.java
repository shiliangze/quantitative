package com.stu.quantitative.service.dto.traceBack;

import java.util.List;

public record TraceBackResponseDto(int stockId, String  stockName, List<TraceBackResponseData> data) {

}
