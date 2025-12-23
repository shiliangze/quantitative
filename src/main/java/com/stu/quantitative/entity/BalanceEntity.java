package com.stu.quantitative.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "balance") // 表名称
@SQLRestriction("deleted = false")
public class BalanceEntity {
    // ID
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 股票编号
    @Column
    private int planCode;

    // 股票名称
    @Column
    private int investCode;


    //  股票代码
    @Column
    private int stockId;

    @Column int priority;

    // 仓位指数
    @Column
    private double share;

    // 备注
    @Column
    private String note;

    // 是否删除
    @JsonIgnore
    @Column
    private boolean deleted;

}
