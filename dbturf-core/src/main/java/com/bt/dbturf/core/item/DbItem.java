package com.bt.dbturf.core.item;

import com.bt.dbturf.core.DbTurfContext;
import com.bt.dbturf.core.db.DbUpdater;
import com.bt.dbturf.core.db.TableId;
import com.bt.dbturf.core.util.Java8Fit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import com.bt.dbturf.core.adjust.Adjuster;
import com.bt.dbturf.core.adjust.Modification;
import com.bt.dbturf.core.adjust.NamedModificationAdjuster;
import com.bt.dbturf.core.domain.ItemKeys;
import com.bt.dbturf.core.domain.NaturalKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

// load/save - db
// read/write - json

@Slf4j
@Getter
@RequiredArgsConstructor
public class DbItem {

    private final ItemKeys itemKeys;
    private final NamedModificationAdjuster readAdjuster;
    private final Adjuster conflictKeyExtractor; //todo init for child

    public DbUpdater getDbUpdater() {
        return itemKeys.getDbUpdater();
    }

    public NaturalKey getNaturalKey() {
        return itemKeys.getNaturalKey();
    }

    public TableId getTableId() {
        return itemKeys.getTableId();
    }

    public List<Map<String, Object>> save(List<Map<String, Object>> items, Modification overrides) {
        NamedModificationAdjuster reader = overrides == null ? readAdjuster
                : readAdjuster.addAfter(AdjusterBuilder.DEFAULTS, AdjusterBuilder.OVERRIDE, overrides);//todo builder

        List<Map<String, Object>> itemsToSave = items.stream()
                .map(reader::adjust)
                .collect(Collectors.toList());

        List<Map<String, Object>> itemsToRemove = getConflictItems(itemsToSave);
        if (!itemsToRemove.isEmpty()) {
            itemsToRemove.forEach(getDbUpdater()::remove);
        }

        return itemsToSave.stream()
                .peek(getDbUpdater()::merge)
                .map(e -> getTableId().getKeyValues(e))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> save(List<Map<String, Object>> examples) {
        return save(examples, null);
    }

    /**
     * Cleanup tricky conflicts like other items in saved child collection, conflicts in non-PK unique index, etc.
     */
    List<Map<String, Object>> getConflictItems(List<Map<String, Object>> itemsToSave) {
        if (conflictKeyExtractor == null) {
            return Java8Fit.listOf();
        }

        TableId tableId = getTableId();
        Set<Map<String, Object>> childrenKeys = itemsToSave.stream()
                .map(tableId::getKeyValues)
                .collect(Collectors.toSet());

        return itemsToSave.stream()
                .map(conflictKeyExtractor::adjust)
                .distinct()
                .map(getDbUpdater()::findByEq)
                .flatMap(Collection::stream)
                .map(tableId::getKeyValues)
                .filter(Java8Fit.not(childrenKeys::contains))
                .collect(Collectors.toList());
    }


    public static ParentDbItem of(ItemKeys itemKeys) {
        return parent(itemKeys);
    }

    public static ParentDbItem parent(ItemKeys itemKeys) {
        return new ParentDbItem(itemKeys, AdjusterBuilder.withDefaults(itemKeys.getJsonItem()).getAdjuster()
                .addFirst(AdjusterBuilder.FILL_BY_NAME, ParentDbItem.getFillByNameModification(itemKeys.getJsonItem())),
                null, Java8Fit.listOf());
    }

    public static ChildDbItem childCollection(ItemKeys itemKeys, ItemKeys parentItemKeys) {
        return new ChildDbItem(itemKeys, AdjusterBuilder.withDefaults(itemKeys.getJsonItem()).getAdjuster(),
                parentItemKeys.getTableId().getKeysRetriever(), null);

    }

    public static ChildDbItem childStandalone(ItemKeys itemKeys, ItemKeys parentItemKeys) {
        return new ChildDbItem(itemKeys, AdjusterBuilder.withDefaults(itemKeys.getJsonItem())
                .internalKeyLookup(parentItemKeys.getNaturalKey().getTechnicalKeyLookupForOthersDefault())
                .getAdjuster(),
                null, null);
    }

    public static class AdjusterBuilder {
        public final static String FILL_BY_NAME = "FillByName";
        public final static String DEFAULTS = "Defaults";
        public final static String PLAIN = "Plain";
        public final static String OVERRIDE = "Override";
        public final static String SUBSTITUTE = "Substitute";
        public final static String ID_GENERATOR = "IdGenerator";
        public final static String INTERNAL_KEY_LOOKUP = "InternalKeyLookup";

        public static final Modification REMOVE_CHILDREN = Modification.withDescription("removeChildren",
                example -> example.entrySet().removeIf(entry -> entry.getValue() instanceof Collection));
        public static final Modification SUBSTITUTE_CONTEXT_VARS = Modification.withDescription("substituteContextVars",
                example -> {
                    StringSubstitutor stringSubstitutor = DbTurfContext.getCurrentDbTurfContext().getStringSubstitutor();
                    example.replaceAll((key, value) -> (value instanceof String) ? stringSubstitutor.replace((String) value) : value);
                });
        static final NamedModificationAdjuster BASE_ADJUSTER = new NamedModificationAdjuster()
                .add(PLAIN, REMOVE_CHILDREN)
                .add(SUBSTITUTE, SUBSTITUTE_CONTEXT_VARS);

        @Getter
        private NamedModificationAdjuster adjuster;

        public AdjusterBuilder(NamedModificationAdjuster adjuster) {
            this.adjuster = adjuster;
        }

        public AdjusterBuilder() {
            this(BASE_ADJUSTER);
        }

        public AdjusterBuilder idGenerator(Modification idGenerator) {
            adjuster = adjuster.add(ID_GENERATOR, idGenerator);
            return this;
        }

        public AdjusterBuilder internalKeyLookup(Modification internalKeyLookup) {
            adjuster = adjuster.add(INTERNAL_KEY_LOOKUP, internalKeyLookup);
            return this;
        }

        public static AdjusterBuilder withDefaults(JsonItem jsonItem) {
            return new AdjusterBuilder(BASE_ADJUSTER.addBefore(PLAIN, AdjusterBuilder.DEFAULTS, getFillDefaultsModification(jsonItem)));
        }

        static Modification getFillDefaultsModification(JsonItem childJsonItem) {
            Map<String, Object> defaults = childJsonItem.getDefaults();
            if (defaults != null && !defaults.isEmpty()) {
                return Modification.putAllIfAbsent(defaults);
            } else {
                return Modification.SAME;
            }
        }

    }

    public static class ParentDbItem extends DbItem {
        private final List<ChildDbItem> childDbItemMap;

        public ParentDbItem(ItemKeys itemKeys, NamedModificationAdjuster readAdjuster, Adjuster conflictKeyExtractor,
                            List<ChildDbItem> childDbItemMap) {
            super(itemKeys, readAdjuster, conflictKeyExtractor);
            this.childDbItemMap = childDbItemMap;
            log.info("Created parent {}: readAdjuster: {}, conflictKeyExtractor: {}, childDbItemMap: {}",
                    getTableId(), readAdjuster, conflictKeyExtractor, childDbItemMap);
        }

        @Override
        public List<Map<String, Object>> save(List<Map<String, Object>> items, Modification overrides) {
            List<Map<String, Object>> parentKeys = super.save(items, overrides);
            assertThat(parentKeys.size()).as(getTableId().getFullName()).isEqualTo(items.size());

            for (ChildDbItem childDbItem : childDbItemMap) {
                for (int i = 0; i < items.size(); i++) {
                    Map<String, Object> parent = items.get(i);
                    Map<String, Object> parentKey = parentKeys.get(i);
                    //todo as override modification?
                    childDbItem.saveJsonChildren(parent, parentKey);
                }
            }
            return parentKeys;
        }

        public ParentDbItem withReadAdjuster(Consumer<AdjusterBuilder> builder) {
            AdjusterBuilder adjusterBuilder = new AdjusterBuilder(getReadAdjuster());
            builder.accept(adjusterBuilder);
            return new ParentDbItem(getItemKeys(), adjusterBuilder.getAdjuster(), getConflictKeyExtractor(), childDbItemMap);
        }

        public ParentDbItem withConflictKeyExtractor(Adjuster conflictKeyExtractor) {
            return new ParentDbItem(getItemKeys(), getReadAdjuster(), conflictKeyExtractor, childDbItemMap);
        }

        public ParentDbItem addChildDbItem(ChildDbItem childDbItem) {
            ArrayList<ChildDbItem> newList = new ArrayList<>(childDbItemMap);
            newList.add(childDbItem);
            return new ParentDbItem(getItemKeys(), getReadAdjuster(), getConflictKeyExtractor(), newList);
        }

        static Modification getFillByNameModification(JsonItem parentJsonItem) {
            return Modification.withDescription(String.format("FillFromJson(%s)", parentJsonItem), (Map<String, Object> userExample) -> {
                List<Map<String, Object>> example = parentJsonItem.getForItem(userExample);
                assertThat(example.size()).as("Expect exactly 1 json entry for %s", userExample).isEqualTo(1);
                parentJsonItem.getJsonKeyCleaner().modify(userExample);
                example.get(0).forEach(userExample::putIfAbsent);
            });
        }

    }

    public static class ChildDbItem extends DbItem {
        private final Adjuster childFieldsByParentKey;

        public ChildDbItem(ItemKeys itemKeys, NamedModificationAdjuster readAdjuster, Adjuster conflictKeyExtractor,
                           Adjuster childFieldsByParentKey) {
            super(itemKeys, readAdjuster, conflictKeyExtractor);
            this.childFieldsByParentKey = childFieldsByParentKey != null ? childFieldsByParentKey : Adjuster.SAME;
            log.info("Created child {}: readAdjuster: {}, conflictKeyExtractor: {}, childFieldsByParentKey: {}",
                    getTableId(), readAdjuster, conflictKeyExtractor, childFieldsByParentKey);
        }

        public ChildDbItem withReadAdjuster(Consumer<AdjusterBuilder> builder) {
            AdjusterBuilder adjusterBuilder = new AdjusterBuilder(getReadAdjuster());
            builder.accept(adjusterBuilder);
            return new ChildDbItem(getItemKeys(), adjusterBuilder.getAdjuster(), getConflictKeyExtractor(), childFieldsByParentKey);
        }

        public ChildDbItem withConflictKeyExtractor(Adjuster conflictKeyExtractor) {
            return new ChildDbItem(getItemKeys(), getReadAdjuster(), conflictKeyExtractor, childFieldsByParentKey);
        }

        public ChildDbItem withChildFieldsByParentKey(Adjuster childFieldsByParentKey) {
            return new ChildDbItem(getItemKeys(), getReadAdjuster(), getConflictKeyExtractor(), childFieldsByParentKey);
        }


        void saveJsonChildren(Map<String, Object> parent, Map<String, Object> parentKey) {
            List<Map<String, Object>> children = getItemKeys().getJsonItem().getForItem(parent);
            if (children != null && !children.isEmpty()) {
                Map<String, Object> childFields = childFieldsByParentKey.adjust(parentKey);
                save(children, Modification.putAll(childFields));
            }
        }

    }
}
