package com.stu.quantitative.controller;

import com.stu.quantitative.dto.ResponseDto;
import com.stu.quantitative.dto.alphavantage.KLineRequestDto;
import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.service.BalanceService;
import com.stu.quantitative.service.PriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin(origins = "*")
@RestController()
@RequestMapping("/balance")
public class BalanceController {
    @GetMapping("/find_by_plan/{planCode}")
    public String backTrack(@PathVariable int planCode) {
        return "success";
    }
}
