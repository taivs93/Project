package com.taivs.project.controller;

import jakarta.validation.Valid;
import com.taivs.project.dto.request.ProductDTO;
import com.taivs.project.dto.response.PagedResponse;
import com.taivs.project.dto.response.ProductImageResponseDTO;
import com.taivs.project.dto.response.ProductResponseDTO;
import com.taivs.project.dto.response.ResponseDTO;
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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> insertProduct(@Valid @RequestBody ProductDTO productDTO, BindingResult bindingResult){
        Product product = productService.createProduct(productDTO);
        ProductResponseDTO productResponseDTO = ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .barcode(product.getBarcode())
                .weight(product.getWeight())
                .width(product.getWidth())
                .height(product.getHeight())
                .length(product.getLength())
                .stock(product.getStock())
                .price(product.getPrice())
                .build();
        if(product.getProductImages() != null){
            productResponseDTO.setImageUrls(product.getProductImages().stream().map(ProductImage::getImageUrl).toList());
        }
        return ResponseEntity.ok(ResponseDTO.builder().status(201).message("Product created successfully").data(productResponseDTO).build());
    }

    @GetMapping("/get-by-id/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> getProductById(@PathVariable("id") Long id){
        ProductResponseDTO productResponseDTO = productService.getProductById(id);
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get product successfully!").data(productResponseDTO).build());
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/search-products")
    public ResponseEntity<ResponseDTO> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String barcode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Page<ProductResponseDTO> products = productService.searchProducts(name, barcode, page, size, sortField, sortDirection);

        PagedResponse<ProductResponseDTO> pagedResponse = new PagedResponse<>(
                products.getContent(),
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isLast()
        );

        return ResponseEntity.ok(ResponseDTO.builder()
                .status(200)
                .message("Search products successfully")
                .data(pagedResponse)
                .build());
    }


    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> updateProduct(@PathVariable(name = "id") Long productId,@Valid @RequestBody ProductDTO productDTO, BindingResult bindingResult){
        Product product = productService.updateProduct(productId,productDTO);
        ProductResponseDTO productResponseDTO = ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .barcode(product.getBarcode())
                .weight(product.getWeight())
                .width(product.getWidth())
                .height(product.getHeight())
                .stock(product.getStock())
                .price(product.getPrice()).build();
        if(product.getProductImages() != null){
            productResponseDTO.setImageUrls(product.getProductImages().stream().map(ProductImage::getImageUrl).toList());
        }
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Product updated successfully").data(productResponseDTO).build());
    }

    @PatchMapping("/delete/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> deleteProduct(@PathVariable(name = "id") Long productId){
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ResponseDTO.builder().status(204).message("Product deleted successfully").build());
    }

    @PostMapping("/{productId}/add-image")
    @PreAuthorize("hasRole('USER')")
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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> deleteImage(@PathVariable Long imageId){
        productService.deleteProductImage( imageId);
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Image deleted successfully").build());
    }
}
