package com.example.demo.model.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "books")
@PrimaryKeyJoinColumn(name = "product_id")
public class Book extends Product {
    
    @Column(nullable = false, length = 100)
    private String author;

    @Column(length = 100)
    private String publisher;
    
    @Column(length = 20, unique = true)
    private String isbn;
    
    @Column(length = 100)
    private String genre;
    
    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(length = 50)
    private String dimensions;

    @Column(length = 50)
    private String language;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "additional_info", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String additionalInfo;

    public Book() {
        this.setType(ProductType.BOOK);
    }

    // Builder pattern
    public static BookBuilder builder() {
        return new BookBuilder();
    }

    public static class BookBuilder {
        private final Book book;

        public BookBuilder() {
            this.book = new Book();
        }

        public BookBuilder name(String name) {
            book.setName(name);
            return this;
        }

        public BookBuilder author(String author) {
            book.setAuthor(author);
            return this;
        }

        public BookBuilder price(BigDecimal price) {
            book.setPrice(price);
            return this;
        }

        public BookBuilder isbn(String isbn) {
            book.setIsbn(isbn);
            return this;
        }

        public Book build() {
            return book;
        }
    }
}
