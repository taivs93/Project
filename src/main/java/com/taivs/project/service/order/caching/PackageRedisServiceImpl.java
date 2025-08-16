package com.taivs.project.service.order.caching;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.taivs.project.dto.response.PackageResponseDTO;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class PackageRedisServiceImpl implements PackageRedisService {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    private static final String VERSION_KEY_PATTERN = "search::version::user::%d";


    @Override
    public Page<PackageResponseDTO> getCachedPackages(String cacheKey, Pageable pageable) {
        List<PackageResponseDTO> cachedList = (List<PackageResponseDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedList == null) return null;
        return new PageImpl<>(cachedList, pageable, cachedList.size());
    }

    @Override
    public void cachePackages(String cacheKey, List<PackageResponseDTO> dtoList, Duration ttl) {
        redisTemplate.opsForValue().set(cacheKey,dtoList,ttl);
    }

    @Override
    public String getUserCacheVersion(Long userId) {
        String key = String.format(VERSION_KEY_PATTERN,userId);
        String version;
        try{
            version = Objects.requireNonNull(redisTemplate.opsForValue().get(key)).toString();
        } catch (NullPointerException e){
            version = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(key,version,Duration.ofDays(1));
        }
        return version;
    }


    @Override
    public void bumpUserCacheVersion(Long userId) {
        String key = String.format(VERSION_KEY_PATTERN,userId);
        String newVersion = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(key,newVersion,Duration.ofDays(1));
    }
}

