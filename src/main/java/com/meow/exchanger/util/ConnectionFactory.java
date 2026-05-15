package com.meow.exchanger.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionFactory {

    private static HikariDataSource dataSource;

    private ConnectionFactory() {
    }

    public static synchronized void init(String databasePath) {
        if (dataSource != null) {
            return;
        }

        System.out.println("Database path: " + databasePath);

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:sqlite:" + databasePath);
        config.setDriverClassName("org.sqlite.JDBC");

        config.setPoolName("CurrencyExchangeHikariPool");

        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);

        config.setConnectionTimeout(10_000);
        config.setMaxLifetime(300_000);

        config.setConnectionInitSql("PRAGMA foreign_keys = ON");

        dataSource = new HikariDataSource(config);

        System.out.println("HikariCP pool initialized");
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
        }
    }
}