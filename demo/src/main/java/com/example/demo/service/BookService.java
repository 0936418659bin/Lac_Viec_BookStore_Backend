package com.example.demo.service;

import com.example.demo.dto.request.BookRequest;
import com.example.demo.dto.response.BookResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Book;
import com.example.demo.model.Category;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductImageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final CategoryService categoryService;
    private final ProductImageService productImageService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(Pageable pageable, String keyword, Long categoryId) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (categoryId != null) {
                return bookRepository.findByNameContainingIgnoreCaseAndCategoryId(keyword, categoryId, pageable)
                        .map(BookResponse::fromBook);
            }
            return bookRepository.findByNameContainingIgnoreCase(keyword, pageable)
                    .map(BookResponse::fromBook);
        } else if (categoryId != null) {
            return bookRepository.findByCategoryId(categoryId, pageable)
                    .map(BookResponse::fromBook);
        }
        return bookRepository.findAll(pageable)
                .map(BookResponse::fromBook);
    }

    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findByIdWithImages(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return BookResponse.fromBook(book);
    }

    @Transactional
    public BookResponse createBook(BookRequest request) {
        // Tạo mới sách
        Book book = new Book();
        updateBookFromRequest(book, request);
        
        // Lưu sách để có ID trước
        Book savedBook = bookRepository.save(book);
        
        // Xử lý ảnh nếu có
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            // Xóa tất cả ảnh cũ nếu có
            savedBook.getImages().clear();
            
            // Thêm ảnh mới (chỉ thêm mỗi ảnh một lần)
            Set<String> uniqueImageUrls = new LinkedHashSet<>(request.getImageUrls());
            int index = 0;
            for (String imageUrl : uniqueImageUrls) {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    savedBook.addImage(imageUrl.trim(), index == 0); // Ảnh đầu tiên là thumbnail
                    index++;
                }
            }
            
            // Lưu lại sách với ảnh đã cập nhật
            savedBook = bookRepository.save(savedBook);
        }
        
        return BookResponse.fromBook(savedBook);
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findByIdWithImages(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));

        updateBookFromRequest(book, request);

        // Xử lý ảnh nếu có
        if (request.getImageUrls() != null) {
            // Xóa tất cả ảnh cũ
            book.getImages().clear();

            // Thêm ảnh mới (chỉ thêm mỗi ảnh một lần)
            Set<String> uniqueImageUrls = new LinkedHashSet<>(request.getImageUrls());
            int index = 0;
            for (String imageUrl : uniqueImageUrls) {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    book.addImage(imageUrl.trim(), index == 0); // Ảnh đầu tiên là thumbnail
                    index++;
                }
            }
        } else if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            // Backward compatibility
            book.getImages().clear();
            book.addImage(request.getImageUrl().trim(), true);
        }

        Book updatedBook = bookRepository.save(book);
        return BookResponse.fromBook(updatedBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));
        
        // Xóa quan hệ với danh mục trước khi xóa sách
        book.getCategories().forEach(category -> category.getProducts().remove(book));
        book.getCategories().clear();
        
        bookRepository.delete(book);
    }

    @Transactional
    public Book saveBook(Book book, List<String> imageUrls) {
        // Lưu sách
        Book savedBook = bookRepository.save(book);
        
        // Lưu ảnh sản phẩm (tự động xóa ảnh cũ nếu có)
        if (imageUrls != null && !imageUrls.isEmpty()) {
            productImageService.saveProductImages(book, imageUrls, true);
        }
        
        return savedBook;
    }

    @Transactional(readOnly = true)
    public Book getBookWithImages(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        
        // Load ảnh (nếu cần)
        book.getImages().size(); // Force load lazy collection
        
        return book;
    }

    private void updateBookFromRequest(Book book, BookRequest request) {
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setPrice(request.getPrice());
        book.setStockQuantity(request.getStockQuantity());
        book.setPublisher(request.getPublisher());
        book.setGenre(request.getGenre());
        book.setPageCount(request.getPageCount());
        book.setPublicationDate(request.getPublicationDate());
        book.setDimensions(request.getDimensions());
        book.setWeightGrams(request.getWeightGrams());
        
        // Xử lý additionalInfo dưới dạng String
        if (request.getAdditionalInfo() != null) {
            try {
                // Kiểm tra xem có phải là JSON hợp lệ không
                objectMapper.readTree(request.getAdditionalInfo());
                book.setAdditionalInfo(request.getAdditionalInfo());
            } catch (Exception e) {
                // Nếu không phải JSON hợp lệ, lưu dưới dạng string thông thường
                book.setAdditionalInfo("\"" + request.getAdditionalInfo() + "\"");
            }
        } else {
            book.setAdditionalInfo(null);
        }
        
        // Update categories if provided
        if (request.getCategoryIds() != null) {
            updateBookCategories(book, request.getCategoryIds());
        }
    }

    private void updateBookCategories(Book book, Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        // Xóa các danh mục cũ không còn trong danh sách mới
        book.getCategories().removeIf(category -> !categoryIds.contains(category.getId()));

        // Thêm các danh mục mới
        Set<Long> existingCategoryIds = book.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        List<Category> newCategories = categoryIds.stream()
                .filter(id -> !existingCategoryIds.contains(id))
                .map(categoryId -> categoryService.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + categoryId)))
                .toList();

        newCategories.forEach(book::addCategory);
    }
}
