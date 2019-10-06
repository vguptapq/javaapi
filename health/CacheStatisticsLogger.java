package com.tdameritrade.microservice.xray.health;

import java.util.HashMap;
import java.util.Map;

import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CacheStatisticsLogger {
  private Map<String, Cache<?, ?>> cacheMap = new HashMap<>();
  private static final Logger LOG = LoggerFactory.getLogger("ops.cache");

  public void addToCacheMap(String cacheName, Cache<?, ?> cache) {
    cacheMap.put(cacheName, cache);
  }

  @Scheduled(fixedDelay = 5 * 60 * 1000)
  public void logCacheStatistics() {
    cacheMap.forEach((name, cache) -> {
      LOG.info("cachename={} size={} hitrate={} stats: {}",
          name, cache.asMap().keySet().size(), cache.stats().hitRate(), cache.stats());
    });
  }
}
