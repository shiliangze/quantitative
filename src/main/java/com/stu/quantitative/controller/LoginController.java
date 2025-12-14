package com.stu.quantitative.controller;

import com.stu.quantitative.dto.LoginResponseDto;
import com.stu.quantitative.dto.ResponseDto;
import com.stu.quantitative.dto.UserInfoResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*")
@RestController()
@RequestMapping("/vue-element-admin/user")
public class LoginController {


    @PostMapping("/login")
    public ResponseDto login() {
        return new ResponseDto<LoginResponseDto>(new LoginResponseDto());
    }

    @PostMapping("/logout")
    public ResponseDto logout(@RequestParam("token") String token) {
        return new ResponseDto<String>("success");
    }

    @GetMapping("/info")
    public ResponseDto info(@RequestParam("token") String token) {
        return new ResponseDto<UserInfoResponseDto>(new UserInfoResponseDto());
    }


}
