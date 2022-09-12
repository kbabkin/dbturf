package com.bt.dbturf.core.db;

import lombok.Getter;
import com.bt.dbturf.core.adjust.Adjuster;
import com.bt.dbturf.core.adjust.RetrieveAdjuster;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Getter
public class TableId {
    private final String schema;
    private final String table;
    private final String fullName;
    private final Set<String> keys;
    private final Adjuster keysRetriever;

    public TableId(String schema, String table, Set<String> keys) {
        this.schema = schema;
        this.table = table;
        this.fullName = schema == null ? table : schema + "." + table;
        this.keys = keys;
        this.keysRetriever = new RetrieveAdjuster(keys, true);
    }

    public TableId(String schema, String table) {
        this(schema, table, Collections.emptySet());
    }

    public Map<String, Object> getKeyValues(Map<String, Object> example) {
        return keysRetriever.adjust(example);
    }

    public Map<String, Object> getKeyValuesIfExist(Map<String, Object> example) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        return "TableId[fullName=" + fullName + ", keys=" + keys + "]";
    }

}
