package com.bt.dbturf.sample.bdd.steps;

import com.bt.dbturf.sample.domain.ProductDomain;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import com.bt.dbturf.core.item.DbItem;
import com.bt.dbturf.core.item.DiffItem;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductSteps implements En {
    //todo convert types from cucumber format

    public ProductSteps() {
        Given("product", (DataTable dataTable) -> {
            List<Map<String, Object>> params = dataTable.asMaps(String.class, Object.class);
            ProductDomain.getProduct().save(params);
        });

        Then("product is", (DataTable dataTable) -> {
            List<Map<String, Object>> params = dataTable.asMaps(String.class, Object.class);
            DbItem dbItem = ProductDomain.getProduct();
            params = dbItem.getNaturalKey().getTechnicalKeyLookupForSelf().toAdjuster().adjustAll(params);
            List<Map<String, Object>> actual = params.stream()
                    .map(dbItem.getTableId()::getKeyValues)
                    .map(dbItem.getDbUpdater()::findByEq)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            DiffItem.diff(params, actual).assertEquals("Product in database is not as expected");
        });

        Given("product xrefs", (DataTable dataTable) -> {
            List<Map<String, Object>> params = dataTable.asMaps(String.class, Object.class);
            ProductDomain.getProductXref().save(params);
        });

        Then("product xrefs are", (DataTable dataTable) -> {
            // todo natural key retriever as param to dbitem
            // todo are/contains
            List<Map<String, Object>> params = dataTable.asMaps(String.class, Object.class);
            DbItem dbItem = ProductDomain.getProductXref();
            DbItem.ParentDbItem productDbItem = ProductDomain.getProduct();
            params = productDbItem.getNaturalKey().getTechnicalKeyLookupForOthersDefault().toAdjuster().adjustAll(params);
            List<Map<String, Object>> actual = params.stream()
                    .map(productDbItem.getTableId()::getKeyValues) // are, not contains
                    .distinct()
                    .map(dbItem.getDbUpdater()::findByEq)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            DiffItem.diff(params, actual).assertEquals("Product xrefs in database are not as expected");
        });
    }
}
