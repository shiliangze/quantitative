package com.stu.quantitative.jpa;

import com.stu.quantitative.entity.BalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BalanceRepository extends JpaRepository<BalanceEntity, Integer>, JpaSpecificationExecutor<BalanceEntity> {
        public List<BalanceEntity> findAllByPlanCode(int planCode);
}
