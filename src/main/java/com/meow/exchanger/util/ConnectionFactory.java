package com.meow.exchanger.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionFactory {

    private static final Logger log = LoggerFactory.getLogger(ConnectionFactory.class);

    private static HikariDataSource dataSource;

    private ConnectionFactory() {
    }

    public static synchronized void init() {
        if (dataSource != null) {
            return;
        }

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:sqlite::resource:database/currency_exchange.db");
        config.setDriverClassName("org.sqlite.JDBC");

        config.setPoolName("CurrencyExchangeHikariPool");

        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);

        config.setConnectionTimeout(10_000);
        config.setMaxLifetime(300_000);

        config.setConnectionInitSql("PRAGMA foreign_keys = ON");

        dataSource = new HikariDataSource(config);

        log.info("HikariCP pool initialized");
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("Connection pool is not initialized");
        }

        return dataSource.getConnection();
    }

    public static synchronized void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;

            log.info("HikariCP pool shut down");
        }
    }
}