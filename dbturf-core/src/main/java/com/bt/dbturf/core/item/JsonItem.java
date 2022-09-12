package com.bt.dbturf.core.item;

import com.bt.dbturf.core.DbTurfException;
import com.bt.dbturf.core.util.Java8Fit;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import com.bt.dbturf.core.adjust.Modification;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class JsonItem {
    public static final String DEFAULTS_NAME = "#defaults";
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public abstract Map<String, Object> getDefaults();

    public abstract List<Map<String, Object>> getForItem(Map<String, Object> userExample);

    public Modification getJsonKeyCleaner() {
        return Modification.SAME;
    }

    public ChildJsonItem child(String childCollection) {
        return new ChildJsonItem(this, childCollection);
    }

    public static class Reader {
        private final URL resource;
        @Getter(lazy = true)
        private final Map<String, Object> data = loadData();

        private Map<String, Object> loadData() {
            try (InputStream inputStream = resource.openStream()) {
                return (Map) OBJECT_MAPPER.readValue(inputStream, Map.class);
            } catch (IOException e) {
                throw new DbTurfException("loadData " + resource, e);
            }
        }

        public Reader(String file) {
            String extName = "/dbturf/" + file + ".json";
            this.resource = getClass().getResource(extName);
            Assertions.assertNotNull(resource, extName);
        }

        public Map<String, Object> getRoot(String parentName) {
            return (Map) getData().get(parentName);
        }

        public ParentJsonItem parent(String parentFieldName) {
            return new ParentJsonItem(this, parentFieldName);
        }
    }

    public static Reader reader(String file) {
        return new Reader(file);
    }

    @Getter
    @RequiredArgsConstructor
    public static class ParentJsonItem extends JsonItem {
        private final Reader reader;
        private final String parentFieldName;

        @Override
        public Map<String, Object> getDefaults() {
            return reader.getRoot(DEFAULTS_NAME);
        }

        @Override
        public List<Map<String, Object>> getForItem(Map<String, Object> userExample) {
            String name = (String) userExample.get(parentFieldName);
            assertThat(name).as("No '%s' in %s", parentFieldName, userExample).isNotEmpty();
            Map<String, Object> example = reader.getRoot(name);
            assertThat(example).as("No sample for %s=%s", parentFieldName, name).isNotEmpty();
            return Java8Fit.listOf(example);
        }

        @Override
        public Modification getJsonKeyCleaner() {
            return Modification.remove(parentFieldName);
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class ChildJsonItem extends JsonItem {
        private final JsonItem parentJsonItem;
        private final String childCollection;

        @Override
        public Map<String, Object> getDefaults() {
            Map<String, Object> parentDefaults = parentJsonItem.getDefaults();
            return parentDefaults != null ? (Map) parentDefaults.get(childCollection) : null;
        }

        @Override
        public List<Map<String, Object>> getForItem(Map<String, Object> parent) {
            //todo flatmap
            List<Map<String, Object>> parentJson = parentJsonItem.getForItem(parent);
            assertThat(parentJson.size()).as("Expect exactly 1 json entry for %s", parent).isEqualTo(1);
            return (List) parentJson.get(0).get(childCollection);
        }
    }
}