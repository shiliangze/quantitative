package com.stu.quantitative.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "code_config") // 表名称
@SQLRestriction("deleted = false")
public class CodeConfigEntity {
    // ID
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 枚举编号
    @Column
    private int code;

    // 枚举名称
    @Column
    private String value;


    // 枚举种类
    @Column
    private String sku;

    // 备注
    @Column
    private String note;

    // 是否删除
    @JsonIgnore
    @Column
    private boolean deleted;

}
