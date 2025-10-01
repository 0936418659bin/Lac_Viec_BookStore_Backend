package com.example.demo.service.product;

import com.example.demo.dto.product.request.BaseProductRequest;
import com.example.demo.dto.product.request.BookRequest;
import com.example.demo.dto.product.response.BookResponse;
import com.example.demo.exception.common.ResourceNotFoundException;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.model.Category;
import com.example.demo.model.product.Book;
import com.example.demo.model.product.ProductType;
import com.example.demo.repository.product.BookRepository;
import com.example.demo.repository.product.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService implements ProductService<Book, BookResponse> {
    
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper<Book, BookRequest, BookResponse> productMapper;

    @Override
    public ProductType getProductType() {
        return ProductType.BOOK;
    }

    @Override
    @Transactional
    public BookResponse create(BaseProductRequest request) {
        if (!(request instanceof BookRequest bookRequest)) {
            throw new IllegalArgumentException("Invalid request type for Book");
        }
        
        log.info("Creating new book: {}", bookRequest.getName());
        
        Book book = productMapper.toEntity(bookRequest);
        book.setType(ProductType.BOOK);
        
        // Set categories
        if (bookRequest.getCategoryIds() != null && !bookRequest.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(bookRequest.getCategoryIds()));
            book.setCategories(categories);
        }
        
        Book savedBook = bookRepository.save(book);
        log.info("Created book with ID: {}", savedBook.getId());
        
        return productMapper.toResponse(savedBook);
    }

    @Override
    @Transactional
    public BookResponse update(Long id, BaseProductRequest request) {
        if (!(request instanceof BookRequest bookRequest)) {
            throw new IllegalArgumentException("Invalid request type for Book");
        }
        
        log.info("Updating book with ID: {}", id);
        
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));
        
        productMapper.updateFromRequest(bookRequest, existingBook);
        
        // Update categories if provided
        if (bookRequest.getCategoryIds() != null) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(bookRequest.getCategoryIds()));
            existingBook.getCategories().clear();
            existingBook.getCategories().addAll(categories);
        }
        
        Book updatedBook = bookRepository.save(existingBook);
        log.info("Updated book with ID: {}", id);
        
        return productMapper.toResponse(updatedBook);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting book with ID: {}", id);
        
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy sách với ID: " + id);
        }
        
        bookRepository.deleteById(id);
        log.info("Deleted book with ID: {}", id);
    }

    @Override
    public BookResponse getById(Long id) {
        log.debug("Fetching book with ID: {}", id);
        
        return bookRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> getAllProducts(Pageable pageable) {
        log.debug("Fetching all books with pagination");
        return bookRepository.findAll(pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> search(String keyword, List<Long> categoryIds, Pageable pageable) {
        log.debug("Searching books with keyword: {}, categoryIds: {}", keyword, categoryIds);

        // Start with an empty specification using allOf() with no arguments
        Specification<Book> spec = Specification.allOf();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchTerm = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), searchTerm),
                    cb.like(cb.lower(root.get("description")), searchTerm),
                    cb.like(cb.lower(root.get("author")), searchTerm),
                    cb.like(cb.lower(root.get("publisher")), searchTerm)
            ));
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                var join = root.join("categories");
                return join.get("id").in(categoryIds);
            });
        }

        return bookRepository.findAll(spec, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional
    public void updateProductStock(Long productId, int quantity) {
        log.info("Updating stock for book ID: {}, quantity: {}", productId, quantity);
        Book book = bookRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + productId));
        
        int newStock = book.getStockQuantity() + quantity;
        if (newStock < 0) {
            throw new IllegalStateException("Số lượng tồn kho không đủ");
        }
        
        book.setStockQuantity(newStock);
        bookRepository.save(book);
        log.info("Updated stock for book ID: {}, new stock: {}", productId, newStock);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> searchProducts(String keyword, ProductType type, Long categoryId, Pageable pageable) {
        log.debug("Searching books with keyword: {}, type: {}, categoryId: {}", keyword, type, categoryId);
        
        // Ensure we're only searching for books
        if (type != null && type != ProductType.BOOK) {
            return Page.empty(pageable);
        }
        
        List<Long> categoryIds = categoryId != null ? List.of(categoryId) : null;
        return search(keyword, categoryIds, pageable);
    }

    @Override
    public boolean supports(ProductType type) {
        return type == ProductType.BOOK;
    }

    @Override
    @Transactional(readOnly = true)
    public Book getProductEntityById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));
    }
}
