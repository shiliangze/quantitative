package com.stu.quantitative.dto;

import lombok.Data;

@Data
public class ResponseDto<T> {
    private Integer code = 20000;
    private final T data;
    public ResponseDto(T data) {
        this.data = data;
    }
}
