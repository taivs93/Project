package com.taivs.project.service.product;

import com.taivs.project.dto.request.ProductDTO;
import com.taivs.project.dto.response.ProductResponseDTO;
import com.taivs.project.entity.Product;
import com.taivs.project.entity.ProductImage;
import com.taivs.project.entity.User;
import com.taivs.project.exception.*;
import com.taivs.project.repository.ProductImageRepository;
import com.taivs.project.repository.ProductRepository;
import com.taivs.project.repository.UserRepository;
import com.taivs.project.service.auth.AuthService;
import com.taivs.project.service.cloudinary.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private AuthService authService;

    private void checkProductOwnership(Product product, User user) {
        if (!Objects.equals(product.getUser().getId(), user.getId())) {
            throw new UnauthorizedAccessException("Not authorized");
        }
    }

    private void validateProductUniqueness(ProductDTO dto, User user, Product existing) {
        if (!dto.getBarcode().equals(existing.getBarcode()) &&
                productRepository.existsByBarcodeAndUserId(dto.getBarcode(), user.getId())) {
            throw new ResourceAlreadyExistsException("Barcode already exists");
        }

        if (!dto.getName().equals(existing.getName()) &&
                productRepository.existsByNameAndUserId(dto.getName(), user.getId())) {
            throw new ResourceAlreadyExistsException("Product name already exists");
        }
    }

    public Product createProduct(ProductDTO dto) {
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
                .stock(dto.getStock())
                .user(user)
                .isDeleted((byte) 0)
                .status((byte) 1)
                .build();

        return productRepository.save(product);
    }

    public Page<ProductResponseDTO> searchProducts(String name, String barcode,
                                                   int page, int size,
                                                   String sortField, String sortDirection) {
        size = Math.min(size, 10);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        User user = authService.getCurrentUser();
        return productRepository.findByNameContainingAndBarcodeContaining(user.getId(),name, barcode, pageable)
                .map(this::toDTO);
    }
    public List<ProductResponseDTO> top10RevenueProducts(){
        return productRepository.findTop10RevenueProducts(authService.getCurrentUser().getId()).stream().map(this::toDTO).toList();
    }
    public List<ProductResponseDTO> top10StockProducts(){

        return productRepository.findTop10StockProducts(authService.getCurrentUser().getId()).stream().map(this::toDTO).toList();
    }

    private ProductResponseDTO toDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .barcode(product.getBarcode())
                .weight(product.getWeight())
                .height(product.getHeight())
                .length(product.getLength())
                .width(product.getWidth())
                .stock(product.getStock())
                .price(product.getPrice())
                .imageUrls(product.getProductImages().stream()
                        .map(ProductImage::getImageUrl)
                        .toList())
                .build();
    }

    public Product updateProduct(Long id, ProductDTO dto) {
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
        product.setStock(dto.getStock());
        product.setPrice(dto.getPrice());

        return productRepository.save(product);
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

        ProductImage image = new ProductImage();
        image.setImageUrl(imageUrl);
        image.setProduct(product);
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

    public ProductResponseDTO getProductById(Long id){
        Product product = productRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Product not found"));
        User user = authService.getCurrentUser();
        if (!user.equals(product.getUser())) throw new UnauthorizedAccessException("You are unauthorized to get this Product");
        return toDTO(product);
    }
}
