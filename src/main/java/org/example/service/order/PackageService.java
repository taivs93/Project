package org.example.service.order;

import org.example.dto.request.PackageDTO;
import org.example.dto.response.PackageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface PackageService {

    Double getExtraFee(java.util.List<org.example.dto.request.PackageProductDTO> items);

    Double getValue(java.util.List<org.example.dto.request.PackageProductDTO> items);

    Double getTotalFee(PackageDTO dto);

    PackageResponseDTO createDraftPackage(PackageDTO dto);

    PackageResponseDTO updatePackageStatus(Long id, int newStatus);

    Page<PackageResponseDTO> searchPackages(String customerTel, Long id, int page, int size, String sortField, String sortDirection);

    Page<PackageResponseDTO> searchDraftPackages(String customerTel, Long id, int page, int size, String sortField, String sortDirection);

    PackageResponseDTO submitDraft(Long draftId);

    PackageResponseDTO updateDraftPackage(Long id, PackageDTO dto);

    PackageResponseDTO getPackageById(Long id);

    Double getRevenue(@RequestParam String time);

    PackageResponseDTO findPackageById(Long id);

    Page<PackageResponseDTO> getAllPackages(int page, int size, String sortField, String sortDirection);
}
