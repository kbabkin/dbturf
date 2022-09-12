package com.bt.dbturf.sample.db;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.RunScript;
import com.bt.dbturf.core.DbTurfException;
import com.bt.dbturf.core.db.DatabaseConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class TestDatabaseConfig {
    private static final String DB_SCHEMA_SQL = "db/schema.sql";
    private static boolean inited;

    public static void initSchema() {
        if (!inited) {
            try (Connection connection = DatabaseConfig.getDataSource().getConnection()) {
                // todo / in resource name?
                try (InputStream resourceAsStream = TestDatabaseConfig.class.getClassLoader()
                        .getResourceAsStream(DB_SCHEMA_SQL)) {
                    log.info("Init schema: {}", DB_SCHEMA_SQL);
                    RunScript.execute(connection, new InputStreamReader(resourceAsStream));
                }
            } catch (SQLException | IOException e) {
                throw new DbTurfException(e);
            }
        }
        inited = true;
    }

}
