package com.example.demo.repository;

import com.example.demo.model.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {

    @Query("SELECT fs FROM FlashSale fs WHERE fs.isActive = true AND fs.startTime <= :currentTime AND fs.endTime >= :currentTime")
    List<FlashSale> findActiveFlashSales(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT fs FROM FlashSale fs WHERE fs.endTime >= :currentTime ORDER BY fs.startTime ASC")
    List<FlashSale> findUpcomingAndActiveFlashSales(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT COUNT(fs) > 0 FROM FlashSale fs " +
            "WHERE fs.id <> :excludeId " +
            "AND fs.isActive = true " +
            "AND fs.startTime <= :endTime " +
            "AND fs.endTime >= :startTime")
    boolean existsByIdNotAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndIsActiveTrue(
            @Param("excludeId") Long excludeId,
            @Param("endTime") LocalDateTime endTime,
            @Param("startTime") LocalDateTime startTime);

    @Query("SELECT COUNT(fs) > 0 FROM FlashSale fs " +
           "WHERE fs.isActive = true " +
           "AND fs.startTime <= :endTime " +
           "AND fs.endTime >= :startTime")
    boolean existsByStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndIsActiveTrue(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT fs FROM FlashSale fs WHERE fs.endTime < :currentTime AND fs.isActive = true")
    List<FlashSale> findByEndTimeBeforeAndIsActiveTrue(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT fs FROM FlashSale fs WHERE fs.startTime <= :currentTime " +
           "AND fs.endTime > :currentTime AND fs.isActive = false")
    List<FlashSale> findByStartTimeLessThanEqualAndEndTimeAfterAndIsActiveFalse(
            @Param("currentTime") LocalDateTime currentTime
    );
}
