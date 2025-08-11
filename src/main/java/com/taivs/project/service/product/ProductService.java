package com.taivs.project.service.product;

import com.taivs.project.dto.request.ProductDTO;
import com.taivs.project.dto.request.ProductInfoDTO;
import com.taivs.project.dto.response.*;
import com.taivs.project.entity.Product;
import com.taivs.project.entity.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductFullResponse createProduct(ProductDTO dto);

    PagedResponse<ProductFullResponse> searchProducts(String name, String barcode, int page, int size, String sortField, String sortDirection);

    List<TopRevenueProductResponse> top10RevenueProducts();

    ProductFullResponse updateProduct(Long id, ProductInfoDTO dto);

    void deleteProduct(Long id);

    ProductImage createProductImage(Long productId, MultipartFile file);

    void deleteProductImage(Long imageId);

    void deleteAllProductImages(Long productId);

    ProductFullResponse getProductById(Long id);

    List<TopRiskStock> topRiskStockProducts(Long warehouseId,int limit);
}
