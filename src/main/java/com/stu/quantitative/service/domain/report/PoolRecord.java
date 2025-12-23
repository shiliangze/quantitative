package com.stu.quantitative.service.domain.report;


import java.time.LocalDate;

public record PoolRecord(LocalDate date, double cash, double amount, double total) {
    public void report() {
        System.out.printf("%s：总市值：%.3f，现金流：%.3f，总资产：%.3f%n",
                date, amount, cash, total);
    }
}
