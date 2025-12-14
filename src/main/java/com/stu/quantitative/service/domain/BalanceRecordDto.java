package com.stu.quantitative.service.domain;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class BalanceRecordDto {
    private final List<StockRecordDto> stockRecords = new ArrayList<>();
    private final int investCode; // 投资组合代码
//    private final double total;   // 总仓资产总值
    private final double amount;    // 平衡仓资产值
    private final double share;   // 平衡仓预期份额：默认0.5
    private final double position;   // 当前实际份额 total / amount
    private final double shareOffset;   // 偏移量：(1.0001-position) / (share + 0.0001)
    private final double shareCoefficient;   // 预期份额算子：Math.log10(this.shareOffset +9)



    public void clearing(StockRecordDto stockRecordDto) {
        this.stockRecords.add(stockRecordDto);
    }

    public String report(){
        StringBuffer output = new StringBuffer();
        output.append(String.format("--------------Balance编码：%d----------\n", this.investCode));
        output.append(
                String.format("持仓资产：%.3f，预期份额：%.3f，当前实际份额：%.3f，份额偏移量：%.3f，份额算子：%.3f\n",
                        this.amount,this.share,this.position,this.shareOffset,this.shareCoefficient)
        );
        output.append(String.format("-------------------------------\n"));
        this.stockRecords.forEach(stockRecordDto -> output.append(stockRecordDto.report()));
        return output.toString();
    }
}
