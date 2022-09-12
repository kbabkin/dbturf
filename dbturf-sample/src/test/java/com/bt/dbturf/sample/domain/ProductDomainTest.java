package com.bt.dbturf.sample.domain;

import com.bt.dbturf.sample.db.TestDatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.bt.dbturf.core.DbTurfContext;
import com.bt.dbturf.core.item.DbItem;
import com.bt.dbturf.core.item.DiffItem;

import java.util.List;
import java.util.Map;

import static com.bt.dbturf.core.util.Java8Fit.listOf;
import static com.bt.dbturf.core.util.Java8Fit.mapOf;

public class ProductDomainTest {

    @BeforeEach
    void setup() {
        TestDatabaseConfig.initSchema();
        DbTurfContext.resetCurrentContext();
    }

    @Test
    void test() {
        DbItem.ParentDbItem dbItem = ProductDomain.getProduct();
        DbItem.ChildDbItem xrefDbItem = ProductDomain.getProductXref();
        List<Map<String, Object>> savedIds = dbItem.save(listOf(mapOf("name", "IBM", "description", "ut desc")));
        DiffItem.diff(listOf(mapOf("pr_id", 1234)), savedIds).assertEquals("Saved parent id");

        List<Map<String, Object>> products = dbItem.getDbUpdater().findByEq(savedIds.get(0));
        DiffItem.diff(listOf(mapOf("name", "IBM", "description", "ut desc", "pr_id", 1234)),
                products).assertEquals("Saved parent");

        List<Map<String, Object>> xrefs = xrefDbItem.getDbUpdater().findByEq(savedIds.get(0));
        DiffItem.diff(listOf(mapOf("xref_type", "RIC", "xref_value", "IBM", "pr_id", 1234)),
                xrefs).assertEquals("Saved child");
    }

}
