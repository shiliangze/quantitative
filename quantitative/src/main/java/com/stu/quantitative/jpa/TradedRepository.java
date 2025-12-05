package com.stu.quantitative.jpa;

import com.stu.quantitative.entity.TradedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TradedRepository extends JpaRepository<TradedEntity, Integer>, JpaSpecificationExecutor<TradedEntity> {
    public List<TradedEntity> findAllByOrderByDate();
    public List<TradedEntity> findAllByStockIdOrderByDate(int stockId);
}
