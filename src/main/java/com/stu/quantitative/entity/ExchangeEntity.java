package com.stu.quantitative.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "exchange") // 表名称
@SQLRestriction("deleted = false")
public class ExchangeEntity {
    // ID
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 枚举编号
    @Column
    private int code;


    // 数据来源
    @Column
    private int source;

    // 枚举名称
    @Column
    private String value;

    // 货币编码
    @Column
    private int currency;




    // 备注
    @Column
    private String note;

    // 是否删除
    @JsonIgnore
    @Column
    private boolean deleted;

}
