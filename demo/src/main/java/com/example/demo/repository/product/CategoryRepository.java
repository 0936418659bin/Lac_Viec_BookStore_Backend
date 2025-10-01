package com.example.demo.repository.product;

import com.example.demo.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    boolean existsByName(String name);
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :id")
    Optional<Category> findByIdWithChildren(@Param("id") Long id);
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products WHERE c.id = :id")
    Optional<Category> findByIdWithProducts(@Param("id") Long id);
    
    List<Category> findByParentIsNull();
    
    List<Category> findByParentId(Long parentId);
    
    Page<Category> findAllByParentIsNull(Pageable pageable);
    
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.children")
    List<Category> findWithChildren();
}
