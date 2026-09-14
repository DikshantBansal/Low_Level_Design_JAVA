package com.lld.cache;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Thread-safe O(1) LFU cache; ties are evicted by least-recent use. */
public final class LfuCache<K, V> implements Cache<K, V> {
    private final int capacity;
    private final Map<K, Entry<V>> entries = new HashMap<>();
    private final Map<Integer, LinkedHashSet<K>> frequencies = new HashMap<>();
    private int minimumFrequency;

    public LfuCache(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        this.capacity = capacity;
    }

    public synchronized Optional<V> get(K key) {
        Entry<V> entry = entries.get(key);
        if (entry == null) return Optional.empty();
        increment(key, entry);
        return Optional.of(entry.value);
    }

    public synchronized void put(K key, V value) {
        Objects.requireNonNull(key); Objects.requireNonNull(value);
        Entry<V> existing = entries.get(key);
        if (existing != null) { existing.value = value; increment(key, existing); return; }
        if (entries.size() == capacity) {
            LinkedHashSet<K> leastUsed = frequencies.get(minimumFrequency);
            K evicted = leastUsed.iterator().next(); leastUsed.remove(evicted); entries.remove(evicted);
            if (leastUsed.isEmpty()) frequencies.remove(minimumFrequency);
        }
        entries.put(key, new Entry<>(value)); frequencies.computeIfAbsent(1, ignored -> new LinkedHashSet<>()).add(key); minimumFrequency = 1;
    }

    public synchronized boolean remove(K key) {
        Entry<V> entry = entries.remove(key); if (entry == null) return false;
        LinkedHashSet<K> keys = frequencies.get(entry.frequency); keys.remove(key); if (keys.isEmpty()) frequencies.remove(entry.frequency);
        if (entry.frequency == minimumFrequency && !entries.isEmpty() && !frequencies.containsKey(minimumFrequency)) minimumFrequency = frequencies.keySet().stream().min(Integer::compareTo).orElse(0);
        return true;
    }
    public synchronized int size() { return entries.size(); }

    private void increment(K key, Entry<V> entry) {
        int old = entry.frequency; LinkedHashSet<K> keys = frequencies.get(old); keys.remove(key);
        if (keys.isEmpty()) { frequencies.remove(old); if (minimumFrequency == old) minimumFrequency++; }
        entry.frequency++; frequencies.computeIfAbsent(entry.frequency, ignored -> new LinkedHashSet<>()).add(key);
    }

    private static final class Entry<V> { private V value; private int frequency = 1; private Entry(V value) { this.value = value; } }
}
