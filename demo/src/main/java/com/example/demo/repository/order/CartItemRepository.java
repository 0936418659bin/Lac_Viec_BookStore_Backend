package com.example.demo.repository.order;

import com.example.demo.model.Cart;
import com.example.demo.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Tìm tất cả các mục trong giỏ hàng theo ID giỏ hàng
     */
    List<CartItem> findByCartId(Long cartId);
    
    /**
     * Tìm tất cả các mục trong giỏ hàng cùng với thông tin sản phẩm và ảnh
     */
    @Query("SELECT ci FROM CartItem ci " +
           "JOIN FETCH ci.product p " +
           "LEFT JOIN FETCH p.images " +
           "WHERE ci.cart.id = :cartId")
    List<CartItem> findByCartIdWithProduct(@Param("cartId") Long cartId);

    /**
     * Tìm một mục trong giỏ hàng theo ID giỏ hàng và ID sản phẩm
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    
    /**
     * Tìm một mục trong giỏ hàng theo ID giỏ hàng, ID sản phẩm và trạng thái flash sale
     */
    @Query("SELECT ci FROM CartItem ci " +
           "WHERE ci.cart.id = :cartId " +
           "AND ci.product.id = :productId " +
           "AND ci.isFlashSale = :isFlashSale")
    Optional<CartItem> findByCartIdAndProductIdAndIsFlashSale(
            @Param("cartId") Long cartId, 
            @Param("productId") Long productId,
            @Param("isFlashSale") boolean isFlashSale);

    /**
     * Xóa tất cả các mục trong giỏ hàng theo ID giỏ hàng
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    int deleteAllByCartId(@Param("cartId") Long cartId);

    /**
     * Xóa một mục trong giỏ hàng theo ID giỏ hàng và ID mục
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.id = :itemId AND ci.cart.id = :cartId")
    int deleteByIdAndCartId(@Param("itemId") Long itemId, @Param("cartId") Long cartId);

    /**
     * Đếm số lượng sản phẩm trong giỏ hàng
     */
    int countByCart(Cart cart);
    
    /**
     * Đếm số lượng sản phẩm trong giỏ hàng (tổng số lượng)
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    int sumQuantityByCartId(@Param("cartId") Long cartId);
    
    /**
     * Đếm số lượng sản phẩm flash sale trong giỏ hàng của người dùng
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci " +
           "JOIN ci.cart c " +
           "WHERE c.user.id = :userId AND ci.isFlashSale = true")
    int countFlashSaleItemsByUserId(@Param("userId") Long userId);

    /**
     * Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
     */
    boolean existsByCartIdAndProductId(Long cartId, Long productId);
    
    /**
     * Kiểm tra xem sản phẩm flash sale đã có trong giỏ hàng chưa
     */
    boolean existsByCartIdAndProductIdAndIsFlashSale(Long cartId, Long productId, boolean isFlashSale);
    
    /**
     * Tìm tất cả các mục flash sale trong giỏ hàng
     */
    @Query("SELECT ci FROM CartItem ci " +
           "JOIN FETCH ci.product p " +
           "LEFT JOIN FETCH p.images " +
           "WHERE ci.cart.id = :cartId AND ci.isFlashSale = true")
    List<CartItem> findFlashSaleItemsByCartId(@Param("cartId") Long cartId);
}
