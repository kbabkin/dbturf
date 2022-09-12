package com.bt.dbturf.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class DbTurfContext {
    @Getter
    private final Map<String, Object> properties = new HashMap<>();
    @Getter(lazy = true)
    private final StringSubstitutor stringSubstitutor = new StringSubstitutor(properties);

    //todo thread local?
    private static DbTurfContext currentDbTurfContext;

    public static DbTurfContext getCurrentDbTurfContext() {
        return currentDbTurfContext;
    }

    public static void resetCurrentContext() {
        DbTurfContext context = new DbTurfContext();
        context.getProperties().put("Timestamp", new java.sql.Timestamp(System.currentTimeMillis()));
        currentDbTurfContext = context;
    }
}
