package com.bt.dbturf.sample.bdd.steps;

import com.bt.dbturf.core.item.DiffItem;
import com.bt.dbturf.sample.domain.OrderDomain;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import com.bt.dbturf.core.item.DbItem;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderSteps implements En {
    public OrderSteps() {
        Given("order", (DataTable dataTable) -> {
            List<Map<String, Object>> params = dataTable.asMaps(String.class, Object.class);
            DbItem dbItem = OrderDomain.getOrder();
            dbItem.save(params);
        });

        Given("order is", (DataTable dataTable) -> {
            //todo convert types ?
            List<Map<String, Object>> params = dataTable.asMaps(String.class, Object.class);
            DbItem dbItem = OrderDomain.getOrder();
            List<Map<String, Object>> actual = params.stream()
                    .map(dbItem.getNaturalKey().getTechnicalKeyLookupForSelf().toAdjuster()::adjust)
                    .map(dbItem.getDbUpdater()::findByEq)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            DiffItem.diff(params, actual).assertEquals("Order in database is not as expected");
        });
    }
}
