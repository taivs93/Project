package com.taivs.project.service.order;

import com.taivs.project.dto.request.PackageDTO;
import com.taivs.project.dto.request.PackageProductDTO;
import com.taivs.project.dto.response.PackageResponseDTO;
import com.taivs.project.dto.response.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

public interface PackageService {

    Double getExtraFee(java.util.List<PackageProductDTO> items);

    Double getValue(java.util.List<PackageProductDTO> items);

    Double getTotalFee(PackageDTO dto);

    PackageResponseDTO createDraftPackage(PackageDTO dto);

    PackageResponseDTO updatePackageStatus(Long id, int newStatus);

    PagedResponse<PackageResponseDTO> searchPackages(String customerTel, Long id, int page, int size, String sortField, String sortDirection);

    PagedResponse<PackageResponseDTO> searchDraftPackages(String customerTel, Long id, int page, int size, String sortField, String sortDirection);

    PackageResponseDTO submitDraft(Long draftId);

    PackageResponseDTO updateDraftPackage(Long id, PackageDTO dto);

    PackageResponseDTO getPackageById(Long id);

    Double getRevenue(@RequestParam String time);

    PackageResponseDTO findPackageById(Long id);

    PagedResponse<PackageResponseDTO> getPackages(Long userId, String customerTel, Long id, int page, int size, String sortField, String sortDirection);

    void deleteDraftPackage(Long id);

    PackageResponseDTO cancelPackage(Long id);
}
