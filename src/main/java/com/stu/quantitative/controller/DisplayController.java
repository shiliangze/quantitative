package com.stu.quantitative.controller;

import com.stu.quantitative.entity.MarketEntity;
import com.stu.quantitative.service.DisplayService;
import com.stu.quantitative.service.MarketService;
import com.stu.quantitative.service.dto.ResponseDto;
import com.stu.quantitative.service.dto.balance.BalanceResponseData;
import com.stu.quantitative.service.dto.balance.BalanceResponseDto;
import com.stu.quantitative.service.dto.recommend.IRecommendedResponseDto;
import com.stu.quantitative.service.dto.recommend.RecommendedRequestDto;
import com.stu.quantitative.service.dto.traceBack.TraceBackRequestDto;
import com.stu.quantitative.service.dto.traceBack.TraceBackResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// 数据展示控制器

@Slf4j
@CrossOrigin(origins = "*")
@RestController()
@RequestMapping("/display")
public class DisplayController {
    @Autowired
    private MarketService marketService;
    @Autowired
    private DisplayService displayService;

    @GetMapping("/find_all_market")
    public ResponseDto<List<MarketEntity>> findAllMarket() {
        return new ResponseDto<>(this.marketService.findAll());
    }

    @GetMapping("/find_all_balance")
    public ResponseDto<BalanceResponseDto> findAll() {
        return new ResponseDto<>(this.displayService.findAllBalance());
    }
    // 计算阈值
    @PostMapping("/recommended")
    public ResponseDto<List<IRecommendedResponseDto>> threshold(@RequestBody RecommendedRequestDto recommendedRequestDto) {
        return new ResponseDto<>(this.displayService.threshold(recommendedRequestDto));
    }
    @PostMapping("/trace_back")
    public ResponseDto<TraceBackResponseDto> traceBack(@RequestBody TraceBackRequestDto traceBackRequestDto) {
        return new ResponseDto<>(this.displayService.traceBack(traceBackRequestDto));
    }
}
