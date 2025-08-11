package com.taivs.project.controller;

import com.taivs.project.dto.request.InventoryDTO;
import com.taivs.project.dto.response.InventoryResponse;
import com.taivs.project.dto.response.ResponseDTO;
import com.taivs.project.service.inventory.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/import")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> importInventory(@Valid @RequestBody InventoryDTO inventoryDTO){
        InventoryResponse inventoryResponse = inventoryService.importInventory(inventoryDTO);

        return ResponseEntity.ok(ResponseDTO.builder()
                        .status(200)
                        .message("Import successfully!")
                        .data(inventoryResponse)
                        .build());
    }

    @PostMapping("/export")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> exportInventory(@Valid @RequestBody InventoryDTO inventoryDTO){
        InventoryResponse inventoryResponse = inventoryService.exportInventory(inventoryDTO);

        return ResponseEntity.ok(ResponseDTO.builder()
                .status(200)
                .message("Export successfully!")
                .data(inventoryResponse)
                .build());
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> adjustInventory(@Valid @RequestBody InventoryDTO inventoryDTO){
        InventoryResponse inventoryResponse = inventoryService.adjustInventory(inventoryDTO);

        return ResponseEntity.ok(ResponseDTO.builder()
                .status(200)
                .message("Adjust inventory successfully!")
                .data(inventoryResponse)
                .build());
    }
}
