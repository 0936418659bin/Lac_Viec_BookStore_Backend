package com.example.demo.service.product;

import com.example.demo.dto.product.request.CategoryRequest;
import com.example.demo.dto.product.response.CategoryResponse;
import com.example.demo.exception.product.CategoryAlreadyExistsException;
import com.example.demo.exception.product.CategoryNotFoundException;
import com.example.demo.model.Category;
import com.example.demo.repository.product.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findAllByParentIsNull(pageable)
                .map(CategoryResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getParentCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Không tìm thấy danh mục với ID: " + id));
        return CategoryResponse.fromEntity(category);
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Không tìm thấy danh mục với ID: " + id));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new CategoryAlreadyExistsException("Tên danh mục đã tồn tại");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        // Nếu có parentId, set danh mục cha
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException("Không tìm thấy danh mục cha với ID: " + request.getParentId()));
            category.setParent(parent);
        }

        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Không tìm thấy danh mục với ID: " + id));

        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByName(request.getName())) {
            throw new CategoryAlreadyExistsException("Tên danh mục đã tồn tại");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        if (request.getParentId() != null && 
            (category.getParent() == null || !category.getParent().getId().equals(request.getParentId()))) {
            Category newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException("Không tìm thấy danh mục cha với ID: " + request.getParentId()));
            
            if (isCircularReference(category, newParent)) {
                throw new IllegalArgumentException("Không thể đặt danh mục con làm danh mục cha");
            }
            
            category.setParent(newParent);
        } else if (request.getParentId() == null && category.getParent() != null) {
            category.setParent(null);
        }

        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findByIdWithChildren(id)
                .orElseThrow(() -> new CategoryNotFoundException("Không tìm thấy danh mục với ID: " + id));
        
        // Kiểm tra nếu danh mục có sản phẩm
        if (!category.getProducts().isEmpty()) {
            throw new IllegalStateException("Không thể xóa danh mục đang chứa sản phẩm");
        }
        
        // Nếu là danh mục cha, xóa tất cả danh mục con
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            categoryRepository.deleteAll(category.getChildren());
        }
        
        categoryRepository.delete(category);
    }

    // Kiểm tra tham chiếu vòng tròn trong danh mục
    private boolean isCircularReference(Category category, Category potentialParent) {
        if (potentialParent == null) {
            return false;
        }
        
        Category current = potentialParent.getParent();
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
