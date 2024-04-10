package ru.fcpsr.domainsport.services;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CacheService {
    private final CacheManager cacheManager;

    public void evictAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
    }

    public void evictCaches(String name){
        Objects.requireNonNull(cacheManager.getCache(name)).clear();
    }

    public Flux<Object> checkCacheContents(String cacheName) {
        CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
        Set<Object> cacheKeys = Objects.requireNonNull(cache).getNativeCache().asMap().keySet();
        return Flux.fromIterable(cacheKeys);
    }

    @Scheduled(fixedRate = 86400000) // Задайте нужный интервал
    public void evictAllCachesAtIntervals() {
        evictAllCaches();
    }
}
