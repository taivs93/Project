package com.taivs.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;


@Getter
public class PackageProductDTO {

    @NotNull(message = "Product ID is required")
    @JsonProperty("product_id")
    @Positive
    private Long productId;

    @NotNull(message = "Warehouse ID is required")
    @JsonProperty("warehouse_id")
    @Positive
    private Long warehouseId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}

