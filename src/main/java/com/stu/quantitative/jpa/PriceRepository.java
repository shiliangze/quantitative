package com.stu.quantitative.jpa;

import com.stu.quantitative.entity.PriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<PriceEntity, Integer>, JpaSpecificationExecutor<PriceEntity> {
    Optional<PriceEntity> findTopByOrderByIdDesc();
    Optional<PriceEntity> findTopByStockIdOrderByDateDesc(int stockId);
    List<PriceEntity>  findByStockIdOrderByDate(int stockId);
    boolean existsByStockIdAndDate(Integer stockId, LocalDate date);
    boolean existsByStockId(Integer stockId);
}