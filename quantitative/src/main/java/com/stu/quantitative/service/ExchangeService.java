package com.stu.quantitative.service;

import com.stu.quantitative.entity.CodeConfigEntity;
import com.stu.quantitative.entity.ExchangeEntity;
import com.stu.quantitative.jpa.CodeConfigRepository;
import com.stu.quantitative.jpa.ExchangeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExchangeService {
    @Autowired
    private ExchangeRepository exchangeRepository;

    public Optional<ExchangeEntity> findByCodeAndSource(int code, int source){
        return this.exchangeRepository.findByCodeAndSource(code,source);
    }
}
