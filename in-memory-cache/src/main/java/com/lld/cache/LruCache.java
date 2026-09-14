package com.lld.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Thread-safe O(1) LRU cache using a hash map and doubly linked list. */
public final class LruCache<K, V> implements Cache<K, V> {
    private final int capacity;
    private final Map<K, Node<K, V>> nodes = new HashMap<>();
    private final Node<K, V> head = new Node<>(null, null);
    private final Node<K, V> tail = new Node<>(null, null);

    public LruCache(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        this.capacity = capacity; head.next = tail; tail.previous = head;
    }

    public synchronized Optional<V> get(K key) {
        Node<K, V> node = nodes.get(key);
        if (node == null) return Optional.empty();
        moveToFront(node);
        return Optional.of(node.value);
    }

    public synchronized void put(K key, V value) {
        Objects.requireNonNull(key); Objects.requireNonNull(value);
        Node<K, V> existing = nodes.get(key);
        if (existing != null) { existing.value = value; moveToFront(existing); return; }
        Node<K, V> node = new Node<>(key, value); nodes.put(key, node); addFirst(node);
        if (nodes.size() > capacity) { Node<K, V> evicted = tail.previous; unlink(evicted); nodes.remove(evicted.key); }
    }

    public synchronized boolean remove(K key) {
        Node<K, V> node = nodes.remove(key); if (node == null) return false; unlink(node); return true;
    }
    public synchronized int size() { return nodes.size(); }
    private void moveToFront(Node<K, V> node) { unlink(node); addFirst(node); }
    private void addFirst(Node<K, V> node) { node.next = head.next; node.previous = head; head.next.previous = node; head.next = node; }
    private void unlink(Node<K, V> node) { node.previous.next = node.next; node.next.previous = node.previous; }

    private static final class Node<K, V> {
        private final K key; private V value; private Node<K, V> previous; private Node<K, V> next;
        private Node(K key, V value) { this.key = key; this.value = value; }
    }
}
