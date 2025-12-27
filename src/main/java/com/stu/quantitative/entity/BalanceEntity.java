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

    @Column
    private String name;

    // 投资类型编号
    @Column
    private int planCode;

    // 预期仓位
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
