package com.stu.quantitative.service;

import com.stu.quantitative.entity.BalanceEntity;
import com.stu.quantitative.jpa.BalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BalanceService {
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private StockService stockService;
    @Autowired
    private CodeConfigService codeConfigService;

    public List<BalanceEntity> findAllByPlanCode(int planCode) {
        return this.balanceRepository.findAllByPlanCode(planCode);
    }
}
