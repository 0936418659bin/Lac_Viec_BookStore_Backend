package com.example.demo.service.order;

import com.example.demo.dto.order.request.AddToCartRequest;
import com.example.demo.dto.order.response.CartItemDTO;
import com.example.demo.dto.order.response.CartSummaryDTO;
import com.example.demo.dto.promotion.response.FlashSaleItemDTO;
import com.example.demo.entity.User;
import com.example.demo.exception.common.ResourceNotFoundException;
import com.example.demo.model.Cart;
import com.example.demo.model.CartItem;
import com.example.demo.model.product.Product;
import com.example.demo.repository.order.CartItemRepository;
import com.example.demo.repository.order.CartRepository;
import com.example.demo.repository.product.ProductRepository;
import com.example.demo.repository.auth.UserRepository;
import com.example.demo.service.promotion.FlashSaleService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FlashSaleService flashSaleService;
    private final ModelMapper modelMapper;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int MAX_ITEMS_PER_CART = 50;

    @Transactional(readOnly = true)
    public List<CartItemDTO> getCartItems() {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);

        return cartItemRepository.findByCartIdWithProduct(cart.getId()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Retryable(value = ObjectOptimisticLockingFailureException.class,
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 100))
    public CartItemDTO addToCart(AddToCartRequest request) {
        validateAddToCartRequest(request);
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);

        if (cartItemRepository.countByCart(cart) >= MAX_ITEMS_PER_CART) {
            throw new IllegalStateException("Giỏ hàng đã đạt số lượng sản phẩm tối đa (" + MAX_ITEMS_PER_CART + ")");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + request.getProductId()));

        // Check for flash sale product
        if (request.getIsFlashSaleItem()) {
            FlashSaleItemDTO flashSaleItem = flashSaleService.checkProductInFlashSale(request.getProductId());
            if (flashSaleItem == null || !flashSaleItem.getActive()) {
                throw new IllegalStateException("Sản phẩm này không còn trong đợt flash sale");
            }
            
            // Check remaining quantity in flash sale
            int remainingQuantity = flashSaleService.getRemainingQuantity(request.getProductId());
            if (remainingQuantity < request.getQuantity()) {
                throw new IllegalStateException("Số lượng sản phẩm flash sale còn lại không đủ. Còn lại: " + remainingQuantity);
            }
            
            // Update sold quantity in flash sale
            flashSaleService.updateSoldQuantity(request.getProductId(), request.getQuantity());
            request.setPrice(flashSaleItem.getSalePrice());
        } 
        // Check regular stock
        else if (product.getStockQuantity() < request.getQuantity()) {
            throw new IllegalStateException("Số lượng tồn kho không đủ cho sản phẩm: " + product.getName());
        }

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductIdAndIsFlashSale(cart.getId(), product.getId(), request.getIsFlashSaleItem());

        if (existingItem.isPresent()) {
            return updateExistingCartItem(existingItem.get(), request.getQuantity(), product);
        } else {
            return createNewCartItem(cart, product, request);
        }
    }

    private CartItemDTO updateExistingCartItem(CartItem existingItem, int additionalQuantity, Product product) {
        int newQuantity = existingItem.getQuantity() + additionalQuantity;

        if (product.getStockQuantity() < newQuantity) {
            throw new IllegalStateException("Số lượng tồn kho không đủ cho sản phẩm: " + product.getName() +
                    ". Yêu cầu: " + newQuantity + ", Có sẵn: " + product.getStockQuantity());
        }

        existingItem.setQuantity(Integer.valueOf(newQuantity));
        cartItemRepository.save(existingItem);

        log.info("Đã cập nhật số lượng sản phẩm trong giỏ hàng. CartItem ID: {}, Số lượng mới: {}",
                existingItem.getId(), Integer.valueOf(newQuantity));

        return convertToDto(existingItem);
    }

    private CartItemDTO createNewCartItem(Cart cart, Product product, AddToCartRequest request) {
        if (request.getIsFlashSaleItem()) {
            validateFlashSaleItem(product, request.getQuantity());
        }

        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProduct(product);
        newItem.setQuantity(request.getQuantity());
        newItem.setPrice(calculateItemPrice(product, request));
        newItem.setIsFlashSale(request.getIsFlashSaleItem());

        if (request.getIsFlashSaleItem()) {
            newItem.setOriginalPrice(product.getPrice());
            newItem.setDiscountAmount(product.getPrice().subtract(newItem.getPrice()));
        }

        cartItemRepository.save(newItem);

        log.info("Đã thêm sản phẩm mới vào giỏ hàng. Product ID: {}, Số lượng: {}",
                product.getId(), request.getQuantity());

        return convertToDto(newItem);
    }

    private BigDecimal calculateItemPrice(Product product, AddToCartRequest request) {
        if (request.getIsFlashSaleItem()) {
            FlashSaleItemDTO flashSaleItem = flashSaleService.checkProductInFlashSale(product.getId());
            if (flashSaleItem != null) {
                return flashSaleItem.getSalePrice();
            }
        }
        return product.getPrice();
    }

    private void validateFlashSaleItem(Product product, int quantity) {
        FlashSaleItemDTO flashSaleItem = flashSaleService.checkProductInFlashSale(product.getId());
        if (flashSaleItem == null || !flashSaleItem.getActive()) {
            throw new IllegalStateException("Không tìm thấy chương trình khuyến mãi cho sản phẩm: " + product.getName());
        }

        if (flashSaleItem.getMaxQuantityPerUser() < quantity) {
            throw new IllegalStateException("Số lượng tối đa cho mỗi khách hàng là " +
                    flashSaleItem.getMaxQuantityPerUser() + " sản phẩm");
        }

        if (flashSaleItem.getRemainingQuantity() < quantity) {
            throw new IllegalStateException("Số lượng sản phẩm khuyến mãi còn lại không đủ");
        }
    }

    @Transactional
    @Retryable(value = ObjectOptimisticLockingFailureException.class,
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 100))
    public CartItemDTO updateCartItemQuantity(Long itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng với ID: " + itemId));

        validateCartItemOwnership(item);

        Product product = item.getProduct();
        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Số lượng tồn kho không đủ. Có sẵn: " + product.getStockQuantity());
        }

        item.setQuantity(Integer.valueOf(quantity));
        cartItemRepository.save(item);

        log.info("Đã cập nhật số lượng sản phẩm. CartItem ID: {}, Số lượng mới: {}", itemId, Integer.valueOf(quantity));

        return convertToDto(item);
    }

    @Transactional
    public void removeFromCart(Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng với ID: " + itemId));

        validateCartItemOwnership(item);

        cartItemRepository.delete(item);
        log.info("Đã xóa sản phẩm khỏi giỏ hàng. CartItem ID: {}", itemId);
    }

    @Transactional
    public void clearCart() {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);

        int deletedCount = cartItemRepository.deleteAllByCartId(cart.getId());
        log.info("Đã xóa toàn bộ " + deletedCount + " sản phẩm khỏi giỏ hàng. User ID: " + currentUser.getId());
    }

    @Transactional
    @Retryable(value = ObjectOptimisticLockingFailureException.class,
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 100))
    public void checkout() {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Không thể thanh toán giỏ hàng trống");
        }

        validateCartBeforeCheckout(cart);
        updateInventoryAndProcessCheckout(cart);

        cartItemRepository.deleteAllByCartId(cart.getId());
        log.info("Đã thanh toán thành công giỏ hàng. User ID: " + currentUser.getId() +
                ", Số sản phẩm: " + cart.getItems().size());
    }

    private void validateCartBeforeCheckout(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Số lượng tồn kho không đủ cho sản phẩm: " +
                        product.getName() + ". Yêu cầu: " + item.getQuantity() + ", Có sẵn: " + product.getStockQuantity());
            }

            if (item.getIsFlashSale()) {
                validateFlashSaleItem(product, item.getQuantity());
            }
        }
    }

    private void updateInventoryAndProcessCheckout(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            int newStock = product.getStockQuantity() - item.getQuantity();
            product.setStockQuantity(Integer.valueOf(newStock));

            if (item.getIsFlashSale()) {
                flashSaleService.updateSoldQuantity(product.getId(), item.getQuantity());
            }

            productRepository.save(product);
        }
    }

    @Transactional(readOnly = true)
    public CartSummaryDTO getCartSummary() {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);

        int totalItems = cartItemRepository.sumQuantityByCartId(cart.getId());
        List<CartItemDTO> items = cartItemRepository.findByCartIdWithProduct(cart.getId())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new CartSummaryDTO(items);
    }

    // Helper methods
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    private void validateCartItemOwnership(CartItem item) {
        if (!item.getCart().getUser().getId().equals(getCurrentUser().getId())) {
            throw new SecurityException("Bạn không có quyền thực hiện thao tác này");
        }
    }

    private void validateAddToCartRequest(AddToCartRequest request) {
        if (request == null || request.getProductId() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Thông tin yêu cầu không hợp lệ");
        }
    }

    private CartItemDTO convertToDto(CartItem item) {
        CartItemDTO dto = modelMapper.map(item, CartItemDTO.class);
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductImage(item.getProduct().getImageUrl());
        dto.setSubTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        return dto;
    }
}