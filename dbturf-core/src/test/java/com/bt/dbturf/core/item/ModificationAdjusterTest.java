package com.bt.dbturf.core.item;

import com.bt.dbturf.core.adjust.Modification;
import com.bt.dbturf.core.adjust.ModificationAdjuster;
import com.bt.dbturf.core.util.Java8Fit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class ModificationAdjusterTest {
    @Test
    void testPut() {
        Map<String, Object> example = Java8Fit.mapOf();

        Map<String, Object> put1 = new ModificationAdjuster(Modification.put("A", 1)).adjust(example);
        Assertions.assertEquals(Java8Fit.mapOf("A", 1), put1);

        Map<String, Object> put2 = new ModificationAdjuster(Modification.put("A", 2)).adjust(put1);
        Assertions.assertEquals(Java8Fit.mapOf("A", 2), put2);
    }

    @Test
    void testPutIfAbsent() {
        Map<String, Object> example = Java8Fit.mapOf();

        Map<String, Object> put1 = new ModificationAdjuster(Modification.putIfAbsent("A", 1)).adjust(example);
        Assertions.assertEquals(Java8Fit.mapOf("A", 1), put1);

        Map<String, Object> put2 = new ModificationAdjuster(Modification.putIfAbsent("A", 2)).adjust(put1);
        Assertions.assertEquals(Java8Fit.mapOf("A", 1), put2);
    }

    @Test
    void testRemove() {

    }

    @Test
    void testRemoveIf() {

    }
}
