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
import java.util.UUID;

@Service
public class PackageRedisServiceImpl implements PackageRedisService {

    @Autowired
    private RedisService redisService;

    private static final String VERSION_KEY_PATTERN = "search::version::user::%d";

    private static final String ADMIN_VERSION_KEY = "search::version::admin";

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

    @Override
    public String getUserCacheVersion(Long userId) {
        String key = String.format(VERSION_KEY_PATTERN,userId);
        String version = redisService.get(key,String.class);
        if(version == null){
            version = UUID.randomUUID().toString();
            redisService.set(key,version,Duration.ofDays(1));
        }

        return version;
    }

    @Override
    public void bumpUserCacheVersion(Long userId) {
        String key = String.format(VERSION_KEY_PATTERN,userId);
        String newVersion = UUID.randomUUID().toString();
        redisService.set(key, newVersion, Duration.ofDays(1));
    }

    @Override
    public String getAdminCacheVersion() {
        String version = redisService.get(ADMIN_VERSION_KEY, String.class);
        if (version == null) {
            version = UUID.randomUUID().toString();
            redisService.set(ADMIN_VERSION_KEY, version, Duration.ofDays(1));
        }
        return version;
    }

    @Override
    public void bumpAdminCacheVersion() {
        String version = UUID.randomUUID().toString();
        redisService.set(ADMIN_VERSION_KEY, version, Duration.ofDays(1));
    }
}

