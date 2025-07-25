package com.taivs.project.service.order.caching;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.taivs.project.dto.response.PackageResponseDTO;

import java.time.Duration;
import java.util.List;

public interface PackageRedisService {

    Page<PackageResponseDTO> getCachedPackages(String cacheKey, Pageable pageable);

    void cachePackages(String cacheKey, List<PackageResponseDTO> dtoList, Duration ttl);

    String getUserCacheVersion(Long userId);

    void bumpUserCacheVersion(Long userId);

}

