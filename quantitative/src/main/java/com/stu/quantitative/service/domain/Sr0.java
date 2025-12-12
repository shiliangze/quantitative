package com.stu.quantitative.service.domain;

public record Sr0(String investment, double close, double quantity, double amount, double shareRate, double call,
                  double put, double callQuantity, double putQuantity){
    public void printAll(){
        System.out.printf("%s::当日收盘价：%.3f，持仓数量：%.3f，持仓市值：%.3f，持仓比例：%.3f，买入价：%.3f，卖出价：%.3f，买入数量：%.3f，卖出数量：%.3f%n",
                 this.investment,this.close, this.quantity, this.amount, this.shareRate, this.call, this.put, this.callQuantity, this.putQuantity);
    }
}
