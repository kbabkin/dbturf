package com.bt.dbturf.core.item;

import com.bt.dbturf.core.db.DbUpdater;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TechnicalKeyLookupModificationTest {
    // @Test
    // void testCachedFindInternalKey() {

    // }

    // @Test
    // void testFindInternalKey() {

    // }

    // @Test
    // void testModify() {

    // }

    // @Test
    // void testOf() {

    // }

    // @Test
    // void testOf2() {

    // }

    @Test
    void testOf3() {
        DbUpdater updater = mock(DbUpdater.class);
        when(updater.findByEq(Collections.singletonMap("RIC", "IBM"))).thenReturn(Collections.singletonList(Collections.singletonMap("SeId", 1234)));
        //fixme set dboperations instance
//        Modification lookup = InternalKeyLookupModification.of(updater, "RIC", "SeId"));
//        Map<String, Object> processed = new ModificationAdjuster().andModification(lookup).process(Java8Fit.mapOf("RIC", "IBM", "Field1", "TBD"));
//        assertThat(processed).isEqualTo(Java8Fit.mapOf("RIC", "IBM", "Field1", "TBD", "SeId", 1234));
    }
}
