package com.stu.quantitative.service.dto.traceBack;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class TraceBackResponseData {
    private final int stockId;
    private final LocalDate date;
    private final double hv;
    private final int direction;
    private final double price;
    private final double quantity;
    private final double offset;
    private final double callTrend;
    private final double putTrend;
    private final double callRate;
    private final double putRate;
    private final double call;
    private final double put;
    private final double cash;
    private final double inventory;
    private final double position;
    private final List<String> descriptions = new ArrayList<>();

    public TraceBackResponseData(int stockId, LocalDate date, double hv, int direction, double price, double quantity, double offset, double callTrend, double putTrend, double callRate, double putRate, double call, double put, double cash, double inventory) {
        this.stockId = stockId;
        this.date = date;
        this.hv = hv;
        this.direction = direction;
        this.price = price;
        this.quantity = quantity;
        this.offset = offset;
        this.callTrend = callTrend;
        this.putTrend = putTrend;
        this.callRate = callRate;
        this.putRate = putRate;
        this.call = call;
        this.put = put;
        this.cash = cash;
        this.inventory = inventory;
        this.position = price * inventory / (price * inventory + cash);
        this.descriptions.add(String.format("position(仓位) = %.3f", position));
        this.descriptions.add(String.format("offset(偏移率) = 2 * tanh(ln(3) * ln(position) / (2 * ln(balanceShare))) = %.3f", offset));
        this.descriptions.add(String.format("quantity = %s = %.3f", direction == 1 ? "cash / 15" : "price * inventory / (15 * balanceShare)", quantity));
        this.descriptions.add(String.format("callTrend(买入趋势) = %s = %.3f", direction == 1 ? "callTrend * 1.618" : "√(callTrend)", callTrend));
        this.descriptions.add(String.format("putTrend(卖出趋势) = %s = %.3f", direction == 1 ? "√(putTrend)" : "putTrend * 1.618", putTrend));
        this.descriptions.add(String.format("callRate(买入强度) = 1 - (hv + callTrend * offset / 100) / 2 = 1 - (%.3f + %.3f * %.3f / 100) / 2 = %.3f", hv, callTrend, offset, callRate));
        this.descriptions.add(String.format("putRate(卖出强度) = 1 - (hv + putTrend * (2 - offset) / 100) / 2 = 1 - (%.3f + %.3f * (2 - %.3f) / 100) / 2 = %.3f", hv, putTrend, offset, putRate));
        this.descriptions.add(String.format("call(买入指导价) = price * callRate = %.3f * %.3f = %.3f", price, callRate, call));
        this.descriptions.add(String.format("put(卖出指导价) = price * putRate = %.3f * %.3f = %.3f", price, putRate, put));
    }
}