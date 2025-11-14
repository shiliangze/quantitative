package com.stu.quantitative.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "stock") // 表名称
@SQLRestriction("deleted = false")
public class StockEntity {
    // ID
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 股票编号
    @Column
    private String ticker;

    // 股票名称
    @Column
    private String name;


    // 货币编码：USD/CNY
    @Column
    private String currencyCode;

    // 基金管理费率（万分之）
    @Column
    private int emr;

    // 备注
    @Column
    private String note;

    // 是否删除
    @JsonIgnore
    @Column
    private boolean deleted;

}
