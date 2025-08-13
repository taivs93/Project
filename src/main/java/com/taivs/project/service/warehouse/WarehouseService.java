package com.taivs.project.service.warehouse;

import com.taivs.project.dto.request.WarehouseDTO;
import com.taivs.project.dto.response.InventoryTransactionResponse;
import com.taivs.project.dto.response.PagedResponse;
import com.taivs.project.dto.response.WarehouseResponse;

import java.util.List;

public interface WarehouseService {

    WarehouseResponse insertWarehouse(WarehouseDTO warehouseDTO);

    WarehouseResponse updateWarehouse(Long id, WarehouseDTO warehouseDTO);

    void deleteWarehouse(Long id);

    WarehouseResponse getWarehouse(Long id);

    List<WarehouseResponse> getWarehouses();

    InventoryTransactionResponse getInventoryTransactionById(Long id);

    PagedResponse<InventoryTransactionResponse> getListInventoryTransactions(int page, int limit, String sortField, String sortDirection, String warehouseName, String productName);
}
