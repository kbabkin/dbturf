package com.bt.dbturf.core.adjust;

import com.bt.dbturf.core.util.Java8Fit;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Transform basic object representation Map<String, Object> in functional
 * style.
 * <br>
 * Benefits:
 * <ul>
 * <li>Apply for multiple items</li>
 * <li>Use lambdas to for better code decomposition and recomposition</li>
 * </ul>
 */
@FunctionalInterface
public interface Adjuster {

    Map<String, Object> adjust(Map<String, Object> example);

    default List<Map<String, Object>> adjustAll(List<Map<String, Object>> examples) {
        return examples.isEmpty() ? Java8Fit.listOf() : examples.stream()
                .map(this::adjust)
                .collect(Collectors.toList());
    }

    Adjuster SAME = example -> example;

    default Adjuster and(Adjuster next) {
        return new Adjuster() {
            @Override
            public Map<String, Object> adjust(Map<String, Object> example) {
                Map<String, Object> own = Adjuster.this.adjust(example);
                return next.adjust(own);
            }

            @Override
            public String toString() {
                return this + " and " + next;
            }
        };
    }
}
