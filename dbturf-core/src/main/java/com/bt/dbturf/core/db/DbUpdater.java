package com.bt.dbturf.core.db;

import com.bt.dbturf.core.DbTurfException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import com.bt.dbturf.core.util.Java8Fit;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
@Slf4j
public class DbUpdater {
    @Getter
    private final TableId tableId;
    private final Handler mergeHandler = new MergeHandler(this);


    private static String whereParams(Collection<String> names) {
        return names.stream()
                .map(name -> name + "=:" + name)
                .collect(Collectors.joining(" and "));
    }

    public List<Map<String, Object>> findByEq(Map<String, Object> example) {
        assertThat(example.size()).isNotEqualTo(0);
        String sql = "select * from " + tableId.getFullName() +
                " where " + whereParams(example.keySet());
        try (Connection connection = new Sql2o(DatabaseConfig.getDataSource()).open()) {
            Query query = connection.createQuery(sql);
            example.forEach(query::addParameter);
            return query.executeAndFetchTable().asList();
        }
    }

    public int insert(Map<String, Object> example) {
        assertThat(example.size()).isNotEqualTo(0);
        log.info("insert into {}: {}", getTableId().getFullName(), example);
        String sql = "insert into " + tableId.getFullName()
                + " (" + example.keySet().stream()
                .collect(Collectors.joining(", "))
                + ") values (" + example.keySet().stream()
                .map(name -> ":" + name)
                .collect(Collectors.joining(", "))
                + ")";
        try (Connection connection = new Sql2o(DatabaseConfig.getDataSource()).open()) {
            Query query = connection.createQuery(sql);
            example.forEach(query::addParameter);
            query.executeUpdate();
            return 1;
        }
    }

    public int update(Map<String, Object> example) {
        assertThat(example.size()).isNotEqualTo(0);
        log.info("update {}: {}", getTableId().getFullName(), example);
        Set<String> keys = tableId.getKeys();
        String sql = "update " + tableId.getFullName()
                + " set " + example.keySet().stream()
                .filter(Java8Fit.not(keys::contains))
                .map(name -> name + "  = :" + name)
                .collect(Collectors.joining(", "))
                + " where " + whereParams(keys);
        try (Connection connection = new Sql2o(DatabaseConfig.getDataSource()).open()) {
            Query query = connection.createQuery(sql);
            example.forEach(query::addParameter);
            query.executeUpdate();
            return 1;
        }
    }

    public int remove(Map<String, Object> example) {
        assertThat(example.size()).isNotEqualTo(0);
        log.info("remove {}: {}", getTableId().getFullName(), example);
        Map<String, Object> keyValues = tableId.getKeyValues(example);
        String sql = "delete from " + tableId.getFullName()
                + " where " + whereParams(keyValues.keySet());
        try (Connection connection = new Sql2o(DatabaseConfig.getDataSource()).open()) {
            Query query = connection.createQuery(sql);
            keyValues.forEach(query::addParameter);
            query.executeUpdate();
            return 1;
        }
    }

    public int merge(Map<String, Object> example) {
        return mergeHandler.handle(example);
    }

    public static abstract class Handler {

        protected abstract List<Map<String, Object>> findExisting(Map<String, Object> example);

        protected Map<String, Object> handleMultipleExisting(List<Map<String, Object>> existingList,
                                                             Map<String, Object> example) {
            Assertions.fail("Multiple existing for " + example + ": " + existingList);
            throw new DbTurfException();
        }

        protected abstract int handleNew(Map<String, Object> example);

        protected abstract boolean isChanged(Map<String, Object> existing, Map<String, Object> example);

        protected abstract int handleChanged(Map<String, Object> existing, Map<String, Object> example);

        protected int handleUnchanged(Map<String, Object> existing, Map<String, Object> example) {
            // do nothing by default
            return 0;
        }

        public int handle(Map<String, Object> example) {
            List<Map<String, Object>> existingList = findExisting(example);
            if (existingList.isEmpty()) {
                return handleNew(example);
            }
            Map<String, Object> existing = (existingList.size() == 1)
                    ? existingList.get(0)
                    : handleMultipleExisting(existingList, example);

            return isChanged(existing, example)
                    ? handleChanged(existing, example)
                    : handleUnchanged(existing, example);
        }

    }

    @RequiredArgsConstructor
    protected static class MergeHandler extends Handler {
        private final DbUpdater dbUpdater;

        @Override
        protected List<Map<String, Object>> findExisting(Map<String, Object> example) {
            Map<String, Object> keyValue = dbUpdater.getTableId().getKeyValues(example);
            return dbUpdater.findByEq(keyValue);
        }

        @Override
        protected int handleNew(Map<String, Object> example) {
            return dbUpdater.insert(example);
        }

        @Override
        protected boolean isChanged(Map<String, Object> existing, Map<String, Object> example) {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        protected int handleChanged(Map<String, Object> existing, Map<String, Object> example) {
            return dbUpdater.update(example);
        }

    }

}
