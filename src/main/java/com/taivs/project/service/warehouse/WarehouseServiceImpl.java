package com.taivs.project.service.warehouse;

import com.taivs.project.dto.request.WarehouseDTO;
import com.taivs.project.dto.response.WarehouseResponse;
import com.taivs.project.entity.Inventory;
import com.taivs.project.entity.InventoryTransaction;
import com.taivs.project.entity.User;
import com.taivs.project.entity.Warehouse;
import com.taivs.project.exception.DataNotFoundException;
import com.taivs.project.exception.ResourceAlreadyExistsException;
import com.taivs.project.exception.UnauthorizedAccessException;
import com.taivs.project.repository.WarehouseRepository;
import com.taivs.project.service.auth.AuthService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseServiceImpl implements WarehouseService{

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private AuthService authService;

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

        return WarehouseResponse.builder()
                .name(existsWarehouse.getName())
                .location(existsWarehouse.getLocation())
                .build();
    }

    @Override
    @Transactional
    public void deleteWarehouse(Long id) {
        User user = authService.getCurrentUser();
        Warehouse existsWarehouse = warehouseRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Warehouse not found"));
        if (!existsWarehouse.getUser().equals(user)) throw new UnauthorizedAccessException("Unauthorize to access this warehouse");

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

        return WarehouseResponse.builder()
                .id(existsWarehouse.getId())
                .name(existsWarehouse.getName())
                .location(existsWarehouse.getLocation())
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


}
