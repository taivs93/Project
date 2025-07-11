package org.example.controller;

import org.example.dto.response.PagedResponse;
import org.example.dto.response.ProductResponseDTO;
import org.example.dto.response.ResponseDTO;
import org.example.service.order.PackageService;
import org.example.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/dashboard")
public class DashboardController {

    @Autowired
    private PackageService packageService;

    @Autowired
    private ProductService productService;

    @GetMapping("/user/top-revenue-products")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> getTopRevenueProducts(@RequestParam(defaultValue = "0") int page
            ,@RequestParam(defaultValue = "5") int size){
        Page<ProductResponseDTO> productResponseDTOS = productService.topRevenueProducts(page,size);
        PagedResponse<ProductResponseDTO> pagedResponse = new PagedResponse<>(
                productResponseDTOS.getContent(),
                productResponseDTOS.getNumber(),
                productResponseDTOS.getSize(),
                productResponseDTOS.getTotalElements(),
                productResponseDTOS.getTotalPages(),
                productResponseDTOS.isLast()
        );
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get top revenue products successfully").data(pagedResponse).build());
    }

    @GetMapping("/user/top-stock-products")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> getTopStockProducts(@RequestParam(defaultValue = "0") int page
            ,@RequestParam(defaultValue = "5") int size){
        Page<ProductResponseDTO> productResponseDTOS = productService.topStockProducts(page,size);
        PagedResponse<ProductResponseDTO> pagedResponse = new PagedResponse<>(
                productResponseDTOS.getContent(),
                productResponseDTOS.getNumber(),
                productResponseDTOS.getSize(),
                productResponseDTOS.getTotalElements(),
                productResponseDTOS.getTotalPages(),
                productResponseDTOS.isLast()
        );
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get top stock products successfully").data(pagedResponse).build());
    }

    @GetMapping("user/get-revenue-by-time")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> getRevenueByTime(@RequestParam String time){
        Double revenue = packageService.getRevenue(time);
        String roundedRevenue = String.format("%.2f", revenue);
        return ResponseEntity.ok(ResponseDTO.builder()
                .status(200)
                .message("Get revenue by time")
                .data(roundedRevenue)
                .build());
    }

}
