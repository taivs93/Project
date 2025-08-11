package com.taivs.project.controller;

import com.taivs.project.dto.response.PagedResponse;
import com.taivs.project.dto.response.ProductResponseDTO;
import com.taivs.project.dto.response.ResponseDTO;
import com.taivs.project.dto.response.TopRevenueProductResponse;
import com.taivs.project.service.order.PackageService;
import com.taivs.project.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/dashboard")
public class DashboardController {

    @Autowired
    private PackageService packageService;

    @Autowired
    private ProductService productService;

    @GetMapping("/top-revenue-products")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> getTopRevenueProducts(){
        List<TopRevenueProductResponse> productResponseDTOS = productService.top10RevenueProducts();
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get top revenue products successfully").data(productResponseDTOS).build());
    }

    @GetMapping("/top-stock-products")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> getTopStockProducts(@RequestParam(defaultValue = "10") int limit,
                                                           @RequestParam(required = false) Long warehouseId
                                                           ){
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get top stock products successfully")
                .data(productService.topRiskStockProducts(warehouseId,limit)).build());
    }

    @GetMapping("/get-revenue-by-time")
    @PreAuthorize("hasRole('SHOP')")
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