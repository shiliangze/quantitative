package com.stu.quantitative.service.domain;

import com.stu.quantitative.entity.PriceEntity;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.HashMap;
import java.util.Map;

class TechnicalAnalysis {
    private Map<Integer, CircularFifoQueue<Double>> history = new HashMap<Integer, CircularFifoQueue<Double>>() {{
        put(5, new CircularFifoQueue<Double>(5));
        put(10, new CircularFifoQueue<Double>(10));
        put(20, new CircularFifoQueue<Double>(20));
        put(30, new CircularFifoQueue<Double>(30));
        put(60, new CircularFifoQueue<Double>(60));
        put(120, new CircularFifoQueue<Double>(120));
        put(250, new CircularFifoQueue<Double>(250));
    }};

    // 添加价格到历史记录
    //  如果CircularFifoQueue未填满，返回0
    public void add(double close) {
        for (CircularFifoQueue<Double> queue : history.values()) {
            queue.add(close);
        }
    }

    // 计算均线
    public double ma(int type) {
        CircularFifoQueue<Double> queue = history.get(type);
        double sum = 0.0;
        for (double p : queue) {
            sum += p;
        }
        return sum / queue.size();
    }

    // 计算历史波动率
    public double hvol(int type) {
        CircularFifoQueue<Double> queue = history.get(type);
        int size = queue.size();

        // 如果数据不足，返回0
        if (size < 2) {
            return 0.05;
        }

        // 计算每日收益率
        double[] returns = new double[size - 1];
        double prev = 0.0;
        int index = 0;

        for (double p : queue) {
            if (prev != 0.0) {
                double dailyReturn = Math.log(p / prev);
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