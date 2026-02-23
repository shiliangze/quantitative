package com.stu.quantitative.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "indicator") // 表名称
@SQLRestriction("deleted = false")
public class IndicatorEntity {
    // ID
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    // 关联外键: stock.id
    @Column
    private int stockId;
    @Column
    private double price;
    @Column
    private int code;
    // 是否删除
    @Column
    private boolean deleted;
    // 交易日
    @Column
    private LocalDate date;
    public IndicatorEntity() {
    }
}
