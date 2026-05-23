package com.meow.exchanger.check;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.util.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class HikariCheck {

    private static final Logger log = LoggerFactory.getLogger(HikariCheck.class);

    public static void main(String[] args) {
        String databasePath = Path.of(
                "src",
                "main",
                "resources",
                "database",
                "currency_exchange.db"
        ).toAbsolutePath().toString();

        try {
            ConnectionFactory.init(databasePath);

            checkSimpleConnection();
            checkCurrencyDao();

            log.info("HikariCP works correctly");

        } catch (Exception e) {
            log.error("HikariCP check failed", e);
        } finally {
            ConnectionFactory.shutdown();
        }
    }

    private static void checkSimpleConnection() throws Exception {
        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {

            if (resultSet.next()) {
                log.info("Connection test result: {}", resultSet.getInt(1));
            }
        }
    }

    private static void checkCurrencyDao() throws Exception {
        CurrencyDao currencyDao = new CurrencyDao();

        List<Currency> currencies = currencyDao.findAll();

        log.info("Currencies count: {}", currencies.size());

        for (Currency currency : currencies) {
            log.info("{} - {}", currency.getCode(), currency.getName());
        }
    }
}