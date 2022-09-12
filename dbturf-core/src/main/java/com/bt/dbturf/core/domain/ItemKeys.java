package com.bt.dbturf.core.domain;

import com.bt.dbturf.core.db.DbUpdater;
import com.bt.dbturf.core.db.TableId;
import com.bt.dbturf.core.item.JsonItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ItemKeys {
    //todo defaults?
    //todo clean generated to compare
    private final DbUpdater dbUpdater;
    private final NaturalKey naturalKey;
    private final JsonItem jsonItem;


    public TableId getTableId() {
        return dbUpdater.getTableId();
    }
}
