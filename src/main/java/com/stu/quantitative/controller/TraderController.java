package com.stu.quantitative.controller;

import com.stu.quantitative.dto.BackTrackRequestDto;
import com.stu.quantitative.dto.BackTrackResponseDto;
import com.stu.quantitative.dto.ResponseDto;
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
    public ResponseDto<BackTrackResponseDto> backTrack(@RequestBody BackTrackRequestDto backTrackRequestDto) {
        return this.traderService.execute(backTrackRequestDto);
    }
}
