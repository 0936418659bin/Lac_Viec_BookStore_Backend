package com.example.demo.controller.api.v1.product;

import com.example.demo.dto.product.request.BookRequest;
import com.example.demo.dto.product.response.BookResponse;
import com.example.demo.service.product.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "APIs for managing books")
public class BookController {

    private final BookService bookService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new book")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<?> createBook(
            HttpServletRequest httpRequest,
            @Valid @org.springframework.web.bind.annotation.RequestBody BookRequest bookRequest) {

        log.info("\n=== INCOMING REQUEST ===");
        log.info("Method: {}", httpRequest.getMethod());
        log.info("Content-Type: {}", httpRequest.getContentType());
        log.info("Request Body: {}", bookRequest.toString());

        try {
            BookResponse response = bookService.create(bookRequest);
            log.info("Book created successfully with ID: {}", response.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating book: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tạo sách: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "Get all books with pagination")
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Fetching books with keyword: {}, categoryId: {}", keyword, categoryId);
        return ResponseEntity.ok(bookService.search(
                keyword,
                categoryId != null ? List.of(categoryId) : null,
                pageable
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a book by ID")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        log.info("Fetching book with ID: {}", id);
        try {
            return ResponseEntity.ok(bookService.getById(id));
        } catch (Exception e) {
            log.error("Error fetching book: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy sách với ID: " + id);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a book")
    public ResponseEntity<?> updateBook(
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody BookRequest bookRequest) {

        log.info("Updating book with ID: {}", id);
        try {
            return ResponseEntity.ok(bookService.update(id, bookRequest));
        } catch (Exception e) {
            log.error("Error updating book: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi khi cập nhật sách: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a book")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        log.info("Deleting book with ID: {}", id);
        try {
            bookService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting book: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa sách: " + e.getMessage());
        }
    }
}