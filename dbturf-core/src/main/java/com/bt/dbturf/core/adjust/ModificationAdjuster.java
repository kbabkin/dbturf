package com.bt.dbturf.core.adjust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adjuster copies example one time only, then multiple modifications are
 * applied without additional copying.
 */
public class ModificationAdjuster implements Modification, Adjuster {
    private final List<Modification> modifications;

    public ModificationAdjuster(List<Modification> modifications) {
        this.modifications = Collections.unmodifiableList(modifications.stream()
                .filter(m -> !Modification.SAME.equals(m))
                .collect(Collectors.toList()));
    }

    public ModificationAdjuster(Modification modification) {
        this(Collections.singletonList(modification));
    }

    public ModificationAdjuster() {
        this(Collections.emptyList());
    }

    @Override
    public Map<String, Object> adjust(Map<String, Object> example) {
        Map<String, Object> result = example == null ? new HashMap<>() : new HashMap<>(example);
        modify(result);
        return result;
    }

    @Override
    public void modify(Map<String, Object> example) {
        modifications.forEach(modification -> modification.modify(example));
    }

    @Override
    public String toString() {
        return "Modifications: " + modifications;
    }

    public ModificationAdjuster andModification(Modification modification) {
        if (Modification.SAME == modification) {
            return this;
        }
        List<Modification> newList = new ArrayList<>(this.modifications);
        newList.add(modification);
        return new ModificationAdjuster(newList);
    }

    public ModificationAdjuster andModification(ModificationAdjuster modificationAdjuster) {
        List<Modification> newList = new ArrayList<>(this.modifications);
        // recursive?
        newList.addAll(modificationAdjuster.modifications);
        return new ModificationAdjuster(newList);
    }

}
