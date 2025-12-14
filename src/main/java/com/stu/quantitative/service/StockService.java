package com.stu.quantitative.service;

import com.stu.quantitative.entity.StockEntity;
import com.stu.quantitative.jpa.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {
    @Autowired
    private StockRepository stockRepository;

    public List<StockEntity> findAll() {
        return stockRepository.findAll();
    }

    public StockEntity findById(int id) {
        return this.stockRepository.findById(id).get();
    }
}
