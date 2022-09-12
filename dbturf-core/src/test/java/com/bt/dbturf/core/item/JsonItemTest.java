package com.bt.dbturf.core.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class JsonItemTest {

    @Test
    void reader() {
        Map<String, Object> pig = JsonItem.reader("pig").getRoot("Peppa");
        Assertions.assertEquals(234, pig.get("pig_id"));
    }

}