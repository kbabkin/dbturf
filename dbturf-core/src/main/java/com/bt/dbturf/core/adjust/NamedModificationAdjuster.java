package com.bt.dbturf.core.adjust;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Modifications are named to handle complex configurations.
 * <ol>
 * <li>Basic configuration has common set of modifications</li>
 * <li>Actual usage adds/removes only required modifications over basic configuration</li>
 * </ol>
 */
public class NamedModificationAdjuster implements Adjuster, Modification {
    private final Map<String, Modification> modificationsNyName;

    public NamedModificationAdjuster() {
        this(new LinkedHashMap<>());
    }

    public NamedModificationAdjuster(LinkedHashMap<String, Modification> modificationsNyName) {
        this.modificationsNyName = Collections.unmodifiableMap(modificationsNyName);
    }

    @Override
    public Map<String, Object> adjust(Map<String, Object> example) {
        Map<String, Object> result = example == null ? new HashMap<>() : new HashMap<>(example);
        modify(result);
        return result;
    }

    @Override
    public void modify(Map<String, Object> example) {
        modificationsNyName.values().forEach(modification -> modification.modify(example));
    }

    @Override
    public String toString() {
        return "Modifications: " + modificationsNyName;
    }

    public NamedModificationAdjuster addFirst(String name, Modification modification) {
        LinkedHashMap<String, Modification> newMap = new LinkedHashMap<>();
        newMap.put(name, modification);
        modificationsNyName.forEach(newMap::putIfAbsent);
        return new NamedModificationAdjuster(newMap);
    }

    public NamedModificationAdjuster add(String name, Modification modification) {
        LinkedHashMap<String, Modification> newMap = new LinkedHashMap<>(modificationsNyName);
        newMap.put(name, modification);
        return new NamedModificationAdjuster(newMap);
    }

    public NamedModificationAdjuster addAll(Map<String, Modification> modifications) {
        LinkedHashMap<String, Modification> newMap = new LinkedHashMap<>(modificationsNyName);
        newMap.putAll(modifications);
        return new NamedModificationAdjuster(newMap);
    }

    public NamedModificationAdjuster addBefore(String beforeName, String name, Modification modification) {
        assertThat(modificationsNyName.containsKey(beforeName)).as("Cannot insert before %s", beforeName).isTrue();

        LinkedHashMap<String, Modification> newMap = new LinkedHashMap<>();
        modificationsNyName.forEach((n, m) -> {
            if (beforeName.equals(n)) {
                newMap.put(name, modification);
            }
            if (!name.equals(n)) {
                newMap.put(n, m);
            }
        });
        return new NamedModificationAdjuster(newMap);
    }

    public NamedModificationAdjuster addAfter(String afterNameName, String name, Modification modification) {
        assertThat(modificationsNyName.containsKey(afterNameName)).as("Cannot insert after %s", afterNameName).isTrue();

        LinkedHashMap<String, Modification> newMap = new LinkedHashMap<>();
        modificationsNyName.forEach((n, m) -> {
            if (!name.equals(n)) {
                newMap.put(n, m);
            }
            if (afterNameName.equals(n)) {
                newMap.put(name, modification);
            }
        });
        return new NamedModificationAdjuster(newMap);
    }

    public NamedModificationAdjuster remove(String name) {
        LinkedHashMap<String, Modification> newMap = new LinkedHashMap<>(modificationsNyName);
        newMap.remove(name);
        return new NamedModificationAdjuster(newMap);
    }

}