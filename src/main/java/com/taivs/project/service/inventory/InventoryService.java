package com.taivs.project.service.inventory;

import com.taivs.project.dto.request.InventoryDTO;
import com.taivs.project.dto.response.InventoryResponse;

public interface InventoryService {

    InventoryResponse importInventory(InventoryDTO inventoryDTO);

    InventoryResponse exportInventory(InventoryDTO inventoryDTO);

    InventoryResponse adjustInventory(InventoryDTO inventoryDTO);
}
