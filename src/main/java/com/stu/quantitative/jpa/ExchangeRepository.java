package com.stu.quantitative.jpa;

import com.stu.quantitative.entity.ExchangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ExchangeRepository extends JpaRepository<ExchangeEntity, Integer>, JpaSpecificationExecutor<ExchangeEntity> {
        public Optional<ExchangeEntity> findByCodeAndSource(int code,int source);
}
