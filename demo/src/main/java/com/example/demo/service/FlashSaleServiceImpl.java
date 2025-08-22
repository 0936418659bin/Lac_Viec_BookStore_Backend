package com.example.demo.service;

import com.example.demo.dto.FlashSaleDTO;
import com.example.demo.dto.FlashSaleItemDTO;
import com.example.demo.dto.request.FlashSaleRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.FlashSale;
import com.example.demo.model.FlashSaleItem;
import com.example.demo.model.Product;
import com.example.demo.repository.FlashSaleItemRepository;
import com.example.demo.repository.FlashSaleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashSaleServiceImpl implements FlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final ProductService productService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public FlashSaleDTO createFlashSale(FlashSaleRequest request) {
        validateFlashSaleTime(request.getStartTime(), request.getEndTime(), null);

        FlashSale flashSale = new FlashSale();
        modelMapper.map(request, flashSale);

        FlashSale savedFlashSale = flashSaleRepository.save(flashSale);

        // Lưu các sản phẩm trong đợt flash sale
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (FlashSaleRequest.FlashSaleItemRequest itemRequest : request.getItems()) {
                addItemToFlashSale(savedFlashSale.getId(), itemRequest);
            }
        }

        return convertToDTO(savedFlashSale);
    }

    @Override
    @Transactional
    public FlashSaleDTO updateFlashSale(Long id, FlashSaleRequest request) {
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt flash sale với ID: " + id));

        validateFlashSaleTime(request.getStartTime(), request.getEndTime(), id);

        modelMapper.map(request, flashSale);
        FlashSale updatedFlashSale = flashSaleRepository.save(flashSale);

        return convertToDTO(updatedFlashSale);
    }

    @Override
    public FlashSaleDTO getFlashSaleById(Long id) {
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt flash sale với ID: " + id));

        FlashSaleDTO dto = convertToDTO(flashSale);

        // Lấy danh sách sản phẩm trong đợt flash sale
        List<FlashSaleItem> items = flashSaleItemRepository.findByFlashSaleId(id);
        List<FlashSaleItemDTO> itemDTOs = items.stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());

        dto.setItems(itemDTOs);

        return dto;
    }

    @Override
    public Page<FlashSaleDTO> getAllFlashSales(Pageable pageable) {
        Page<FlashSale> flashSales = flashSaleRepository.findAll(pageable);

        List<FlashSaleDTO> dtos = flashSales.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, flashSales.getTotalElements());
    }

    @Override
    @Transactional
    public void deleteFlashSale(Long id) {
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt flash sale với ID: " + id));

        // Xóa tất cả các sản phẩm trong đợt flash sale
        flashSaleItemRepository.deleteByFlashSaleId(id);

        // Xóa đợt flash sale
        flashSaleRepository.delete(flashSale);
    }

    @Override
    public List<FlashSaleDTO> getActiveFlashSales() {
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> activeFlashSales = flashSaleRepository.findActiveFlashSales(now);

        return activeFlashSales.stream()
                .map(flashSale -> {
                    FlashSaleDTO dto = convertToDTO(flashSale);
                    // Tính thời gian còn lại (giây)
                    long secondsRemaining = Duration.between(now, flashSale.getEndTime()).getSeconds();
                    dto.setTimeRemainingInSeconds(secondsRemaining > 0 ? secondsRemaining : 0);

                    // Lấy danh sách sản phẩm trong đợt flash sale
                    List<FlashSaleItem> items = flashSaleItemRepository.findActiveItemsByFlashSaleId(flashSale.getId());
                    List<FlashSaleItemDTO> itemDTOs = items.stream()
                            .map(this::convertToItemDTO)
                            .collect(Collectors.toList());

                    dto.setItems(itemDTOs);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<FlashSaleDTO> getUpcomingFlashSales() {
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> upcomingFlashSales = flashSaleRepository.findUpcomingAndActiveFlashSales(now);

        return upcomingFlashSales.stream()
                .map(flashSale -> {
                    FlashSaleDTO dto = convertToDTO(flashSale);
                    // Tính thời gian còn lại đến khi bắt đầu (giây)
                    long secondsRemaining = Duration.between(now, flashSale.getStartTime()).getSeconds();
                    dto.setTimeRemainingInSeconds(secondsRemaining > 0 ? secondsRemaining : 0);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FlashSaleItemDTO addItemToFlashSale(Long flashSaleId, FlashSaleRequest.FlashSaleItemRequest itemRequest) {
        FlashSale flashSale = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt flash sale với ID: " + flashSaleId));

        // Kiểm tra thời gian flash sale
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(flashSale.getStartTime()) || now.isAfter(flashSale.getEndTime())) {
            throw new BadRequestException("Không thể thêm sản phẩm vào đợt flash sale đã kết thúc hoặc chưa bắt đầu");
        }

        // Kiểm tra sản phẩm đã tồn tại trong đợt flash sale chưa
        Optional<FlashSaleItem> existingItem = flashSaleItemRepository.findByFlashSaleIdAndProductId(
                flashSaleId, itemRequest.getProductId());

        if (existingItem.isPresent()) {
            throw new BadRequestException("Sản phẩm đã tồn tại trong đợt flash sale này");
        }

        // Kiểm tra sản phẩm có tồn tại không
        Product product = productService.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + itemRequest.getProductId()));

        // Tạo mới FlashSaleItem
        FlashSaleItem flashSaleItem = new FlashSaleItem();
        flashSaleItem.setFlashSale(flashSale);
        flashSaleItem.setProduct(product);
        flashSaleItem.setPrice(itemRequest.getSalePrice());
        flashSaleItem.setDiscountPrice(BigDecimal.valueOf(itemRequest.getDiscountPercent()));
        flashSaleItem.setQuantity(itemRequest.getQuantity());
        flashSaleItem.setIsActive(itemRequest.getIsActive());

        FlashSaleItem savedItem = flashSaleItemRepository.save(flashSaleItem);

        return convertToItemDTO(savedItem);
    }

    @Override
    @Transactional
    public FlashSaleItemDTO updateFlashSaleItem(Long flashSaleId, Long itemId, FlashSaleRequest.FlashSaleItemRequest itemRequest) {
        FlashSaleItem flashSaleItem = flashSaleItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong đợt flash sale"));

        // Kiểm tra sản phẩm có thuộc đợt flash sale không
        if (!flashSaleItem.getFlashSale().getId().equals(flashSaleId)) {
            throw new BadRequestException("Sản phẩm không thuộc đợt flash sale này");
        }

        // Cập nhật thông tin sản phẩm
        flashSaleItem.setPrice(itemRequest.getSalePrice());
        flashSaleItem.setDiscountPrice(BigDecimal.valueOf(itemRequest.getDiscountPercent()));
        flashSaleItem.setQuantity(itemRequest.getQuantity());
        flashSaleItem.setIsActive(itemRequest.getIsActive());

        FlashSaleItem updatedItem = flashSaleItemRepository.save(flashSaleItem);

        return convertToItemDTO(updatedItem);
    }

    @Override
    @Transactional
    public void removeItemFromFlashSale(Long flashSaleId, Long itemId) {
        FlashSaleItem flashSaleItem = flashSaleItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong đợt flash sale"));

        // Kiểm tra sản phẩm có thuộc đợt flash sale không
        if (!flashSaleItem.getFlashSale().getId().equals(flashSaleId)) {
            throw new BadRequestException("Sản phẩm không thuộc đợt flash sale này");
        }

        flashSaleItemRepository.delete(flashSaleItem);
    }

    @Override
    @Transactional
    public void addToFlashSale(Long productId, int quantity) {
        // Kiểm tra sản phẩm có tồn tại không
        Product product = productService.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

        // Tìm đợt flash sale đang hoạt động
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> activeFlashSales = flashSaleRepository.findActiveFlashSales(now);

        if (activeFlashSales.isEmpty()) {
            throw new BadRequestException("Hiện không có đợt flash sale nào đang diễn ra");
        }

        // Lấy đợt flash sale đầu tiên đang hoạt động
        FlashSale flashSale = activeFlashSales.get(0);

        // Kiểm tra sản phẩm đã có trong đợt flash sale chưa
        Optional<FlashSaleItem> existingItem = flashSaleItemRepository.findByFlashSaleIdAndProductId(
                flashSale.getId(), productId);

        if (existingItem.isPresent()) {
            // Nếu đã tồn tại, cập nhật số lượng
            FlashSaleItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            flashSaleItemRepository.save(item);
        } else {
            // Nếu chưa tồn tại, tạo mới
            FlashSaleItem flashSaleItem = new FlashSaleItem();
            flashSaleItem.setFlashSale(flashSale);
            flashSaleItem.setProduct(product);
            flashSaleItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(0.8))); // Giảm giá 20%
            flashSaleItem.setDiscountPrice(BigDecimal.valueOf(20));
            flashSaleItem.setQuantity(quantity);
            flashSaleItem.setSoldQuantity(0);
            flashSaleItem.setIsActive(true);
            
            flashSaleItemRepository.save(flashSaleItem);
        }
    }

    @Override
    public Page<FlashSaleItemDTO> getActiveFlashSaleItems(Pageable pageable) {
        // Thêm tham số LocalDateTime.now() làm tham số đầu tiên
        Page<FlashSaleItem> activeItems = flashSaleItemRepository.findAllActiveFlashSaleItems(
                LocalDateTime.now(),  // Thêm thời gian hiện tại
                pageable
        );

        List<FlashSaleItemDTO> dtos = activeItems.getContent().stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, activeItems.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public FlashSaleItemDTO checkProductInFlashSale(Long productId) {
        LocalDateTime now = LocalDateTime.now();

        return flashSaleItemRepository
                .findActiveByProductIdAndTime(productId, now)
                .stream()
                .map(item -> {
                    FlashSaleItemDTO dto = modelMapper.map(item, FlashSaleItemDTO.class);
                    dto.setActive(true);
                    return dto;
                })
                .findFirst()  // If you only want the first item
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public int getRemainingQuantity(Long productId) {
        return flashSaleItemRepository
                .findActiveByProductId(productId, LocalDateTime.now())
                .map(item -> item.getQuantity() - item.getSoldQuantity())
                .orElse(0);
    }

    @Override
    @Transactional
    public void updateSoldQuantity(Long productId, int quantity) {
        flashSaleItemRepository
                .findActiveByProductId(productId, LocalDateTime.now())
                .ifPresent(item -> {
                    int newSoldQuantity = item.getSoldQuantity() + quantity;
                    if (newSoldQuantity > item.getQuantity()) {
                        throw new IllegalStateException("Số lượng bán vượt quá giới hạn cho phép");
                    }
                    item.setSoldQuantity(newSoldQuantity);
                    flashSaleItemRepository.save(item);
                });
    }

    @Override
    @Transactional
    public void removeFromFlashSale(Long productId) {
        // Tìm đợt flash sale đang hoạt động
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> activeFlashSales = flashSaleRepository.findActiveFlashSales(now);

        if (activeFlashSales.isEmpty()) {
            throw new BadRequestException("Hiện không có đợt flash sale nào đang diễn ra");
        }

        // Lấy đợt flash sale đầu tiên đang hoạt động
        FlashSale flashSale = activeFlashSales.get(0);

        // Tìm sản phẩm trong đợt flash sale
        Optional<FlashSaleItem> existingItem = flashSaleItemRepository.findByFlashSaleIdAndProductId(
                flashSale.getId(), productId);

        if (!existingItem.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm trong đợt flash sale hiện tại");
        }

        // Xóa sản phẩm khỏi flash sale
        flashSaleItemRepository.delete(existingItem.get());
    }

    @Override
    @Transactional
    public boolean purchaseFlashSale(Long productId, int quantity) {
        // Tìm sản phẩm trong flash sale đang hoạt động
        LocalDateTime now = LocalDateTime.now();
        Optional<FlashSaleItem> flashSaleItemOpt = flashSaleItemRepository.findActiveByProductId(productId, now);

        if (!flashSaleItemOpt.isPresent()) {
            return false; // Sản phẩm không có trong flash sale
        }

        FlashSaleItem flashSaleItem = flashSaleItemOpt.get();

        // Kiểm tra số lượng còn đủ không
        if (flashSaleItem.getQuantity() - flashSaleItem.getSoldQuantity() < quantity) {
            return false; // Không đủ hàng
        }

        // Cập nhật số lượng đã bán
        flashSaleItem.setSoldQuantity(flashSaleItem.getSoldQuantity() + quantity);
        flashSaleItemRepository.save(flashSaleItem);
        
        return true;
    }

    @Override
    @Scheduled(cron = "0 * * * * *") // Chạy mỗi phút
    @Transactional
    public void updateFlashSaleStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        // Cập nhật các flash sale đã kết thúc
        List<FlashSale> expiredFlashSales = flashSaleRepository.findByEndTimeBeforeAndIsActiveTrue(now);
        for (FlashSale flashSale : expiredFlashSales) {
            flashSale.setActive(false);
            flashSaleRepository.save(flashSale);
            
            // Cập nhật trạng thái các sản phẩm trong flash sale
            List<FlashSaleItem> items = flashSaleItemRepository.findByFlashSaleId(flashSale.getId());
            for (FlashSaleItem item : items) {
                item.setIsActive(false);
            }
            flashSaleItemRepository.saveAll(items);
        }
        
        // Kích hoạt các flash sale mới bắt đầu
        List<FlashSale> startingFlashSales = flashSaleRepository.findByStartTimeLessThanEqualAndEndTimeAfterAndIsActiveFalse(
            now);
        for (FlashSale flashSale : startingFlashSales) {
            flashSale.setActive(true);
            flashSaleRepository.save(flashSale);
            
            // Kích hoạt các sản phẩm trong flash sale
            List<FlashSaleItem> items = flashSaleItemRepository.findByFlashSaleId(flashSale.getId());
            for (FlashSaleItem item : items) {
                item.setIsActive(true);
            }
            flashSaleItemRepository.saveAll(items);
        }
    }

    private void validateFlashSaleTime(LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        if (startTime.isAfter(endTime)) {
            throw new BadRequestException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }

        // Kiểm tra xem có đợt flash sale nào trùng thời gian không
        if (excludeId == null) {
            if (flashSaleRepository.existsByStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndIsActiveTrue(endTime, startTime)) {
                throw new BadRequestException("Đã có đợt flash sale khác diễn ra trong khoảng thời gian này");
            }
        } else {
            if (flashSaleRepository.existsByIdNotAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndIsActiveTrue(
                    excludeId, endTime, startTime)) {
                throw new BadRequestException("Đã có đợt flash sale khác diễn ra trong khoảng thời gian này");
            }
        }
    }

    private FlashSaleDTO convertToDTO(FlashSale flashSale) {
        FlashSaleDTO dto = modelMapper.map(flashSale, FlashSaleDTO.class);

        // Tính thời gian còn lại (nếu đang diễn ra)
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(flashSale.getStartTime()) && now.isBefore(flashSale.getEndTime())) {
            long secondsRemaining = Duration.between(now, flashSale.getEndTime()).getSeconds();
            dto.setTimeRemainingInSeconds(secondsRemaining > 0 ? secondsRemaining : 0);
        }

        return dto;
    }

    private FlashSaleItemDTO convertToItemDTO(FlashSaleItem item) {
        if (item == null) {
            return null;
        }

        FlashSaleItemDTO dto = modelMapper.map(item, FlashSaleItemDTO.class);
        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getName());
        }

        // Thêm kiểm tra null cho flashSale
        if (item.getFlashSale() != null) {
            dto.setFlashSaleId(item.getFlashSale().getId());
        } else {
            dto.setFlashSaleId(null); // hoặc giá trị mặc định phù hợp
        }

        return dto;
    }
}
