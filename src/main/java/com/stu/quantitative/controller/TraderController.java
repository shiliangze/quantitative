package com.stu.quantitative.controller;

import com.stu.quantitative.dto.BackTrackRequestDto;
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

    @PostMapping("/back_track")
    public void backTrack(@RequestBody BackTrackRequestDto backTrackRequestDto) {
        this.traderService.execute(backTrackRequestDto);
        System.out.println(backTrackRequestDto);
    }
}
