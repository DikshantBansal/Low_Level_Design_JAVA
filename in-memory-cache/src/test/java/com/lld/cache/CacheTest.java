package com.lld.cache;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CacheTest {
    @Test void lruEvictsLeastRecentlyUsed() {
        Cache<String, Integer> cache = new LruCache<>(2);
        cache.put("a", 1); cache.put("b", 2); cache.get("a"); cache.put("c", 3);
        assertTrue(cache.get("b").isEmpty()); assertEquals(1, cache.get("a").orElseThrow());
    }

    @Test void lfuEvictsLeastFrequentlyUsed() {
        Cache<String, Integer> cache = new LfuCache<>(2);
        cache.put("a", 1); cache.put("b", 2); cache.get("a"); cache.put("c", 3);
        assertTrue(cache.get("b").isEmpty()); assertEquals(1, cache.get("a").orElseThrow());
    }

    @Test void lfuBreaksFrequencyTiesByRecency() {
        Cache<String, Integer> cache = new LfuCache<>(2);
        cache.put("a", 1); cache.put("b", 2); cache.put("c", 3);
        assertTrue(cache.get("a").isEmpty());
    }
}
