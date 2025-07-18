package com.taivs.project.service.order.caching;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.taivs.project.dto.response.PackageResponseDTO;
import com.taivs.project.service.redis.RedisService;

import java.time.Duration;
import java.util.List;

@Service
public class PackageRedisServiceImpl implements PackageRedisService {

    @Autowired
    private RedisService redisService;

    @Override
    public Page<PackageResponseDTO> getCachedPackages(String cacheKey, Pageable pageable) {
        List<PackageResponseDTO> cachedList = redisService.get(cacheKey, List.class);
        if (cachedList == null) return null;
        return new PageImpl<>(cachedList, pageable, cachedList.size());
    }

    @Override
    public void cachePackages(String cacheKey, List<PackageResponseDTO> dtoList, Duration ttl) {
        redisService.set(cacheKey, dtoList, ttl);
    }
}

