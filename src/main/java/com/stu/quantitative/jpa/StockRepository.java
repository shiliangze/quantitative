package com.stu.quantitative.jpa;

import com.stu.quantitative.entity.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface StockRepository extends JpaRepository<StockEntity, Integer>, JpaSpecificationExecutor<StockEntity> {
        StockEntity findByTicker(String ticker);
        List<StockEntity> findAllByBalanceId(int balanceId);
}
