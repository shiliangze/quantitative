package com.stu.quantitative.service;

import com.stu.quantitative.entity.BalanceEntity;
import com.stu.quantitative.jpa.BalanceRepository;
import com.stu.quantitative.service.domain.B0;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BalanceService {
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private StockService stockService;
    @Autowired
    private CodeConfigService codeConfigService;

    public List<B0> findAccountAllByPlanCode(int planCode) {
        Map<Integer, List<BalanceEntity>> balances = this.balanceRepository.findAllByPlanCode(planCode).stream().collect(Collectors.groupingBy(BalanceEntity::getInvestCode));
        return balances.keySet().stream().map(it ->
                new B0(this.codeConfigService.findBySkuAndCode("investName", it).getValue(),
                        balances.get(it).getFirst().getShare(),
                        balances.get(it).stream().map(be -> this.stockService.findById(be.getStockId())).toList()
                )).toList();
    }

    public List<BalanceEntity> findAllByPlanCode(int planCode) {
        return this.balanceRepository.findAllByPlanCode(planCode);

    }


}
