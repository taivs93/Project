package com.taivs.project.controller;

import com.taivs.project.dto.request.ProductInfoDTO;
import com.taivs.project.dto.response.*;
import jakarta.validation.Valid;
import com.taivs.project.dto.request.ProductDTO;
import com.taivs.project.entity.Product;
import com.taivs.project.entity.ProductImage;
import com.taivs.project.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    ProductService productService;

    @PostMapping("/insert")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> insertProduct(@Valid @RequestBody ProductDTO productDTO){
        return ResponseEntity.ok(ResponseDTO.builder().status(201).message("Product created successfully").data(productService.createProduct(productDTO)).build());
    }

    @PreAuthorize("hasRole('SHOP')")
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<ResponseDTO> getProductById(@PathVariable("id") Long id){
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get product successfully!").data(productService.getProductById(id)).build());
    }

    @PreAuthorize("hasRole('SHOP')")
    @GetMapping("/search-products")
    public ResponseEntity<ResponseDTO> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String barcode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        PagedResponse<ProductFullResponse> pagedResponse = productService.searchProducts(name, barcode, page, size, sortField, sortDirection);

        return ResponseEntity.ok(ResponseDTO.builder()
                .status(200)
                .message("Search products successfully")
                .data(pagedResponse)
                .build());
    }


    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> updateProduct(@PathVariable(name = "id") Long productId,@Valid @RequestBody ProductInfoDTO productDTO){
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Product updated successfully").data(productService.updateProduct(productId,productDTO )).build());
    }

    @PatchMapping("/delete/{id}")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> deleteProduct(@PathVariable(name = "id") Long productId){
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ResponseDTO.builder().status(204).message("Product deleted successfully").build());
    }

    @PostMapping("/{productId}/add-image")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> addImage(@PathVariable Long productId,
                                                @RequestParam("file") MultipartFile file) {
        ProductImage productImage = productService.createProductImage(productId, file);

        return ResponseEntity.ok(ResponseDTO.builder()
                .status(200)
                .message("Image added successfully")
                .data(ProductImageResponseDTO.fromEntity(productImage))
                .build());
    }

    @PatchMapping("/delete-image/{imageId}")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> deleteImage(@PathVariable Long imageId){
        productService.deleteProductImage( imageId);
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Image deleted successfully").build());
    }
}
