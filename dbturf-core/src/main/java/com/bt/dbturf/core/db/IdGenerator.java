package com.bt.dbturf.core.db;

import lombok.RequiredArgsConstructor;
import com.bt.dbturf.core.adjust.Modification;
import com.bt.dbturf.core.util.Java8Fit;

@RequiredArgsConstructor
public class IdGenerator<V> {
    private final String query;
    private final Class<V> clazz;

    public V generate() {
        return DbOperations.getInstance().nativeAsScalar(query, Java8Fit.mapOf(), clazz);
    }

    public Modification getModification(String fieldName) {
        return Modification.withDescription(getClass().getSimpleName(),
                example -> example.put(fieldName, generate()));
    }

    public static class LongIdGenerator extends IdGenerator<Long> {
        public LongIdGenerator(String query) {
            super(query, Long.class);
        }
    }

    public static class SeqIdGenerator extends LongIdGenerator {
        public SeqIdGenerator(TableId sequenceId) {
            super(String.format("select %s.nextval from dual", sequenceId.getFullName()));
        }
    }

    public static class MaxIdGenerator extends LongIdGenerator {
        public MaxIdGenerator(TableId tableId) {
            super(String.format("select coalesce(max(%s),0)+1 from %s", tableId.getKeys().iterator().next(), tableId.getFullName()));
        }
    }
}
