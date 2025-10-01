package com.example.demo.service.product;

import com.example.demo.exception.product.ProductNotFoundException;
import com.example.demo.model.product.Product;
import com.example.demo.model.product.ProductImage;
import com.example.demo.repository.product.ProductImageRepository;
import com.example.demo.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;
    
    private static final String UPLOAD_DIR = "uploads/products/";

    @Transactional
    public List<String> uploadImages(Long productId, List<MultipartFile> files) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return files.stream()
                .map(file -> {
                    try {
                        String fileName = generateUniqueFileName(file.getOriginalFilename());
                        String filePath = fileStorageService.storeFile(file, UPLOAD_DIR, fileName);
                        
                        ProductImage image = new ProductImage();
                        image.setProduct(product);
                        image.setImageUrl(filePath);
                        productImageRepository.save(image);
                        
                        return filePath;
                    } catch (IOException e) {
                        log.error("Lỗi khi lưu file: {}", e.getMessage(), e);
                        throw new RuntimeException("Không thể lưu file: " + e.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteImage(Long productId, String imageUrl) {
        productImageRepository.deleteByProductIdAndImageUrl(productId, imageUrl);
        
        try {
            fileStorageService.deleteFile(imageUrl);
        } catch (IOException e) {
            log.error("Lỗi khi xóa file: {}", e.getMessage(), e);
            // Không ném ngoại lệ ở đây vì đã xóa khỏi DB
        }
    }

    @Transactional
    public void deleteAllImagesByProductId(Long productId) {
        productImageRepository.deleteByProductId(productId);
        // Lưu ý: Cần xử lý xóa file vật lý nếu cần
    }

    @Transactional
    public void deleteByProductId(Long productId) {
        deleteAllImagesByProductId(productId);
    }

    public List<String> getImageUrlsByProductId(Long productId) {
        return productImageRepository.findByProductId(productId).stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveProductImages(Product product, List<String> imageUrls, boolean isPrimary) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        // If this is the first image, set it as primary
        boolean setPrimary = isPrimary || productImageRepository.countByProductId(product.getId()) == 0;

        for (String imageUrl : imageUrls) {
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);
            productImage.setImageUrl(imageUrl);
            productImage.setThumbnail(setPrimary);
            productImageRepository.save(productImage);
            
            // Only the first image should be primary
            setPrimary = false;
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + fileExtension;
    }

    @Service
    public static class FileStorageService {
        
        public String storeFile(MultipartFile file, String uploadDir, String fileName) throws IOException {
            Path uploadPath = Paths.get(uploadDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            
            return filePath.toString();
        }
        
        public void deleteFile(String filePath) throws IOException {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        }
    }
}
