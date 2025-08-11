package com.taivs.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryResponse {

    private Long id;

    @JsonProperty(value = "warehouse_id")
    private Long warehouseId;

    @JsonProperty("product_id")
    private Long productId;

    private Integer quantity;
}
