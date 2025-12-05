package com.stu.quantitative.service;

import com.stu.quantitative.entity.TradedEntity;
import com.stu.quantitative.jpa.TradedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradedService {
    @Autowired
    private TradedRepository tradedRepository;

    public List<TradedEntity> findAllByOrderByDate() {
        return this.tradedRepository.findAllByOrderByDate();
    }

    public List<TradedEntity> findAllByStockIdOrderByDate(int stockId) {
        return this.tradedRepository.findAllByStockIdOrderByDate(stockId);
    }

}
