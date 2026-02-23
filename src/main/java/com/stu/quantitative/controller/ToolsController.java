package com.stu.quantitative.controller;

import com.stu.quantitative.service.dto.ResponseDto;
import com.stu.quantitative.entity.StockEntity;
import com.stu.quantitative.service.SyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin(origins = "*")
@RestController()
@RequestMapping("/tools")
public class ToolsController {
    @Autowired
    private SyncService syncService;

    @GetMapping("/sync/{balanceId}")
    public ResponseDto<List<StockEntity>> syncKlineByBalanceId(@PathVariable int balanceId) {
        List<StockEntity> response = this.syncService.sync(balanceId);
        return new ResponseDto<>(response);
    }
}
