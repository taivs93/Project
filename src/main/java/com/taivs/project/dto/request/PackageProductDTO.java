package com.taivs.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;


@Getter
public class PackageProductDTO {

    @JsonProperty("product_id")
    @Positive
    private Long productId;

    @JsonProperty("warehouse_id")
    @Positive
    private Long warehouseId;

    @Size(max = 250, message = "Name must be at most 250 characters")
    @JsonProperty("product_name")
    private String productName;

    @Size(max = 50, message = "Barcode must be at most 50 characters")
    private String barcode;

    @Positive(message = "Weight must be greater than 0")
    private Double weight;

    @Positive(message = "Height must be greater than 0")
    private Double height;

    @Positive(message = "Length must be greater than 0")
    private Double length;

    @Positive(message = "Width must be greater than 0")
    private Double width;

    @PositiveOrZero(message = "Price must be zero or positive")
    private Double price;

    @NotNull(message = "Quantity must not be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}

