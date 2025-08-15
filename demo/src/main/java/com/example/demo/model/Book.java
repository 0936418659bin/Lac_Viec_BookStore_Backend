package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "books")
@PrimaryKeyJoinColumn(name = "product_id")
public class Book extends Product {
    @Transient
    private String title; // Sử dụng transient vì đã có name từ Product

    @Column(nullable = false, length = 100)
    private String author;

    @Column(length = 100)
    private String publisher;
    
    @Column(length = 20)
    private String isbn;
    
    @Column(length = 100)
    private String genre;
    
    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(length = 50)
    private String dimensions;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "additional_info", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String additionalInfo;

    public Book() {
        super();
        this.setType(ProductType.BOOK);
    }

    @PostLoad
    private void syncWithProduct() {
        // Đồng bộ name từ Product sang title
        if (this.getName() != null) {
            this.title = this.getName();
        }
    }

    @PrePersist
    @PreUpdate
    public void prePersist() {
        // Đồng bộ title vào name của Product trước khi lưu
        if (this.title != null) {
            this.setName(this.title);
        }
    }

    // Các phương thức tiện ích
    public void addCategory(Category category) {
        if (category != null) {
            this.getCategories().add(category);
            category.getProducts().add(this);
        }
    }

    public void removeCategory(Category category) {
        if (category != null) {
            this.getCategories().remove(category);
            category.getProducts().remove(this);
        }
    }
}
