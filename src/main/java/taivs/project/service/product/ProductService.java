package taivs.project.service.product;

import taivs.project.dto.request.ProductDTO;
import taivs.project.dto.response.ProductResponseDTO;
import taivs.project.entity.Product;
import taivs.project.entity.ProductImage;
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
