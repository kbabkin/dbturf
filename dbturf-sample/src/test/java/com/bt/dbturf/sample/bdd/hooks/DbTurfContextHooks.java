package com.bt.dbturf.sample.bdd.hooks;

import com.bt.dbturf.core.DbTurfContext;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class DbTurfContextHooks {
    @Before
    public void setupContext(Scenario scenario) {
        DbTurfContext.resetCurrentContext();
    }
}

