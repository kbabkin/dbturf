package com.bt.dbturf.core.item;

import lombok.Value;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.fail;

@UtilityClass
public class DiffItem {
    @Value
    public static class ItemDiff {
        Set<String> changed;
        Map<String, Object> expected;
        Map<String, Object> actual;

        public boolean isMatched() {
            return changed.isEmpty();
        }

        public void assertEquals(String messageFormat, Object... args) {
            if (isMatched()) {
                return;
            }
            fail(String.format("Error: %s, Diff: %s",
                    String.format(messageFormat, args),
                    changed.stream()
                            .map(k -> String.format("%s expected '%s' actual '%s'", k, expected.get(k), actual.get(k)))
                            .collect(Collectors.joining(", "))));
        }

    }

    @Value
    public static class ListDiff {
        int same;
        List<Map<String, Object>> missing;
        List<Map<String, Object>> unexpected;

        public boolean isMatched() {
            return missing.isEmpty() && unexpected.isEmpty();
        }

        public void assertEquals(String messageFormat, Object... args) {
            if (isMatched()) {
                return;
            }
            if (missing.size() == 1 && unexpected.size() == 1) {
                diff(missing.get(0), unexpected.get(0)).assertEquals(messageFormat, args);
            }
            fail(String.format("Error: %s, Same: %s, Missing: %s, Unexpected: %s ", String.format(messageFormat, args), same, missing, unexpected));
        }

    }

    public ListDiff diff(List<Map<String, Object>> expectedList, List<Map<String, Object>> actualList) {
        List<Map<String, Object>> missing = new ArrayList<>();
        List<Map<String, Object>> unexpected = new ArrayList<>(actualList);
        for (Map<String, Object> expected : expectedList) {
            boolean found = false;
            for (Iterator<Map<String, Object>> i = unexpected.iterator(); i.hasNext(); ) {
                Map<String, Object> actual = i.next();
                if (diff(expected, actual).isMatched()) {
                    found = true;
                    i.remove();
                    break;
                }
            }
            if (!found) {
                missing.add(expected);
            }
        }
        return new ListDiff(expectedList.size() - missing.size(), missing, unexpected);
    }

    public ItemDiff diff(Map<String, Object> expected, Map<String, Object> actual) {
        return new ItemDiff(expected.entrySet().stream()
                .filter(e -> !valueEquals(e.getValue(), actual.get(e.getKey())))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet()),
                expected, actual);
    }

    public boolean valueEquals(Object expected, Object actual) {
        if (expected == null && actual == null) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }
        if (expected.equals(actual)) {
            return true;
        }
        // todo compare by type
        return expected.toString().equals(actual.toString());
    }
}
