package com.stu.quantitative.service.dto.traceBack;

import java.time.LocalDate;

public record TraceBackRequestDto (
        int stockId,
        LocalDate startDate,
        double startPosition
) {
}
