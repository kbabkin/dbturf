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

@UtilityClass
public class ProductDomain {
    final ItemKeys PRODUCT_KEYS = Java8Fit.create(JsonItem.reader("product").parent("name"), jsonItem -> {
        TableId tableId = new TableId("test", "Product", Java8Fit.setOf("pr_id"));
        DbUpdater dbUpdater = new DbUpdater(tableId);
        NaturalKey naturalKey = NaturalKey.of(dbUpdater, Java8Fit.mapOf("product", "name"), Java8Fit.mapOf("pr_id", "pr_id"));
        return new ItemKeys(dbUpdater, naturalKey, jsonItem);
    });

    final ItemKeys PRODUCT_XREF_KEYS = Java8Fit.create(PRODUCT_KEYS.getJsonItem().child("xrefs"), jsonItem -> {
        TableId tableId = new TableId("test", "Product_Xref", Java8Fit.setOf("pr_id", "xref_type"));//, "xref_value"));
        DbUpdater dbUpdater = new DbUpdater(tableId);
        NaturalKey naturalKey = NaturalKey.of(dbUpdater, Java8Fit.mapOf("productXref", "name"), Java8Fit.mapOf("pr_id", "pr_id"));
        return new ItemKeys(dbUpdater, naturalKey, jsonItem);
    });

    @Getter(lazy = true)
    private final DbItem.ChildDbItem productXref = DbItem.childStandalone(PRODUCT_XREF_KEYS, PRODUCT_KEYS);

    @Getter(lazy = true)
    private final DbItem.ParentDbItem product = DbItem.parent(PRODUCT_KEYS)
            .addChildDbItem(DbItem.childCollection(PRODUCT_XREF_KEYS, PRODUCT_KEYS));
}
