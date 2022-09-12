package com.bt.dbturf.core.db;

import lombok.Getter;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.util.List;
import java.util.Map;

public class DbOperations {
    @Getter(lazy = true)
    private final static DbOperations instance = new DbOperations();

    public <V> V nativeAsScalar(String sql, Map<String, Object> params, Class<V> clazz) {
        try (Connection connection = new Sql2o(DatabaseConfig.getDataSource()).open()) {
            Query query = connection.createQuery(sql);
            params.forEach(query::addParameter);
            return query.executeScalar(clazz);
        }
    }

    public List<Map<String, Object>> nativeQuery(String sql, Map<String, Object> params) {
        try (Connection connection = new Sql2o(DatabaseConfig.getDataSource()).open()) {
            Query query = connection.createQuery(sql);
            params.forEach(query::addParameter);
            return query.executeAndFetchTable().asList();
        }
    }

}
