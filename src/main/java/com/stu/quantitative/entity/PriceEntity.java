package com.stu.quantitative.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "price") // 表名称
@SQLRestriction("deleted = false")
public class PriceEntity {
    // ID
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    // 关联外键: stock.id
    @Column
    private int stockId;
    // 开盘价
    @Column
    private double open;
    // 开盘价
    @Column
    private double high;
    // 开盘价
    @Column
    private double low;
    // 成交量
    @Column
    private long volume;
    // 收盘价
    @Column
    private double close;
    // 交易日
    @Column
    private LocalDate date;
    // 是否删除
    @JsonIgnore
    @Column
    private boolean deleted;

    public PriceEntity() {
    }
}
