package com.example.demo.repository.promotion;

import com.example.demo.model.FlashSale;
import com.example.demo.model.FlashSaleItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, Long> {

    // Lấy tất cả sản phẩm trong một đợt flash sale
    List<FlashSaleItem> findByFlashSaleId(Long flashSaleId);

    // Xóa tất cả sản phẩm trong một đợt flash sale
    @Modifying
    @Transactional
    @Query("DELETE FROM FlashSaleItem fsi WHERE fsi.flashSale.id = :flashSaleId")
    void deleteByFlashSaleId(@Param("flashSaleId") Long flashSaleId);

    // Lấy danh sách sản phẩm đang hoạt động trong một đợt flash sale
    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "JOIN FETCH fsi.product p " +
           "JOIN fsi.flashSale fs " +
           "WHERE fs.id = :flashSaleId AND fsi.isActive = true")
    List<FlashSaleItem> findActiveItemsByFlashSaleId(@Param("flashSaleId") Long flashSaleId);

    // Tìm kiếm sản phẩm flash sale đang hoạt động theo ID sản phẩm và thời gian
    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "JOIN fsi.flashSale fs " +
           "WHERE fsi.product.id = :productId " +
           "AND fsi.isActive = true " +
           "AND fs.startTime <= :currentTime " +
           "AND fs.endTime >= :currentTime " +
           "AND (fsi.quantity - fsi.soldQuantity) > 0")
    Optional<FlashSaleItem> findActiveByProductIdAndTime(
            @Param("productId") Long productId,
            @Param("currentTime") LocalDateTime currentTime);

    // Lấy tất cả sản phẩm flash sale đang hoạt động
    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "JOIN FETCH fsi.product p " +
           "JOIN fsi.flashSale fs " +
           "WHERE fsi.isActive = true " +
           "AND fs.startTime <= :currentTime " +
           "AND fs.endTime >= :currentTime " +
           "AND (fsi.quantity - fsi.soldQuantity) > 0")
    List<FlashSaleItem> findAllActive(@Param("currentTime") LocalDateTime currentTime);

    // Lấy tất cả sản phẩm flash sale đang hoạt động có phân trang
    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "JOIN fsi.flashSale fs " +
           "WHERE fs.isActive = true " +
           "AND fs.startTime <= :currentTime " +
           "AND fs.endTime >= :currentTime " +
           "AND fsi.isActive = true " +
           "AND (fsi.quantity - fsi.soldQuantity) > 0")
    Page<FlashSaleItem> findAllActiveFlashSaleItems(
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable);

    // Tìm sản phẩm trong đợt flash sale theo ID sản phẩm và ID flash sale
    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "JOIN FETCH fsi.product p " +
           "JOIN fsi.flashSale fs " +
           "WHERE fs.id = :flashSaleId AND p.id = :productId AND fsi.isActive = true")
    Optional<FlashSaleItem> findByFlashSaleIdAndProductId(
            @Param("flashSaleId") Long flashSaleId,
            @Param("productId") Long productId);

    // Tìm sản phẩm flash sale đang hoạt động theo ID sản phẩm
    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "JOIN fsi.flashSale fs " +
           "WHERE fsi.product.id = :productId " +
           "AND fsi.isActive = true " +
           "AND fs.startTime <= :currentTime " +
           "AND fs.endTime >= :currentTime")
    Optional<FlashSaleItem> findActiveByProductId(
            @Param("productId") Long productId,
            @Param("currentTime") LocalDateTime currentTime);

    // Lấy tất cả sản phẩm flash sale đang hoạt động kèm thông tin sản phẩm
    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "JOIN FETCH fsi.product p " +
           "JOIN fsi.flashSale fs " +
           "WHERE fs.startTime <= :currentTime " +
           "AND fs.endTime >= :currentTime " +
           "AND fs.isActive = true " +
           "AND fsi.isActive = true")
    List<FlashSaleItem> findAllActiveWithProduct(@Param("currentTime") LocalDateTime currentTime);

    // Kiểm tra xem có đợt flash sale nào trùng thời gian không
    @Query("SELECT COUNT(fs) > 0 FROM FlashSale fs " +
           "WHERE fs.isActive = true " +
           "AND fs.startTime <= :endTime " +
           "AND fs.endTime >= :startTime")
    boolean existsByStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndIsActiveTrue(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // Kiểm tra xem có đợt flash sale nào khác trùng thời gian không (trừ một đợt cụ thể)
    @Query("SELECT COUNT(fs) > 0 FROM FlashSale fs " +
           "WHERE fs.id != :excludeId " +
           "AND fs.isActive = true " +
           "AND fs.startTime <= :endTime " +
           "AND fs.endTime >= :startTime")
    boolean existsByIdNotAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndIsActiveTrue(
            @Param("excludeId") Long excludeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // Lấy danh sách đợt flash sale đang hoạt động
    @Query("SELECT fs FROM FlashSale fs " +
           "WHERE fs.isActive = true " +
           "AND fs.startTime <= :now " +
           "AND fs.endTime >= :now")
    List<FlashSale> findActiveFlashSales(@Param("now") LocalDateTime now);

    // Lấy danh sách đợt flash sale sắp diễn ra
    @Query("SELECT fs FROM FlashSale fs " +
           "WHERE fs.isActive = true " +
           "AND fs.startTime > :now " +
           "ORDER BY fs.startTime ASC")
    List<FlashSale> findUpcomingAndActiveFlashSales(@Param("now") LocalDateTime now);
}
