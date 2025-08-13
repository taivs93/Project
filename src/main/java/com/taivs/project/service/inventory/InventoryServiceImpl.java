package com.taivs.project.service.inventory;

import com.taivs.project.dto.request.InventoryDTO;
import com.taivs.project.dto.response.InventoryResponse;
import com.taivs.project.entity.*;
import com.taivs.project.exception.DataNotFoundException;
import com.taivs.project.exception.NotEnoughStockException;
import com.taivs.project.exception.UnauthorizedAccessException;
import com.taivs.project.repository.InventoryRepository;
import com.taivs.project.repository.InventoryTransactionRepository;
import com.taivs.project.repository.ProductRepository;
import com.taivs.project.repository.WarehouseRepository;
import com.taivs.project.service.auth.AuthService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceImpl implements InventoryService{

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    private void saveInventoryTransaction(Inventory inventory, Integer quantity, TransactionType type){
        InventoryTransaction inventoryTransaction = InventoryTransaction.builder()
                .warehouse(inventory.getWarehouse())
                .product(inventory.getProduct())
                .type(type)
                .quantity(quantity)
                .resultingQuantity(inventory.getQuantity())
                .user(inventory.getUser())
                .build();

        inventoryTransactionRepository.save(inventoryTransaction);
    }

    private Inventory getInventoryIfExistAndAuth(InventoryDTO inventoryDTO){
        User user = authService.getCurrentUser();

        Inventory inventory = inventoryRepository.findByWarehouseIdAndProductId(inventoryDTO.getWarehouseId(), inventoryDTO.getProductId()).orElseThrow(() -> new DataNotFoundException("Inventory not found"));

        if (!inventory.getUser().equals(user)) throw new UnauthorizedAccessException("Can not access this resource");

        return inventory;
    }

    @Override
    @Transactional
    public InventoryResponse importInventory(InventoryDTO inventoryDTO) {
        User user = authService.getCurrentUser();
        Product product = productRepository.findById(inventoryDTO.getProductId()).orElseThrow(
                () -> new DataNotFoundException("Product not found")
        );
        Warehouse warehouse = warehouseRepository.findById(inventoryDTO.getWarehouseId()).orElseThrow(
                () -> new DataNotFoundException("Warehouse not found")
        );
        Inventory inventory = inventoryRepository.
                findByWarehouseIdAndProductId(warehouse.getId(), product.getId()).orElse(null);
        if (inventory != null) {
            Integer currentStock = inventory.getQuantity();
            inventory.setQuantity(currentStock+inventoryDTO.getQuantity());
        } else {
            inventory = Inventory.builder()
                    .warehouse(warehouse)
                    .product(product)
                    .quantity(inventoryDTO.getQuantity())
                    .user(user)
                    .build();
        }

        this.saveInventoryTransaction(inventory, inventoryDTO.getQuantity(),TransactionType.IMPORT);
        inventoryRepository.save(inventory);
        return InventoryResponse.builder()
                .id(inventory.getId())
                .warehouseId(inventoryDTO.getWarehouseId())
                .productId(inventoryDTO.getProductId())
                .quantity(inventory.getQuantity())
                .build();
    }

    @Override
    public InventoryResponse exportInventory(InventoryDTO inventoryDTO) {
        Inventory inventory = this.getInventoryIfExistAndAuth(inventoryDTO);

        if (inventoryDTO.getQuantity() > inventory.getQuantity()) throw new NotEnoughStockException("Not enough stock to export");

        Integer currentQuantity = inventory.getQuantity();

        inventory.setQuantity(currentQuantity - inventoryDTO.getQuantity());

        this.saveInventoryTransaction(inventory, inventoryDTO.getQuantity(),TransactionType.EXPORT);

        inventoryRepository.save(inventory);

        return InventoryResponse.builder()
                .id(inventory.getId())
                .warehouseId(inventory.getWarehouse().getId())
                .productId(inventory.getProduct().getId())
                .quantity(inventory.getQuantity())
                .build();
    }

    @Override
    public InventoryResponse adjustInventory(InventoryDTO inventoryDTO) {
        Inventory inventory = this.getInventoryIfExistAndAuth(inventoryDTO);

        inventory.setQuantity(inventory.getQuantity());

        this.saveInventoryTransaction(inventory, inventoryDTO.getQuantity(),TransactionType.ADJUST);

        inventoryRepository.save(inventory);

        return InventoryResponse.builder()
                .id(inventory.getId())
                .warehouseId(inventory.getWarehouse().getId())
                .productId(inventory.getProduct().getId())
                .quantity(inventory.getQuantity())
                .build();
    }
}
