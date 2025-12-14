package com.stu.quantitative.dto.tsanghi;

import lombok.Getter;

import java.util.List;
@Getter
public class ResponseVo {
    private int code;
    private String msg;
    private List<ResponseDataVo> data;
}
