package com.stu.quantitative.jpa;

import com.stu.quantitative.entity.PriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<PriceEntity, Integer>, JpaSpecificationExecutor<PriceEntity> {
    Optional<PriceEntity> findTopByOrderByIdDesc();
    Optional<PriceEntity> findTopByStockIdOrderByDateDesc(int stockId);
    List<PriceEntity>  findByStockIdOrderByDate(int stockId);
    boolean existsByStockId(Integer stockId);
    @Query("SELECT p FROM PriceEntity p WHERE p.date = (SELECT MAX(p2.date) FROM PriceEntity p2 WHERE p2.stockId = p.stockId)")
    List<PriceEntity> findLatestPricesForAllStocks();
    List<PriceEntity> findByStockIdOrderByDateDesc(int stockId, Pageable pageable);
}