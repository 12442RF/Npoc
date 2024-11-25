package com.attempt.npoc.utils;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import java.util.function.BiConsumer;

public class SyncLockMap<K, V> {
    private final AtomicBoolean readOnly = new AtomicBoolean(false);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ConcurrentHashMap<K, V> map;

    public SyncLockMap() {
        this.map = new ConcurrentHashMap<>();
    }

    public SyncLockMap(Map<K, V> initialMap) {
        this.map = new ConcurrentHashMap<>(initialMap);
    }

    public void lock() {
        readOnly.set(true);
    }

    public void unlock() {
        readOnly.set(false);
    }

    public boolean set(K key, V value) {
        if (readOnly.get()) {
            throw new IllegalStateException("Map is currently in read-only mode");
        }

        lock.writeLock().lock();
        try {
            map.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    public V get(K key) {
        lock.readLock().lock();
        try {
            return map.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean delete(K key) {
        lock.writeLock().lock();
        try {
            map.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    public void iterate(BiConsumer<K, V> action) {
        lock.readLock().lock();
        try {
            map.forEach(action);
        } finally {
            lock.readLock().unlock();
        }
    }

    public SyncLockMap<K, V> cloneMap() {
        lock.readLock().lock();
        try {
            return new SyncLockMap<>(new ConcurrentHashMap<>(map));
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean has(K key) {
        lock.readLock().lock();
        try {
            return map.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return map.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            map.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public K getKeyWithValue(V value) {
        lock.readLock().lock();
        try {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (entry.getValue().equals(value)) {
                    return entry.getKey();
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return null;
    }

    public void merge(Map<K, V> otherMap) {
        if (readOnly.get()) {
            throw new IllegalStateException("Map is currently in read-only mode");
        }

        lock.writeLock().lock();
        try {
            map.putAll(otherMap);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Map<K, V> getAll() {
        lock.readLock().lock();
        try {
            return new ConcurrentHashMap<>(map);
        } finally {
            lock.readLock().unlock();
        }
    }

    public static void main(String[] args) {
        // 创建一个新的SyncLockMap实例
        SyncLockMap<String, Integer> syncLockMap = new SyncLockMap<>();

        // 添加元素
        syncLockMap.set("key1", 1);
        syncLockMap.set("key2", 2);

        // 获取元素
        Integer value1 = syncLockMap.get("key1");
        System.out.println("key1: " + value1);

        // 检查键是否存在
        boolean hasKey2 = syncLockMap.has("key2");
        System.out.println("Has key2: " + hasKey2);

        // 删除元素
        syncLockMap.delete("key1");
        boolean hasKey1 = syncLockMap.has("key1");
        System.out.println("Has key1 after deletion: " + hasKey1);

        // 清空所有元素
        syncLockMap.clear();
        boolean isEmpty = syncLockMap.isEmpty();
        System.out.println("Is map empty after clear: " + isEmpty);

        // 添加一些新元素
        syncLockMap.set("key3", 3);
        syncLockMap.set("key4", 4);

        // 获取所有元素
        Map<String, Integer> allElements = syncLockMap.getAll();
        System.out.println("All elements: " + allElements);

        // 克隆当前的Map
        SyncLockMap<String, Integer> clonedMap = syncLockMap.cloneMap();
        System.out.println("Cloned map: " + clonedMap.getAll());

        // 迭代所有元素
        syncLockMap.iterate((k, v) -> System.out.println(k + ": " + v));

        // 合并另一个Map
        Map<String, Integer> newMap = new HashMap<>();
        newMap.put("key5", 5);
        newMap.put("key6", 6);
        syncLockMap.merge(newMap);
        System.out.println("After merging new map: " + syncLockMap.getAll());

        // 锁定Map为只读模式
        syncLockMap.lock();

        try {
            // 尝试在只读模式下添加元素
            syncLockMap.set("key7", 7);
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // 解锁Map
        syncLockMap.unlock();

        // 现在可以添加元素
        syncLockMap.set("key7", 7);
        System.out.println("After unlocking and adding key7: " + syncLockMap.getAll());
    }
}
