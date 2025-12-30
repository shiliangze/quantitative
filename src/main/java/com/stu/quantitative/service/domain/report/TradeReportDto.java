package com.stu.quantitative.service.domain.report;

import com.stu.quantitative.service.domain.BalanceRecord;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TradeReportDto {
    // 股票交易记录
    private final List<TradeRecord> tradeRecords = new ArrayList<>();
    // 清算记录：依次为：总表，平衡表，股票表
    private final List<PoolRecord> poolRecords = new ArrayList<>();
    private final List<BalanceRecord> balanceRecords = new ArrayList<>();
    private final List<StockRecord> stockRecords = new ArrayList<>();

    public void exchange(LocalDate date, int stockId, String stockName, int direction, double price, double quantity) {
        this.tradeRecords.add(new TradeRecord(date, stockId, stockName, direction, price, quantity));
    }

    // 总仓清盘
    public void clearing(LocalDate date, double cash, double amount, double total) {
        this.poolRecords.add(new PoolRecord(date, cash, amount, total));
    }

    // 平衡仓清盘
    public void clearing(LocalDate date, int investCode, String investName, double amount, double share,
                         double position, double shareOffset, double shareCoefficient) {
        this.balanceRecords.add(new BalanceRecord(
                date, investCode, investName, amount, share, position, shareOffset, shareCoefficient));
    }

    // 股票清盘
    public void clearing(
            LocalDate date, int stockId, String stockName, int investCode,
            int direction, double amount, double close, double quantity,
            double hv, double asymptote, double putTrend, double callTrend,
            double putRate, double callRate, double put, double call,
            double profit, double profitRate) {

        this.stockRecords.add(new StockRecord(
                date, stockId, stockName, investCode, direction, amount, close, quantity, hv,
                asymptote, putTrend, callTrend, putRate, callRate, put, call,
                profit, profitRate));
    }

    public void report(LocalDate startDate, LocalDate endDate) {
        System.out.printf("========回测交易：开始日期：%s========%n", startDate);
        this.report(startDate, endDate, true);
        System.out.printf("========终日清算日期：%s========%n", endDate);
        this.report(endDate, endDate.plusDays(1), false);
        System.out.printf("========结算信息========%n");
        this.statistic();

    }

    private void report(LocalDate start, LocalDate end, Boolean normal) {
        start.datesUntil(end).forEach(date -> {
            List<TradeRecord> tradeRecords = this.tradeRecords.stream()
                    .filter(it -> it.date().equals(date)).toList();
            if (tradeRecords.isEmpty() && normal) {
//                System.out.println("无交易记录\n");
            } else {
                System.out.printf("========交易日期：%s========%n", date);
                // 打印交易信息，因为一天可能发生多比交易，所以用循环
                this.tradeRecords.stream().filter(it -> it.date().equals(date))
                        .forEach(TradeRecord::report);
                // 打印清算信息
                // 打印平衡仓清算信息
                this.balanceRecords.stream().filter(it -> it.date().equals(date))
                        .forEach(BalanceRecord::report);

                this.stockRecords.stream().filter(it -> it.date().equals(date))
                        .forEach(StockRecord::report);
                // 打印总仓清算信息
                this.poolRecords.stream().filter(it -> it.date().equals(date)).findFirst().get().report();
            }
        });
    }

    private void statistic() {
        Set<Integer> stockIds = this.tradeRecords.stream()
                .map(TradeRecord::stockId).collect(Collectors.toSet());
        stockIds.forEach(stockId -> {
            List<StockRecord> records = this.stockRecords.stream()
                    .filter(it -> it.stockId() == stockId).toList();
            List<TradeRecord> tradeRecords = this.tradeRecords.stream()
                    .filter(it -> it.stockId() == stockId).toList();
            if (!records.isEmpty()) {
                Statistic statistic = new Statistic(records, tradeRecords);
                statistic.report();

            }
        });
    }

    class Statistic {
        private final StockRecord start, end;
        private final long delta;
        private final int tradeCount;   // 交易次数
        private final double mesh;   // 平均网格
        private final List<StockRecord> maxBuy;

        public Statistic(List<StockRecord> records, List<TradeRecord> tradeRecords) {
            this.start = records.getFirst();
            this.end = records.getLast();
            this.delta = ChronoUnit.DAYS.between(start.date(), end.date());
            this.tradeCount = tradeRecords.size();
            this.mesh = records.stream().mapToDouble(it -> it.putRate() - it.callRate()).average().orElse(0.0);
            this.maxBuy = getLongestDirection(records, 1);
        }

        private List<StockRecord> getLongestDirection(List<StockRecord> records, int direction) {
            int delimiter = -1 * direction;
            List<StockRecord> result = new ArrayList<>();
            List<StockRecord> middleware = new ArrayList<>();
            for (StockRecord record : records) {
                // direction为分隔符&&中间件数组不为空：判定分隔符首位
                if (record.direction() == delimiter && !middleware.isEmpty()) {
                    if (middleware.size() > result.size()) {
                        result = middleware;
                    }
                    middleware = new ArrayList<>();
                } else if (record.direction() == direction) {
                    middleware.add(record);
                }
            }
            return result;
        }

        public void report() {
            System.out.printf("%s：ipo日期：%s，结束日期：%s，总共天数：%d，交易次数：%d，年均交易次数：%.2f，平均网格：%.2f%n",
                    start.stockName(), start.date(), end.date(), delta, tradeCount, tradeCount * 365.0 / delta, mesh);
            System.out.printf("最大连续购买：%d%n", maxBuy.size());
            maxBuy.forEach(it -> System.out.printf("日期：%s，收盘价：%.3f,趋势买入因子：%.4f，买入指导价：%.3f%n", it.date(), it.close(),it.callTrend(),it.call()));
        }
    }
}
