package com.example.demo.dto.product.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookRequest extends BaseProductRequest {
    @NotBlank(message = "Tác giả không được để trống")
    @Size(max = 100, message = "Tên tác giả không được vượt quá 100 ký tự")
    private String author;

    @Size(max = 100, message = "Nhà xuất bản không được vượt quá 100 ký tự")
    private String publisher;

    @NotBlank(message = "ISBN không được để trống")
    @Pattern(regexp = "^(?=(?:\\D*\\d){10}(?:(?:\\D*\\d){3})?$)[\\d-]+",
            message = "ISBN không hợp lệ")
    private String isbn;

    @Size(max = 100, message = "Thể loại không được vượt quá 100 ký tự")
    private String genre;

    @PositiveOrZero(message = "Số trang phải là số dương")
    private Integer pageCount;

    private LocalDate publicationDate;

    @Size(max = 50, message = "Kích thước không được vượt quá 50 ký tự")
    private String dimensions;

    @Size(max = 50, message = "Ngôn ngữ không được vượt quá 50 ký tự")
    private String language;

    @PositiveOrZero(message = "Trọng lượng phải là số dương")
    private Integer weightGrams;

    private String additionalInfo;
}