package com.bt.dbturf.sample.domain;

import com.bt.dbturf.sample.db.TestDatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.bt.dbturf.core.DbTurfContext;
import com.bt.dbturf.core.item.DbItem;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static com.bt.dbturf.core.util.Java8Fit.listOf;
import static com.bt.dbturf.core.util.Java8Fit.mapOf;

public class OrderDomainTest {
    @BeforeEach
    void setup() {
        DbTurfContext.resetCurrentContext();
    }

    @Test
    void test() {
        TestDatabaseConfig.initSchema();

        DbItem customerDbItem = CustomerDomain.getCustomer();
        customerDbItem.save(listOf(mapOf("name", "TripleOne")));

        DbItem productDbItem = ProductDomain.getProduct();
        productDbItem.save(listOf(mapOf("name", "IBM")));

        DbItem orderDbItem = OrderDomain.getOrder();
        List<Map<String, Object>> saved = orderDbItem.save(listOf(mapOf("template", "Delivery",
                "customer", "TripleOne", "product", "IBM", "amount", 123)));

        assertThat(saved.size()).isNotEqualTo(0);
        assertThat(saved.get(0).get("or_id")).isNotNull();
    }

}
