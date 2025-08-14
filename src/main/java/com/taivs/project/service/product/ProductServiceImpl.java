package com.taivs.project.service.product;

import com.taivs.project.dto.request.InventoryDTO;
import com.taivs.project.dto.request.InventoryWarehouse;
import com.taivs.project.dto.request.ProductDTO;
import com.taivs.project.dto.request.ProductInfoDTO;
import com.taivs.project.dto.response.*;
import com.taivs.project.entity.*;
import com.taivs.project.exception.*;
import com.taivs.project.repository.InventoryRepository;
import com.taivs.project.repository.ProductImageRepository;
import com.taivs.project.repository.ProductRepository;
import com.taivs.project.repository.WarehouseRepository;
import com.taivs.project.service.auth.AuthService;
import com.taivs.project.service.cloudinary.CloudinaryService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private void insertNewInventories(List<InventoryWarehouse> inventoryWarehouses, Product product){

        User user = authService.getCurrentUser();

        Map<Long, Integer> mergedQuantities = inventoryWarehouses.stream()
                .collect(Collectors.toMap(
                        InventoryWarehouse::getWarehouseId,
                        InventoryWarehouse::getQuantity,
                        Integer::sum
                ));

        List<Long> warehouseIds = new ArrayList<>(mergedQuantities.keySet());
        List<Warehouse> warehouses = warehouseRepository.findAllByIdIn(warehouseIds, user.getId());

        Map<Long, Warehouse> warehouseMap = warehouses.stream()
                .peek(wh -> {
                    if (!wh.getUser().equals(user)) {
                        throw new UnauthorizedAccessException("Cannot access warehouse " + wh.getId());
                    }
                })
                .collect(Collectors.toMap(Warehouse::getId, wh -> wh));

        if (warehouseMap.size() != warehouseIds.size()) {
            throw new DataNotFoundException("Some warehouses not found");
        }

        for (Map.Entry<Long, Integer> entry : mergedQuantities.entrySet()) {
            Warehouse warehouse = warehouseMap.get(entry.getKey());
            Integer quantity = entry.getValue();

            Inventory newInventory = Inventory.builder()
                    .warehouse(warehouse)
                    .product(product)
                    .quantity(quantity)
                    .build();

            inventoryRepository.save(newInventory);
        }
    }

    private void checkProductOwnership(Product product, User user) {
        if (!Objects.equals(product.getUser().getId(), user.getId())) {
            throw new UnauthorizedAccessException("Not authorized");
        }
    }

    private void validateProductUniqueness(ProductInfoDTO dto, User user, Product existing) {
        if (!dto.getBarcode().equals(existing.getBarcode()) &&
                productRepository.existsByBarcodeAndUserId(dto.getBarcode(), user.getId())) {
            throw new ResourceAlreadyExistsException("Barcode already exists");
        }

        if (!dto.getName().equals(existing.getName()) &&
                productRepository.existsByNameAndUserId(dto.getName(), user.getId())) {
            throw new ResourceAlreadyExistsException("Product name already exists");
        }
    }

    @Transactional
    public ProductFullResponse createProduct(ProductDTO dto) {
        User user = authService.getCurrentUser();

        if (productRepository.existsByBarcodeAndUserId(dto.getBarcode(), user.getId())) {
            throw new ResourceAlreadyExistsException("Barcode already exists");
        }
        if (productRepository.existsByNameAndUserId(dto.getName(), user.getId())) {
            throw new ResourceAlreadyExistsException("Product name already exists");
        }

        Product product = Product.builder()
                .name(dto.getName())
                .barcode(dto.getBarcode())
                .weight(dto.getWeight())
                .width(dto.getWidth())
                .height(dto.getHeight())
                .length(dto.getLength())
                .price(dto.getPrice())
                .user(user)
                .isDeleted((byte) 0)
                .status((byte) 1)
                .build();

        productRepository.save(product);

        insertNewInventories(dto.getInventoryDTOS(),product);

        int totalStock = inventoryRepository.sumQuantityByProductId(product.getId())
                .orElse(0);

        ProductFullResponse.ProductFullResponseBuilder builder = ProductFullResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .barcode(product.getBarcode())
                .weight(product.getWeight())
                .width(product.getWidth())
                .height(product.getHeight())
                .length(product.getLength())
                .price(product.getPrice())
                .stock(totalStock);

        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            builder.imageUrls(
                    product.getProductImages()
                            .stream()
                            .map(ProductImage::getImageUrl)
                            .toList()
            );
        }

        return builder.build();
    }


    public PagedResponse<ProductFullResponse> searchProducts(String name, String barcode,
                                                             int page, int size,
                                                             String sortField, String sortDirection) {
        size = Math.min(size, 10);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        User user = authService.getCurrentUser();
        Page<ProductFullResponse> products = productRepository.findByNameContainingAndBarcodeContaining(user.getId(),name, barcode, pageable)
                .map(p -> ProductFullResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .barcode(p.getBarcode())
                        .weight(p.getWeight())
                        .width(p.getWidth())
                        .height(p.getHeight())
                        .stock(inventoryRepository.sumQuantityByProductId(p.getId()).orElse(0))
                        .price(p.getPrice()).build() );
        return new PagedResponse<>(
                products.getContent(),
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isLast()
        );
    }
    public List<TopRevenueProductResponse> top10RevenueProducts() {
        Long userId = authService.getCurrentUser().getId();

        List<TopRevenueProduct> topProducts = productRepository.findTop10RevenueProducts(userId);

        return topProducts.stream()
                .map(p -> TopRevenueProductResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .revenueQuantity(p.getTotalQuantity())
                        .build())
                .toList();
    }

    public ProductFullResponse updateProduct(Long id, ProductInfoDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        User user = authService.getCurrentUser();
        checkProductOwnership(product, user);

        if (productRepository.countProductInPackage(id) > 0) {
            throw new InvalidProductUpdated("Cannot update this product, it has been ordered");
        }

        validateProductUniqueness(dto, user, product);

        product.setName(dto.getName());
        product.setBarcode(dto.getBarcode());
        product.setWeight(dto.getWeight());
        product.setWidth(dto.getWidth());
        product.setHeight(dto.getHeight());
        product.setLength(dto.getLength());
        product.setPrice(dto.getPrice());

        productRepository.save(product);

        Integer stock = inventoryRepository.sumQuantityByProductId(product.getId()).orElse(0);

        ProductFullResponse productResponseDTO = ProductFullResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .barcode(product.getBarcode())
                .weight(product.getWeight())
                .width(product.getWidth())
                .height(product.getHeight())
                .stock(stock)
                .price(product.getPrice()).build();
        if(product.getProductImages() != null){
            productResponseDTO.setImageUrls(product.getProductImages().stream().map(ProductImage::getImageUrl).toList());
        }

        return productResponseDTO;
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        User user = authService.getCurrentUser();
        checkProductOwnership(product, user);

        if (productRepository.countProductInPackage(id) > 0) {
            throw new InvalidProductUpdated("Cannot delete this product, it has been ordered");
        }

        product.setIsDeleted((byte) 1);
        deleteAllProductImages(product.getId());

        for (Inventory inventory : product.getInventories()){
            inventory.setIsDeleted((byte) 1);
        }
        productRepository.save(product);
    }

    public ProductImage createProductImage(Long productId, MultipartFile file) {
        User user = authService.getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        if (!product.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Not authorized");
        }

        if (product.getProductImages().size() >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            throw new ImageLimitExceededException("Maximum " +ProductImage.MAXIMUM_IMAGES_PER_PRODUCT +" images allowed per product");
        }
        if (file.isEmpty()) {
            throw new InvalidFileException("Empty file");
        }

        String imageUrl = cloudinaryService.uploadFile(file);

        ProductImage image = ProductImage.builder()
                .imageUrl(imageUrl)
                .product(product)
                .build();
        return productImageRepository.save(image);
    }

    public void deleteProductImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new DataNotFoundException("Image not found"));

        User user = authService.getCurrentUser();
        if (!image.getProduct().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Not authorized to delete this image");
        }

        image.setIsDeleted(1);
        productImageRepository.save(image);
    }

    public void deleteAllProductImages(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));
        User user = authService.getCurrentUser();
        if (!product.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Not authorized to delete this product");
        }

        product.getProductImages().forEach(image -> {
            image.setIsDeleted(1);
            productImageRepository.save(image);
        });
    }

    public ProductFullResponse getProductById(Long id){
        Product product = productRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Product not found"));
        User user = authService.getCurrentUser();
        if (!user.equals(product.getUser())) throw new UnauthorizedAccessException("You are unauthorized to get this Product");
        return ProductFullResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .barcode(product.getBarcode())
                .stock(inventoryRepository.sumQuantityByProductId(id).orElse(0))
                .weight(product.getWeight())
                .width(product.getWidth())
                .height(product.getHeight())
                .price(product.getPrice()).build();
    }

    @Override
    public List<TopRiskStock> topRiskStockProducts(Long warehouseId, int limit) {
        User user = authService.getCurrentUser();
        return productRepository.findTopInventoryRiskProducts(user.getId(),warehouseId,limit);
    }
}
