package com.taivs.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taivs.project.dto.request.InventoryDTO;
import com.taivs.project.dto.request.InventoryWarehouse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class WarehouseResponse {

    private Long id;
    private String name;
    private String location;
    @JsonProperty("inventories")
    private List<InventoryResponse> inventoryDTOS;
}
