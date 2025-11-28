package com.stu.quantitative.controller;

import com.stu.quantitative.service.TraderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*")
@RestController()
@RequestMapping("/trader")
public class TraderController {

    @Autowired
    private TraderService traderService;

    @GetMapping("/back_track/{planCode}")
    public void backTrack(@PathVariable int planCode) {
         this.traderService.execute(planCode);
    }
}
