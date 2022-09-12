package com.bt.dbturf.sample.domain;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import com.bt.dbturf.core.db.DbUpdater;
import com.bt.dbturf.core.db.TableId;
import com.bt.dbturf.core.domain.ItemKeys;
import com.bt.dbturf.core.domain.NaturalKey;
import com.bt.dbturf.core.item.DbItem;
import com.bt.dbturf.core.item.JsonItem;
import com.bt.dbturf.core.util.Java8Fit;

import java.util.Collections;

@UtilityClass
public class CustomerDomain {

    final ItemKeys CUSTOMER_KEYS = Java8Fit.create(JsonItem.reader("customer").parent("name"), jsonItem -> {
        TableId tableId = new TableId("test", "Customer", Collections.singleton("cu_id"));
        DbUpdater dbUpdater = new DbUpdater(tableId);
        NaturalKey naturalKey = NaturalKey.of(dbUpdater, Java8Fit.mapOf("customer", "name"), Java8Fit.mapOf("cu_id", "cu_id"));
        return new ItemKeys(dbUpdater, naturalKey, jsonItem);
    });

    @Getter(lazy = true)
    private final DbItem customer = DbItem.of(CUSTOMER_KEYS);
}
