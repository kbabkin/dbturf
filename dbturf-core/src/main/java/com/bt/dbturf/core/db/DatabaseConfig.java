package com.bt.dbturf.core.db;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

public class DatabaseConfig {

    @Getter(lazy = true)
    private final static HikariDataSource dataSource = createDataSource();

    static HikariDataSource createDataSource() {
        Config conf = ConfigFactory.load();
        HikariConfig jdbcConfig = getHikariConfig(conf.getConfig("pool"));
        return new HikariDataSource(jdbcConfig);
    }

    static HikariConfig getHikariConfig(Config conf) {
        // return ConfigBeanFactory.create(conf, HikariConfig.class);
        HikariConfig jdbcConfig = new HikariConfig();
        jdbcConfig.setPoolName(conf.getString("poolName"));
        jdbcConfig.setDriverClassName(conf.getString("driverClassName"));
        jdbcConfig.setJdbcUrl(conf.getString("jdbcUrl"));
        jdbcConfig.setUsername(conf.getString("username"));
        jdbcConfig.setPassword(conf.getString("password"));
        return jdbcConfig;
    }
}
