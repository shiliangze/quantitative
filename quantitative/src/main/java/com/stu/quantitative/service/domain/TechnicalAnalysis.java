package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.PriceEntity;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.HashMap;
import java.util.Map;

class TechnicalAnalysis {
    private Map<Integer, CircularFifoQueue<PriceEntity>> history = new HashMap<Integer, CircularFifoQueue<PriceEntity>>() {{
        put(5, new CircularFifoQueue<PriceEntity>(5));
        put(10, new CircularFifoQueue<PriceEntity>(10));
        put(20, new CircularFifoQueue<PriceEntity>(20));
        put(30, new CircularFifoQueue<PriceEntity>(30));
        put(60, new CircularFifoQueue<PriceEntity>(60));
        put(120, new CircularFifoQueue<PriceEntity>(120));
        put(250, new CircularFifoQueue<PriceEntity>(250));
    }};

    // 添加价格到历史记录
    //  如果CircularFifoQueue未填满，返回0
    public void add(PriceEntity price) {
        for (CircularFifoQueue<PriceEntity> queue : history.values()) {
            queue.add(price);
        }
    }

    // 计算均线
    public double ma(int type) {
        CircularFifoQueue<PriceEntity> queue = history.get(type);
        double sum = 0.0;
        for (PriceEntity p : queue) {
            sum += p.getClose();
        }
        return sum / queue.size();
    }

    // 计算历史波动率
    public double hvol(int type) {
        CircularFifoQueue<PriceEntity> queue = history.get(type);
        int size = queue.size();

        // 如果数据不足，返回0
        if (size < 2) {
            return 0.05;
        }

        // 计算每日收益率
        double[] returns = new double[size - 1];
        PriceEntity prev = null;
        int index = 0;

        for (PriceEntity p : queue) {
            if (prev != null) {
                double dailyReturn = Math.log(p.getClose() / prev.getClose());
                returns[index++] = dailyReturn;
            }
            prev = p;
        }

        // 使用Apache Commons Math的StandardDeviation类计算标准差
        double volatility = new StandardDeviation().evaluate(returns);

        // 年化处理（乘以交易日的平方根，通常252个交易日）
        return volatility * Math.sqrt(252);
    }
    }