package com.example.demo.controller.api.v1.product;

import com.example.demo.dto.product.request.BookRequest;
import com.example.demo.dto.product.response.BookResponse;
import com.example.demo.service.product.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "APIs for managing books")
public class BookController {

    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Get all books with pagination and filtering",
        description = "Search and filter books with pagination. Results can be sorted by various fields.")
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @Parameter(description = "Search by title, author, ISBN, or description")
            @RequestParam(required = false) String keyword,
            
            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId,
            
            @Parameter(description = "Pagination and sorting parameters. Default sort: createdAt,desc")
            @PageableDefault(
                size = 20,
                sort = "createdAt",
                direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return ResponseEntity.ok(bookService.search(
                keyword,
                categoryId != null ? List.of(categoryId) : null,
                pageable
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a book by ID")
    public ResponseEntity<BookResponse> getBookById(
            @Parameter(description = "ID of the book to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(bookService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new book",
        description = "Create a new book. Requires ADMIN role. Maximum 5 images allowed.")
    @RequestBody(
        description = "Book object that needs to be added to the store",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BookRequest.class)
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Book created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody BookRequest bookRequest) {
        BookResponse response = bookService.create(bookRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing book")
    public ResponseEntity<BookResponse> updateBook(
            @Parameter(description = "ID of the book to update", required = true)
            @PathVariable Long id,
            
            @Valid @RequestBody BookRequest bookRequest) {
        return ResponseEntity.ok(bookService.update(id, bookRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a book")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID of the book to delete", required = true)
            @PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
