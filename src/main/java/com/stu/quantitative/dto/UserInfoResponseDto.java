package com.stu.quantitative.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoResponseDto {
    private String name = "admin";
    private List<String> roles = List.of("admin");
    private String introduction = "I am a super administrator";
    private String avatar = "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif";
}
