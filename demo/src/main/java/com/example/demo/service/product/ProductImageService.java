package com.example.demo.service.product;

import com.example.demo.model.Product;
import com.example.demo.model.ProductImage;
import com.example.demo.repository.product.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductImageService {

    private final ProductImageRepository productImageRepository;

    @Autowired
    public ProductImageService(ProductImageRepository productImageRepository) {
        this.productImageRepository = productImageRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductId(productId);
    }

    @Transactional(readOnly = true)
    public String getProductThumbnailUrl(Long productId) {
        return productImageRepository.findByProductIdAndThumbnailTrue(productId)
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }

    @Transactional
    public void saveProductImages(Product product, List<String> imageUrls, boolean setFirstAsThumbnail) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        // Xóa ảnh cũ nếu có
        productImageRepository.deleteAllByProductId(product.getId());

        // Thêm ảnh mới
        for (int i = 0; i < imageUrls.size(); i++) {
            String imageUrl = imageUrls.get(i);
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl(imageUrl);
            image.setThumbnail(setFirstAsThumbnail && i == 0);
            productImageRepository.save(image);
        }
    }

    @Transactional
    public void setAsThumbnail(Long productId, Long imageId) {
        // Bỏ đặt tất cả ảnh khác làm non-thumbnail
        productImageRepository.findByProductId(productId).forEach(image -> {
            if (!image.getId().equals(imageId)) {
                image.setThumbnail(false);
                productImageRepository.save(image);
            }
        });

        // Đặt ảnh được chọn làm thumbnail
        productImageRepository.findById(imageId).ifPresent(image -> {
            image.setThumbnail(true);
            productImageRepository.save(image);
        });
    }
}
