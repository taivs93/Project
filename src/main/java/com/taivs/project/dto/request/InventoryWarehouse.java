package com.taivs.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class InventoryWarehouse {

    @NotNull(message = "Warehouse id must not be null")
    @Min(value = 1, message = "Warehouse id must be greater than zero")
    @JsonProperty(value = "warehouse_id")
    private Long warehouseId;

    @Min(value = 1, message = "Quantity id must be greater than zero")
    @NotNull(message = "Quantity must not be null")
    private Integer quantity;
}
