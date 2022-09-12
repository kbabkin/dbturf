package com.bt.dbturf.core.adjust;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

@FunctionalInterface
public interface Modification {
    void modify(Map<String, Object> example);

    default ModificationAdjuster toAdjuster() {
        return new ModificationAdjuster(this);
    }

    Modification SAME = example -> {
    };

    static Modification withDescription(String description, Modification modification) {
        return new Modification() {
            @Override
            public void modify(Map<String, Object> example) {
                modification.modify(example);
            }

            @Override
            public String toString() {
                return description;
            }
        };
    }

    static Modification putIfAbsent(String key, Object value) {
        return withDescription("putIfAbsent", example -> example.putIfAbsent(key, value));
    }

    static Modification putAllIfAbsent(Map<String, Object> defaults) {
        return withDescription("putAllIfAbsent", example -> defaults.forEach(example::putIfAbsent));
    }

    static Modification put(String key, Object value) {
        return withDescription("put", example -> example.put(key, value));
    }

    static Modification putAll(Map<String, Object> overrides) {
        return withDescription("putAll", example -> example.putAll(overrides));
    }

    static Modification remove(String key) {
        return withDescription("remove", example -> example.remove(key));
    }

    static Modification removeAll(Collection<String> keySet) {
        return withDescription("removeAll", example -> example.keySet().removeAll(keySet));
    }

    static Modification removeIf(Predicate<String> keyMatcher) {
        return withDescription("removeIf", example -> example.keySet().removeIf(keyMatcher));
    }

    static Modification retainAll(Collection<String> keys) {
        return withDescription("retainAll", example -> example.keySet().retainAll(keys));
    }

    static Modification rename(String from, String to) {
        return withDescription("rename", example -> {
            if (example.containsKey(from)) {
                Object value = example.remove(from);
                example.put(to, value);
            }
        });
    }

}