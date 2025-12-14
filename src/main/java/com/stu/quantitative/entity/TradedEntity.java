package com.stu.quantitative.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "traded") // 表名称
@SQLRestriction("deleted = false")
public class TradedEntity {
    // ID
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    // 关联外键: stock.id
    @Column
    private int stockId;
    // 交易价
    @Column
    private double price;
    // 交易数量
    @Column
    private double quantity;
    // 交易方向，1为买入，-1为卖出
    @Column
    private int direction;
    // 开盘价
    @Column
    private double brokerage;
    // 交易日
    @Column
    private LocalDate date;
    // 是否删除
    @JsonIgnore
    @Column
    private boolean deleted;

}
