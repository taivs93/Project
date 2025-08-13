package com.taivs.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taivs.project.entity.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InventoryTransactionResponse {

    private Long  id;

    @JsonProperty("warehouse")
    private String warehouseName;

    @JsonProperty("product")
    private String productName;

    private Integer quantity;

    @JsonProperty("result_quantity")
    private Integer resultingQuantity;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("type")
    private TransactionType type;
}
