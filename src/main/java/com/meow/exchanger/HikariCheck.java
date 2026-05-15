package com.meow.exchanger;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.util.ConnectionFactory;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class HikariCheck {

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

            System.out.println("HikariCP works correctly");

        } catch (Exception e) {
            System.err.println("HikariCP check failed");
            e.printStackTrace();
        } finally {
            ConnectionFactory.shutdown();
        }
    }

    private static void checkSimpleConnection() throws Exception {
        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {

            if (resultSet.next()) {
                System.out.println("Connection test result: " + resultSet.getInt(1));
            }
        }
    }

    private static void checkCurrencyDao() throws Exception {
        CurrencyDao currencyDao = new CurrencyDao();

        List<Currency> currencies = currencyDao.findAll();

        System.out.println("Currencies count: " + currencies.size());

        for (Currency currency : currencies) {
            System.out.println(currency.getCode() + " - " + currency.getName());
        }
    }
}