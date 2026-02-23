package com.stu.quantitative.service;

import com.stu.quantitative.entity.TradedEntity;
import com.stu.quantitative.jpa.TradedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TradedService {
    @Autowired
    private TradedRepository tradedRepository;

    public List<TradedEntity> findAll() {
        return this.tradedRepository.findAll();
    }

    public List<TradedEntity> findAllByStockIdOrderByDate(int stockId) {
        return this.tradedRepository.findAllByStockIdOrderByDate(stockId);
    }

    // 生成资产结余列表
    public Map<Integer,Double> groupToResidual(){
        return this.tradedRepository.findAll().stream()
                .collect(Collectors.toMap(
                        TradedEntity::getStockId,  // key: TradedEntity.id
                        entity -> entity.getDirection() * entity.getQuantity(),  // value: direction * quantity
                        Double::sum  // 合并函数：当 key 冲突时，累加 value
                ));

    }
}
