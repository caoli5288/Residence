package com.bekvon.bukkit.residence.utils;

import java.util.Objects;

public class Pair<K, V> {

    private final K key;
    private V value;

    public Pair(K key, V value) {
        this.key = Objects.requireNonNull(key);
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) other;
        return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return ("Pair{" + "key=" + key +
                ", value=" + value +
                '}');
    }
}
