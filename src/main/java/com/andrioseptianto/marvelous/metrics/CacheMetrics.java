package com.andrioseptianto.marvelous.metrics;

import com.andrioseptianto.marvelous.service.MarvelServiceImpl;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
public class CacheMetrics {

    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;

    private static final Logger logger = LoggerFactory.getLogger(CacheMetrics.class);


    @Autowired
    public CacheMetrics(MeterRegistry meterRegistry) {
        this.cacheHitCounter = meterRegistry.counter("cache_hits");
        this.cacheMissCounter = meterRegistry.counter("cache_misses");
    }

    public void incrementCacheHit() {
        cacheHitCounter.increment();
        logger.info("Cache hit count: {}", cacheHitCounter.count());
    }

    public void incrementCacheMiss() {
        cacheMissCounter.increment();
        logger.info("Cache miss count: {}", cacheHitCounter.count());

    }
}