package com.taivs.project.service.product;

import com.taivs.project.dto.request.ProductDTO;
import com.taivs.project.dto.response.ProductResponseDTO;
import com.taivs.project.entity.Product;
import com.taivs.project.entity.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    Product createProduct(ProductDTO dto);

    Page<ProductResponseDTO> searchProducts(String name, String barcode, int page, int size, String sortField, String sortDirection);

    List<ProductResponseDTO> top10RevenueProducts();

    List<ProductResponseDTO> top10StockProducts();

    Product updateProduct(Long id, ProductDTO dto);

    void deleteProduct(Long id);

    ProductImage createProductImage(Long productId, MultipartFile file);

    void deleteProductImage(Long imageId);

    void deleteAllProductImages(Long productId);

    ProductResponseDTO getProductById(Long id);
}
