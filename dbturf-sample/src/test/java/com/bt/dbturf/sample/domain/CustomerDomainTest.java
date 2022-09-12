package com.bt.dbturf.sample.domain;

import com.bt.dbturf.sample.db.TestDatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.bt.dbturf.core.DbTurfContext;
import com.bt.dbturf.core.db.DbUpdater;
import com.bt.dbturf.core.db.TableId;
import com.bt.dbturf.core.item.DbItem;
import com.bt.dbturf.core.item.DiffItem;
import com.bt.dbturf.core.item.JsonItem;

import java.util.List;
import java.util.Map;

import static com.bt.dbturf.core.util.Java8Fit.listOf;
import static com.bt.dbturf.core.util.Java8Fit.mapOf;
import static com.bt.dbturf.core.util.Java8Fit.setOf;

public class CustomerDomainTest {

    @BeforeEach
    void setup() {
        DbTurfContext.resetCurrentContext();
    }

    @Test
    void test() {
        TestDatabaseConfig.initSchema();

        DbItem dbItem = CustomerDomain.getCustomer();
        dbItem.save(listOf(mapOf("name", "TripleOne")));

        dbItem.save(listOf(mapOf("name", "TripleOne", "updated_by", "Changed")));
        //todo check
    }

    static void readmeExample() {
        // Read template from JSON
        Map<String, Object> item = JsonItem.reader("customer").getRoot("TripleOne");
        // Customize for scenario
        item.put("country", "ECO");
        // Merge (insert/update as required) in database
        DbUpdater dbUpdater = new DbUpdater(new TableId("test", "Customer", setOf("cu_id")));
        dbUpdater.merge(item);
        // Load from database and compare
        List<Map<String, Object>> loaded = dbUpdater.findByEq(mapOf("cu_id", 111));
        DiffItem.diff(listOf(mapOf("name", "TripleOne", "country", "ECO")), loaded)
                .assertEquals("loaded as saved");
    }
}
