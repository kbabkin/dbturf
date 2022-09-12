package com.bt.dbturf.core.domain;

import com.bt.dbturf.core.DbTurfContext;
import com.bt.dbturf.core.adjust.Adjuster;
import com.bt.dbturf.core.adjust.Modification;
import com.bt.dbturf.core.adjust.RetrieveRenameAdjuster;
import com.bt.dbturf.core.db.DbUpdater;
import com.bt.dbturf.core.util.Java8Fit;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor(staticName = "of")
public class TechnicalKeyLookupModification implements Modification {
    private final DbUpdater dbUpdater;
    private final Adjuster naturalKeyRetriever;
    // in referent table
    private final Adjuster internalKeyRetriever;

    private final Modification postProcessor;

    @Override
    public void modify(Map<String, Object> example) {
        Map<String, Object> findExample = naturalKeyRetriever.adjust(example);
        Map<String, Object> internalKeyValue = getCachedInternalKeys().computeIfAbsent(findExample, this::findInternalKey);
        example.putAll(internalKeyValue);
        postProcessor.modify(example);
    }

    Map<String, Object> findInternalKey(Map<String, Object> findExample) {
        List<Map<String, Object>> found = dbUpdater.findByEq(findExample);
        assertThat(found).as("Nothing found for %s: %s", dbUpdater.getTableId(), findExample).isNotNull();
        assertThat(found.size()).as("Expected single result for %s: %s, actual result: %s", dbUpdater.getTableId(), findExample, found)
                .isEqualTo(1);

        return internalKeyRetriever.adjust(found.get(0));
    }

    Map<Map<String, Object>, Map<String, Object>> getCachedInternalKeys() {
        Object lookups = DbTurfContext.getCurrentDbTurfContext().getProperties().computeIfAbsent("TechnicalKeyLookup", k -> new HashMap<>());
        return (Map) ((Map) lookups).computeIfAbsent(this, k -> new HashMap<>());
    }

    @Override
    public String toString() {
        return "TechnicalKeyLookupModification{" + dbUpdater.getTableId() + '}';
    }

    public static TechnicalKeyLookupModification of(
            DbUpdater dbUpdater,
            String naturalKeyReferring,
            String naturalKeyReferred,
            String internalKeyReferred,
            String internalKeyReferring) {
        return new TechnicalKeyLookupModification(dbUpdater,
                new RetrieveRenameAdjuster(Java8Fit.mapOf(naturalKeyReferring, naturalKeyReferred), true),
                new RetrieveRenameAdjuster(Java8Fit.mapOf(internalKeyReferred, internalKeyReferring), true),
                Modification.remove(naturalKeyReferring));
    }
}
