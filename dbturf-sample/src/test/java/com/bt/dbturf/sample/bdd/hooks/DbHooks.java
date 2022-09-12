package com.bt.dbturf.sample.bdd.hooks;

import com.bt.dbturf.sample.db.TestDatabaseConfig;
import io.cucumber.java.BeforeAll;

public class DbHooks {
    @BeforeAll
    public static void initSchema() {
        TestDatabaseConfig.initSchema();
    }
}
