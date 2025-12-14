package com.stu.quantitative.controller;

import com.stu.quantitative.dto.alphavantage.KLineRequestDto;
import com.stu.quantitative.dto.ResponseDto;
import com.stu.quantitative.entity.PriceEntity;
import com.stu.quantitative.service.PriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin(origins = "*")
@RestController()
@RequestMapping("/stock")
public class StockController {

    @Autowired
    private PriceService priceService;


    @PostMapping("/kline")
    public ResponseDto getKLine(@RequestBody KLineRequestDto kLineRequestDto) {
        return new ResponseDto<List<PriceEntity>>(priceService.findByTickerOrderByDate(kLineRequestDto.getTicker()));
    }
}
