package com.bt.dbturf.core.adjust;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Value
@RequiredArgsConstructor
public class RetrieveRenameAdjuster implements Adjuster {
    Map<String, String> fieldsMapping;
    boolean requireAll;

    @Override
    public Map<String, Object> adjust(Map<String, Object> example) {
        Map<String, Object> result;
        if (fieldsMapping.size() == 1) {
            Map.Entry<String, String> entry = fieldsMapping.entrySet().iterator().next();
            Object value = example.get(entry.getKey());
            result = (value == null) ? Collections.emptyMap() : Collections.singletonMap(entry.getValue(), value);
        } else {
            result = fieldsMapping.entrySet().stream()
                    .filter(e -> example.containsKey(e.getKey()))
                    .map(e -> new AbstractMap.SimpleEntry<>(e.getValue(), example.get(e.getKey())))
                    .filter(e -> e.getValue() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        if (requireAll) {
            assertThat(result.size())
                    .as("Missing some values for %s: %s", fieldsMapping.keySet(), result)
                    .isEqualTo(fieldsMapping.size());
        }
        return result;
    }

}
