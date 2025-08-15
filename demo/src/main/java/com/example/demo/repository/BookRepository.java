package com.example.demo.repository;

import com.example.demo.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    
    @EntityGraph(attributePaths = {"images", "categories"})
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithImages(@Param("id") Long id);
    
    Optional<Book> findByIsbn(String isbn);
    
    @Query("SELECT b FROM Book b JOIN b.categories c WHERE c.id = :categoryId")
    Page<Book> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.images WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> findByNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT b FROM Book b JOIN b.categories c WHERE c.id = :categoryId AND LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> findByNameContainingIgnoreCaseAndCategoryId(
            @Param("keyword") String keyword, 
            @Param("categoryId") Long categoryId, 
            Pageable pageable);
    boolean existsByIsbn(String isbn);
}
