package com.stu.quantitative.jpa;

import com.stu.quantitative.entity.MarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MarketRepository extends JpaRepository<MarketEntity, Integer>, JpaSpecificationExecutor<MarketEntity> {
}
