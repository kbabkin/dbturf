package com.bt.dbturf.core.domain;

import com.bt.dbturf.core.adjust.Modification;
import com.bt.dbturf.core.adjust.RetrieveAdjuster;
import com.bt.dbturf.core.adjust.RetrieveRenameAdjuster;
import com.bt.dbturf.core.db.DbUpdater;
import com.bt.dbturf.core.util.Java8Fit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Example
 * <ul>
 * <li>others lookup by item field "customer", searched in "Customer"."name" table fields</li>
 * <li>self lookup by column "name"</li>
 * <li>technical id is "cu_id", it is added to ite, field "customer" is removed for others lookup</li>
 * </ul>
 */
@RequiredArgsConstructor
@Getter
public class NaturalKey {
    private final LinkedHashSet<String> naturalKeys;
    private final LinkedHashSet<String> technicalKeys;
    private final Modification technicalKeyLookupForSelf;
    // default lookup for others, can be different
    private final Modification technicalKeyLookupForOthersDefault;

    public static NaturalKey of(DbUpdater dbUpdater, Map<String, String> othersToSelfNaturalKey, Map<String, String> technicalKeyToOthers) {
        //todo default as part of key?
        LinkedHashSet<String> naturalKeys = new LinkedHashSet<>(othersToSelfNaturalKey.values());
        LinkedHashSet<String> technicalKeys = new LinkedHashSet<>(technicalKeyToOthers.keySet());
        TechnicalKeyLookupModification forSelf = TechnicalKeyLookupModification.of(dbUpdater,
                new RetrieveAdjuster(naturalKeys, true),
                new RetrieveAdjuster(technicalKeys, true),
                Modification.SAME);

        Set<String> fieldsToRemove = othersToSelfNaturalKey.keySet().stream()
                .filter(Java8Fit.not(othersToSelfNaturalKey.values()::contains))
                .filter(Java8Fit.not(technicalKeys::contains))
                .collect(Collectors.toSet());
        TechnicalKeyLookupModification forOthers = TechnicalKeyLookupModification.of(dbUpdater,
                new RetrieveRenameAdjuster(othersToSelfNaturalKey, true),
                new RetrieveRenameAdjuster(technicalKeyToOthers, true),
                fieldsToRemove.isEmpty() ? Modification.SAME : Modification.removeAll(fieldsToRemove));

        return new NaturalKey(naturalKeys, technicalKeys, forSelf, forOthers);
    }
}
