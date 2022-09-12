package com.bt.dbturf.core.adjust;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Value
@RequiredArgsConstructor
public class RetrieveAdjuster implements Adjuster {
    Set<String> fields;
    boolean requireAll;

    @Override
    public Map<String, Object> adjust(Map<String, Object> example) {
        Map<String, Object> result;
        if (fields.size() == 1) {
            String key = fields.iterator().next();
            Object value = example.get(key);
            result = (value == null) ? Collections.emptyMap() : Collections.singletonMap(key, value);
        } else {
            result = fields.stream()
                    .filter(example::containsKey)
                    .filter(k -> example.get(k) != null)
                    .collect(Collectors.toMap(Function.identity(), example::get));
        }

        if (requireAll) {
            assertThat(result.size())
                    .as("Missing some values for %s: %s", fields, result)
                    .isEqualTo(fields.size());
        }
        return result;
    }

}
