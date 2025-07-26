package com.taivs.project.controller;

import jakarta.validation.Valid;
import com.taivs.project.dto.request.PackageDTO;
import com.taivs.project.dto.request.PackageProductDTO;
import com.taivs.project.dto.request.StatusUpdateRequest;
import com.taivs.project.dto.response.PackageResponseDTO;
import com.taivs.project.dto.response.PagedResponse;
import com.taivs.project.dto.response.ResponseDTO;
import com.taivs.project.service.order.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/package")
public class PackageController {

    @Autowired
    private PackageService packageService;

    @PostMapping("/get-extra-fee")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> getExtraFee(@Valid @RequestBody List<PackageProductDTO> packageProductDTOS){
        Double extraFee = packageService.getExtraFee(packageProductDTOS);
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get extra fee successfully").data(extraFee).build());
    }

    @PostMapping("/get-value")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> getValue(@Valid @RequestBody List<PackageProductDTO> packageProductDTOS){
        Double value = packageService.getValue(packageProductDTOS);
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get value successfully").data(value).build());
    }

    @PostMapping("/get-total-fee")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> getTotalFee(@Valid @RequestBody PackageDTO packageDTO){
        Double totalFee = packageService.getTotalFee(packageDTO);
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get total fee successfully").data(totalFee).build());
    }

    @PostMapping("/insert-draft")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> insertDraftPackage(@Valid @RequestBody PackageDTO packageDTO){
        PackageResponseDTO newPackageResponse = packageService.createDraftPackage(packageDTO);
        return ResponseEntity.status(201).body(ResponseDTO.builder().status(201).message("Insert draft package successfully").data(newPackageResponse).build());
    }

    @PatchMapping("/submit-package/{id}")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> insertPackage(@PathVariable Long id){
        PackageResponseDTO newPackageResponse = packageService.submitDraft(id);
        return ResponseEntity.status(201).body(ResponseDTO.builder().status(201).message("Submit package successfully").data(newPackageResponse).build());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request
    ) {
        PackageResponseDTO packageResponseDTO = packageService.updatePackageStatus(id, request.getNewStatus());
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Update status successfully").data(packageResponseDTO).build());
    }

    @GetMapping("/search-packages")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> searchPackages(@RequestParam(required = false) String customerTel,
                                                      @RequestParam(required = false) Long id,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size,
                                                      @RequestParam(defaultValue = "id") String sortField,
                                                      @RequestParam(defaultValue = "DESC") String sortDirection){
        Page<PackageResponseDTO> packagePage = packageService.searchPackages(customerTel, id, page, size, sortField, sortDirection);

        PagedResponse<PackageResponseDTO> pagedResponse = new PagedResponse<>(
                packagePage.getContent(),
                packagePage.getNumber(),
                packagePage.getSize(),
                packagePage.getTotalElements(),
                packagePage.getTotalPages(),
                packagePage.isLast()
        );

        return ResponseEntity.ok(ResponseDTO.builder()
                .status(200)
                .message("Get packages successfully")
                .data(pagedResponse)
                .build());
    }

    @GetMapping("/search-draft-packages")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> searchDraftPackages(@RequestParam(required = false) String customerTel,
                                                           @RequestParam(required = false) Long id,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size,
                                                           @RequestParam(defaultValue = "id") String sortField,
                                                           @RequestParam(defaultValue = "DESC") String sortDirection){
        Page<PackageResponseDTO> packagePage = packageService.searchDraftPackages(customerTel, id, page, size, sortField, sortDirection);

        PagedResponse<PackageResponseDTO> pagedResponse = new PagedResponse<>(
                packagePage.getContent(),
                packagePage.getNumber(),
                packagePage.getSize(),
                packagePage.getTotalElements(),
                packagePage.getTotalPages(),
                packagePage.isLast()
        );

        return ResponseEntity.ok(ResponseDTO.builder()
                .status(200)
                .message("Get draft packages successfully")
                .data(pagedResponse)
                .build());
    }

    @PutMapping("/update-draft/{id}")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> updateDraftPackage(@PathVariable Long id, @Valid @RequestBody PackageDTO packageDTO){
        PackageResponseDTO packageResponseDTO = packageService.updateDraftPackage(id, packageDTO);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDTO.builder().status(200).message("Update draft package successfully").data(packageResponseDTO).build());
    }

    @GetMapping("/get-by-id/{id}")
    @PreAuthorize("hasRole('SHOP') or hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> getPackageById(@PathVariable("id") Long id){
        PackageResponseDTO packageResponseDTO = packageService.getPackageById(id);
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get package successfully!").data(packageService.findPackageById(id)).build());
    }

    @GetMapping("/get-packages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> getPackages(@RequestParam(required = false) Long userId,
                                                   @RequestParam(required = false) String customerTel,
                                                   @RequestParam(required = false) Long id,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "id") String sortField,
                                                   @RequestParam(defaultValue = "DESC") String sortDirection){
        Page<PackageResponseDTO> packageResponseDTOPage = packageService.getPackages(userId,customerTel,id,page,size,sortField,sortDirection);
        PagedResponse<PackageResponseDTO> pagedResponse = new PagedResponse<>(
                packageResponseDTOPage.getContent(),
                packageResponseDTOPage.getNumber(),
                packageResponseDTOPage.getSize(),
                packageResponseDTOPage.getTotalElements(),
                packageResponseDTOPage.getTotalPages(),
                packageResponseDTOPage.isLast()
        );

        return ResponseEntity.ok(ResponseDTO.builder()
                .status(200)
                .message("Get all packages successfully")
                .data(pagedResponse)
                .build());
    }

    @DeleteMapping("/delete-draft/{id}")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> deleteDraftPackageById(@PathVariable Long id){
        packageService.deleteDraftPackage(id);
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .status(204)
                        .message("Delete draft successfully")
                        .build()
        );
    }

    @PatchMapping("/cancel-package/{id}")
    @PreAuthorize("hasRole('SHOP')")
    public ResponseEntity<ResponseDTO> cancelPackage(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDTO.builder().status(200)
                .message("Cancel package successfully.")
                .data(packageService.cancelPackage(id))
                .build()
        );
    }
}
