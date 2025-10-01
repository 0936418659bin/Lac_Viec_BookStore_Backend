package com.example.demo.dto.product.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookResponse extends BaseProductResponse {
    private String author;
    private String publisher;
    private String isbn;
    private String genre;
    private Integer pageCount;
    private LocalDate publicationDate;
    private String dimensions;
    private String language;
    private Integer weightGrams;
    private String additionalInfo;
}