package com.bt.dbturf.core.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Simulate nice features of newer Java versions and still allow to work on Java 8
 */
@UtilityClass
public class Java8Fit {

    public <V> Predicate<V> not(Predicate<V> p) {
        return p.negate();
    }

    public <V> V create(Supplier<V> supplier) {
        return supplier.get();
    }

    public <P1, V> V create(P1 p1, Function<P1, V> supplier) {
        return supplier.apply(p1);
    }

    public <K, V> Map<K, V> mapOf() {
        return Collections.emptyMap();
    }

    public <K, V> Map<K, V> mapOf(K k1, V v1) {
        return Collections.singletonMap(k1, v1);
    }

    public <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return Collections.unmodifiableMap(map);
    }

    public <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return Collections.unmodifiableMap(map);
    }

    public <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        return Collections.unmodifiableMap(map);
    }

    public <V> List<V> listOf() {
        return Collections.emptyList();
    }

    public <V> List<V> listOf(V v1) {
        return Collections.singletonList(v1);
    }

    public <V> List<V> listOf(V v1, V v2) {
        List<V> list = new ArrayList<>();
        list.add(v1);
        list.add(v2);
        return Collections.unmodifiableList(list);
    }

    public <V> List<V> listOf(V v1, V v2, V v3) {
        List<V> list = new ArrayList<>();
        list.add(v1);
        list.add(v2);
        list.add(v3);
        return Collections.unmodifiableList(list);
    }

    public <V> Set<V> setOf() {
        return Collections.emptySet();
    }

    public <V> Set<V> setOf(V v1) {
        return Collections.singleton(v1);
    }

    public <V> Set<V> setOf(V v1, V v2) {
        HashSet<V> set = new HashSet<>();
        set.add(v1);
        set.add(v2);
        return Collections.unmodifiableSet(set);
    }

    public <V> Set<V> setOf(V v1, V v2, V v3) {
        HashSet<V> set = new HashSet<>();
        set.add(v1);
        set.add(v2);
        set.add(v3);
        return Collections.unmodifiableSet(set);
    }

    public <V> Set<V> setOf(V v1, V v2, V v3, V v4) {
        HashSet<V> set = new HashSet<>();
        set.add(v1);
        set.add(v2);
        set.add(v3);
        set.add(v4);
        return Collections.unmodifiableSet(set);
    }

    public <V> Set<V> setOf(V v1, V v2, V v3, V v4, V v5) {
        HashSet<V> set = new HashSet<>();
        set.add(v1);
        set.add(v2);
        set.add(v3);
        set.add(v4);
        set.add(v5);
        return Collections.unmodifiableSet(set);
    }

    public <V> Set<V> setOf(V v1, V v2, V v3, V v4, V v5, V v6) {
        HashSet<V> set = new HashSet<>();
        set.add(v1);
        set.add(v2);
        set.add(v3);
        set.add(v4);
        set.add(v5);
        set.add(v6);
        return Collections.unmodifiableSet(set);
    }

}
