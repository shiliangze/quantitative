package com.stu.quantitative.service;

import com.stu.quantitative.entity.MarketEntity;
import com.stu.quantitative.jpa.MarketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketService {
    @Autowired
    private MarketRepository marketRepository;

    public List<MarketEntity> findAll(){
        return this.marketRepository.findAll();
    }

    public MarketEntity findById(int id){
        return this.marketRepository.findById(id).get();
    }

}
