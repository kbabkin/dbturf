package com.bt.dbturf.sample.bdd.steps;

import com.bt.dbturf.sample.domain.CustomerDomain;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import com.bt.dbturf.core.item.DbItem;
import com.bt.dbturf.core.item.DiffItem;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomerSteps implements En {
    public CustomerSteps() {
        Given("customer", (DataTable dataTable) -> {
            List<Map<String, Object>> params = dataTable.asMaps(String.class, Object.class);
            DbItem dbItem = CustomerDomain.getCustomer();
            dbItem.save(params);
        });

        Given("customer is", (DataTable dataTable) -> {
            //todo convert types ?
            List<Map<String, Object>> params = dataTable.asMaps(String.class, Object.class);
            DbItem dbItem = CustomerDomain.getCustomer();
            List<Map<String, Object>> actual = params.stream()
                    .map(dbItem.getNaturalKey().getTechnicalKeyLookupForSelf().toAdjuster()::adjust)
                    .map(dbItem.getDbUpdater()::findByEq)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            DiffItem.diff(params, actual).assertEquals("Customer in database is not as expected");
        });
    }
}
