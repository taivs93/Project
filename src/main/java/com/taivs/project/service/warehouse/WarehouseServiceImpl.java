package com.taivs.project.service.warehouse;

import com.taivs.project.dto.request.WarehouseDTO;
import com.taivs.project.dto.response.InventoryResponse;
import com.taivs.project.dto.response.InventoryTransactionResponse;
import com.taivs.project.dto.response.PagedResponse;
import com.taivs.project.dto.response.WarehouseResponse;
import com.taivs.project.entity.Inventory;
import com.taivs.project.entity.InventoryTransaction;
import com.taivs.project.entity.User;
import com.taivs.project.entity.Warehouse;
import com.taivs.project.exception.DataNotFoundException;
import com.taivs.project.exception.MainWarehouseDeleteException;
import com.taivs.project.exception.ResourceAlreadyExistsException;
import com.taivs.project.exception.UnauthorizedAccessException;
import com.taivs.project.repository.InventoryTransactionRepository;
import com.taivs.project.repository.WarehouseRepository;
import com.taivs.project.service.auth.AuthService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseServiceImpl implements WarehouseService{

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Override
    public WarehouseResponse insertWarehouse(WarehouseDTO warehouseDTO) {
        User user = authService.getCurrentUser();

        if (warehouseRepository.existsByUserIdAndNameOrUserIdAndLocation(user.getId(),warehouseDTO.getName(), warehouseDTO.getLocation())){
            throw new ResourceAlreadyExistsException("Name or location has already existed");
        }

        Warehouse newWarehouse = Warehouse.builder()
                .name(warehouseDTO.getName())
                .location(warehouseDTO.getLocation())
                .user(user)
                .build();

        warehouseRepository.save(newWarehouse);

        return WarehouseResponse.builder()
                .id(newWarehouse.getId())
                .name(newWarehouse.getName())
                .location(newWarehouse.getLocation())
                .build();
    }

    @Override
    public WarehouseResponse updateWarehouse(Long id, WarehouseDTO warehouseDTO) {
        User user = authService.getCurrentUser();

        Warehouse existsWarehouse = warehouseRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Warehouse not found"));

        if (!existsWarehouse.getUser().equals(user)) throw new UnauthorizedAccessException("Unauthorize to access this warehouse");

        if ((!existsWarehouse.getName().equals(warehouseDTO.getName()) && !existsWarehouse.getLocation().equals(warehouseDTO.getLocation())) &&
                warehouseRepository.existsByUserIdAndNameOrUserIdAndLocation(user.getId(), warehouseDTO.getName(), warehouseDTO.getLocation())
        ) throw new ResourceAlreadyExistsException("Name or location has already existed");

        existsWarehouse.setLocation(warehouseDTO.getLocation());
        existsWarehouse.setName(warehouseDTO.getName());

        warehouseRepository.save(existsWarehouse);

        List<InventoryResponse> inventoryResponses = existsWarehouse.getInventories().stream().filter(inventory -> inventory.getIsDeleted() == 0).map(inventory
                -> InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getId())
                .quantity(inventory.getQuantity())
                .build()
        ).toList();
        return WarehouseResponse.builder()
                .name(existsWarehouse.getName())
                .location(existsWarehouse.getLocation())
                .inventoryDTOS(inventoryResponses)
                .build();
    }

    @Override
    @Transactional
    public void deleteWarehouse(Long id) {
        User user = authService.getCurrentUser();
        Warehouse existsWarehouse = warehouseRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Warehouse not found"));
        if (!existsWarehouse.getUser().equals(user)) throw new UnauthorizedAccessException("Unauthorize to access this warehouse");
        if (existsWarehouse.getIsMain() == 1) throw new MainWarehouseDeleteException("Main warehouse can not be deleted!");

        List<Inventory> inventories = existsWarehouse.getInventories();
        List<InventoryTransaction> inventoryTransactions = existsWarehouse.getInventoryTransactions();
        for (Inventory inventory : inventories) inventory.setIsDeleted((byte) 1);
        for (InventoryTransaction inventoryTransaction : inventoryTransactions) inventoryTransaction.setIsDeleted((byte) 1);

        existsWarehouse.setInventories(inventories);
        existsWarehouse.setInventoryTransactions(inventoryTransactions);
        existsWarehouse.setIsDeleted((byte) 1);

        warehouseRepository.save(existsWarehouse);
    }

    @Override
    public WarehouseResponse getWarehouse(Long id) {
        User user = authService.getCurrentUser();
        Warehouse existsWarehouse = warehouseRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Warehouse not found"));
        if (!existsWarehouse.getUser().equals(user)) throw new UnauthorizedAccessException("Unauthorize to access this warehouse");

        List<InventoryResponse> inventoryResponses = existsWarehouse.getInventories().stream().filter(inventory -> inventory.getIsDeleted() == 0).map(inventory
                -> InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getId())
                .quantity(inventory.getQuantity())
                .build()
        ).toList();

        return WarehouseResponse.builder()
                .id(existsWarehouse.getId())
                .name(existsWarehouse.getName())
                .location(existsWarehouse.getLocation())
                .inventoryDTOS(inventoryResponses)
                .build();
    }

    @Override
    public List<WarehouseResponse> getWarehouses() {
        User user = authService.getCurrentUser();

        List<Warehouse> warehouses = warehouseRepository.getWarehouses(user.getId());

        return warehouses.stream().map(w -> WarehouseResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .location(w.getLocation())
                .build()).toList();
    }

    @Override
    public InventoryTransactionResponse getInventoryTransactionById(Long id) {
        InventoryTransaction inventoryTransaction = inventoryTransactionRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException("Inventory transaction not found!")
        );

        if (inventoryTransaction.getWarehouse().getUser().equals(authService.getCurrentUser())) throw new UnauthorizedAccessException("Unauthorize to access this resource");

        return InventoryTransactionResponse.builder()
                .id(inventoryTransaction.getId())
                .warehouseName(inventoryTransaction.getWarehouse().getName())
                .productName(inventoryTransaction.getProduct().getName())
                .quantity(inventoryTransaction.getQuantity())
                .resultingQuantity(inventoryTransaction.getResultingQuantity())
                .type(inventoryTransaction.getType())
                .createdAt(inventoryTransaction.getCreatedAt())
                .build();
    }

    @Override
    public PagedResponse<InventoryTransactionResponse> getListInventoryTransactions(int page, int size, String sortField, String sortDirection, String warehouseName, String productName) {
        size = Math.min(size, 10);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        User user = authService.getCurrentUser();

        Page<InventoryTransactionResponse> inventories = inventoryTransactionRepository
                .getListInventoryTransactions(pageable,user.getId(),warehouseName,productName)
                .map(i -> InventoryTransactionResponse.builder()
                        .id(i.getId())
                        .warehouseName(i.getWarehouse().getName())
                        .productName(i.getProduct().getName())
                        .quantity(i.getQuantity())
                        .resultingQuantity(i.getResultingQuantity())
                        .type(i.getType())
                        .createdAt(i.getCreatedAt())
                        .build());

        return  new PagedResponse<>(
                inventories.getContent(),
                inventories.getNumber(),
                inventories.getSize(),
                inventories.getTotalElements(),
                inventories.getTotalPages(),
                inventories.isLast()
        );
    }


}
