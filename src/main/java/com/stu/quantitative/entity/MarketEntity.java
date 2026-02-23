package com.stu.quantitative.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

@Data
@Entity
@Table(name = "market") // 表名称
@SQLRestriction("deleted = false")
public class MarketEntity {
    // ID
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    // 关联外键: stock.id
    @Column
    private String name;
    @Column
    private double cash;
    @Column
    private double boardLot;
    // 是否删除
    @JsonIgnore
    @Column
    private boolean deleted;
}
