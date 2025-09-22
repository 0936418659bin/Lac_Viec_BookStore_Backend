package com.example.demo.repository.order;

import com.example.demo.entity.User;
import com.example.demo.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Tìm giỏ hàng theo người dùng
     */
    Optional<Cart> findByUser(User user);

    /**
     * Kiểm tra xem người dùng đã có giỏ hàng chưa
     */
    boolean existsByUser(User user);
    
    /**
     * Tìm giỏ hàng cùng với các mục hàng và sản phẩm liên quan
     * Sử dụng JOIN FETCH để tối ưu truy vấn, tránh N+1
     */
    @Query("SELECT DISTINCT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.product p " +
           "LEFT JOIN FETCH p.images " +
           "WHERE c.user = :user")
    Optional<Cart> findByUserWithItems(@Param("user") User user);
    
    /**
     * Tìm giỏ hàng theo ID cùng với các mục hàng và sản phẩm liên quan
     */
    @Query("SELECT DISTINCT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.product p " +
           "LEFT JOIN FETCH p.images " +
           "WHERE c.id = :cartId")
    Optional<Cart> findByIdWithItems(@Param("cartId") Long cartId);
    
    /**
     * Đếm số lượng sản phẩm trong giỏ hàng của người dùng
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM Cart c JOIN c.items ci WHERE c.user = :user")
    int countItemsByUser(@Param("user") User user);
}
