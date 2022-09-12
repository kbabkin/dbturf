package com.bt.dbturf.sample.domain;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import com.bt.dbturf.core.adjust.Modification;
import com.bt.dbturf.core.adjust.ModificationAdjuster;
import com.bt.dbturf.core.db.DbUpdater;
import com.bt.dbturf.core.db.IdGenerator;
import com.bt.dbturf.core.db.TableId;
import com.bt.dbturf.core.domain.ItemKeys;
import com.bt.dbturf.core.domain.NaturalKey;
import com.bt.dbturf.core.item.DbItem;
import com.bt.dbturf.core.item.JsonItem;
import com.bt.dbturf.core.util.Java8Fit;

import java.util.Collections;

@UtilityClass
public class OrderDomain {
    final ItemKeys ORDER_KEYS = Java8Fit.create(JsonItem.reader("order").parent("template"), jsonItem -> {
        TableId tableId = new TableId("test", "Orders", Collections.singleton("or_id"));
        DbUpdater dbUpdater = new DbUpdater(tableId);
        NaturalKey naturalKey = NaturalKey.of(dbUpdater, Java8Fit.mapOf("customer", "name"), Java8Fit.mapOf("cu_id", "cu_id"));
        return new ItemKeys(dbUpdater, naturalKey, jsonItem);
    });

    @Getter(lazy = true)
    private final DbItem order = Java8Fit.create(() -> {
        Modification idGenerator = new IdGenerator.MaxIdGenerator(ORDER_KEYS.getTableId()).getModification("or_id");
        ModificationAdjuster internalKeyLookup = CustomerDomain.CUSTOMER_KEYS.getNaturalKey().getTechnicalKeyLookupForOthersDefault().toAdjuster()
                .andModification(ProductDomain.PRODUCT_KEYS.getNaturalKey().getTechnicalKeyLookupForOthersDefault().toAdjuster());
        return DbItem.of(ORDER_KEYS)
                .withReadAdjuster(builder -> builder
                        .idGenerator(idGenerator)
                        .internalKeyLookup(internalKeyLookup));
    });
}
