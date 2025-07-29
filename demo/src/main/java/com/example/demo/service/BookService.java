package com.example.demo.service;

import com.example.demo.dto.request.BookRequest;
import com.example.demo.dto.response.BookResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Book;
import com.example.demo.model.Category;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(Pageable pageable, String keyword, Long categoryId) {
        Specification<Book> spec = Specification.where(null);
        
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), "%" + keyword.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                )
            );
        }
        
        if (categoryId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.join("categories").get("id"), categoryId)
            );
        }
        
        return bookRepository.findAll(spec, pageable).map(BookResponse::fromBook);
    }

    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        return BookResponse.fromBook(book);
    }

    @Transactional
    public BookResponse createBook(BookRequest bookRequest) {
        // Check if ISBN already exists
        if (bookRepository.existsByIsbn(bookRequest.getIsbn())) {
            throw new IllegalArgumentException("A book with this ISBN already exists");
        }

        Book book = new Book();
        mapRequestToBook(bookRequest, book);
        
        // Save book first to get the ID
        Book savedBook = bookRepository.save(book);
        
        // Update categories
        updateBookCategories(savedBook, bookRequest.getCategoryIds());
        
        return BookResponse.fromBook(savedBook);
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest bookRequest) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
            
        // Check if ISBN is being changed to an existing one
        if (!book.getIsbn().equals(bookRequest.getIsbn()) && 
            bookRepository.existsByIsbn(bookRequest.getIsbn())) {
            throw new IllegalArgumentException("A book with this ISBN already exists");
        }
        
        mapRequestToBook(bookRequest, book);
        updateBookCategories(book, bookRequest.getCategoryIds());
        
        Book updatedBook = bookRepository.save(book);
        return BookResponse.fromBook(updatedBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
            
        // Remove book from categories
        book.getCategories().forEach(category -> category.getBooks().remove(book));
        book.getCategories().clear();
        
        bookRepository.delete(book);
    }

    private void mapRequestToBook(BookRequest request, Book book) {
        book.setTitle(request.getTitle());
        book.setDescription(request.getDescription());
        book.setAuthor(request.getAuthor());
        book.setPublishYear(request.getPublishYear());
        book.setPrice(request.getPrice());
        book.setPages(request.getPages());
        book.setLanguage(request.getLanguage());
        book.setPublisher(request.getPublisher());
        book.setIsbn(request.getIsbn());
        book.setImageUrl(request.getImageUrl());
        book.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
    }

    private void updateBookCategories(Book book, Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            book.getCategories().clear();
            return;
        }

        // Get existing categories
        Set<Category> existingCategories = new HashSet<>(book.getCategories());
        
        // Clear current categories
        book.getCategories().clear();
        
        // Add new categories
        categoryIds.stream()
            .map(categoryId -> categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId)))
            .forEach(book::addCategory);
        
        // Remove book from old categories that are no longer associated
        existingCategories.stream()
            .filter(category -> !book.getCategories().contains(category))
            .forEach(category -> category.getBooks().remove(book));
    }
}
