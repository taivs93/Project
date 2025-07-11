package org.example.service.product;

import org.example.dto.request.ProductDTO;
import org.example.dto.response.ProductResponseDTO;
import org.example.entity.Product;
import org.example.entity.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

    Product createProduct(ProductDTO dto);

    Page<ProductResponseDTO> searchProducts(String name, String barcode, int page, int size, String sortField, String sortDirection);

    Page<ProductResponseDTO> topRevenueProducts(int page, int size);

    Page<ProductResponseDTO> topStockProducts(int page, int size);

    Product updateProduct(Long id, ProductDTO dto);

    void deleteProduct(Long id);

    ProductImage createProductImage(Long productId, MultipartFile file);

    void deleteProductImage(Long imageId);

    void deleteAllProductImages(Long productId);

    ProductResponseDTO getProductById(Long id);
}
