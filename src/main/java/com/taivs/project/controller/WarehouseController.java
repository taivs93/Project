package com.taivs.project.controller;

import com.taivs.project.dto.request.WarehouseDTO;
import com.taivs.project.dto.response.ResponseDTO;
import com.taivs.project.dto.response.WarehouseResponse;
import com.taivs.project.service.warehouse.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> getById(@PathVariable Long id){

        WarehouseResponse warehouseResponse = warehouseService.getWarehouse(id);
        return ResponseEntity.status(200).body(ResponseDTO.builder()
                        .status(200)
                        .message("Get warehouse successfully")
                        .data(warehouseResponse)
                .build());
    }

    @PostMapping("insert")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> insertWarehouse(@RequestBody @Valid WarehouseDTO warehouseDTO){
        WarehouseResponse warehouseResponse = warehouseService.insertWarehouse(warehouseDTO);
        return ResponseEntity.status(200).body(ResponseDTO.builder()
                .status(201)
                .message("Insert warehouse successfully")
                .data(warehouseResponse)
                .build());
    }

    @PutMapping("update/{id}")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> updateWarehouse(@PathVariable Long id, @RequestBody @Valid WarehouseDTO warehouseDTO){
        WarehouseResponse warehouseResponse = warehouseService.updateWarehouse(id,warehouseDTO);

        return ResponseEntity.status(200).body(ResponseDTO.builder()
                .status(200)
                .message("Update warehouse successfully")
                .data(warehouseResponse)
                .build());
    }

    @PutMapping("delete/{id}")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> deleteWarehouse(@PathVariable Long id){
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.status(200).body(ResponseDTO.builder()
                .status(204)
                .message("Delete warehouse successfully")
                .build());
    }

    @GetMapping("get-warehouses")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> getWarehouses(){
        List<WarehouseResponse> warehouseResponses = warehouseService.getWarehouses();

        return ResponseEntity.status(200).body(ResponseDTO.builder()
                .status(200)
                .data(warehouseResponses)
                .message("Get warehouses successfully")
                .build());

    }

}
