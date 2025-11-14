package com.stu.quantitative.service;

import com.stu.quantitative.entity.CodeConfigEntity;
import com.stu.quantitative.jpa.CodeConfigRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodeConfigService {
    @Autowired
    private CodeConfigRepository codeConfigRepository;

    private List<CodeConfigEntity> configs;


    @PostConstruct
    public void init() {
        // Spring加载完@Autowired后自动执行，查询结果并赋值
        this.configs = codeConfigRepository.findAll();
    }

    public List<CodeConfigEntity> findAllBySku(String sku) {
        return this.configs.stream()
                .filter(it -> it.getSku().equals(sku))
                .toList();
    }

    public CodeConfigEntity findBySkuAndCode(String sku,int tokenCode) {
        return this.configs.stream()
                .filter(it -> it.getCode() == tokenCode && it.getSku().equals(sku))
                .findFirst().orElse(null);
    }
}
