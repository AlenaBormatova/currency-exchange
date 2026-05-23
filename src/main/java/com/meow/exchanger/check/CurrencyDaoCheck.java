package com.meow.exchanger.check;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.model.Currency;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CurrencyDaoCheck {

    public static void main(String[] args) {
        CurrencyDao currencyDao = new CurrencyDao();

        try {
            checkFindAll(currencyDao);
            checkFindByCode(currencyDao);
            checkInsert(currencyDao);
        } catch (SQLException e) {
            System.err.println("Database error:");
            e.printStackTrace();
        }
    }

    private static void checkFindAll(CurrencyDao currencyDao) throws SQLException {
        System.out.println("=== findAll() ===");

        List<Currency> currencies = currencyDao.findAll();

        if (currencies.isEmpty()) {
            System.out.println("No currencies found");
            return;
        }

        for (Currency currency : currencies) {
            printCurrency(currency);
        }
    }

    private static void checkFindByCode(CurrencyDao currencyDao) throws SQLException {
        System.out.println("\n=== findByCode(\"USD\") ===");

        Optional<Currency> currency = currencyDao.findByCode("USD");

        if (currency.isPresent()) {
            printCurrency(currency.get());
        } else {
            System.out.println("Currency USD not found");
        }
    }

    private static void checkInsert(CurrencyDao currencyDao) throws SQLException {
        System.out.println("\n=== insert(\"GBP\") ===");

        Optional<Currency> existingCurrency = currencyDao.findByCode("GBP");

        if (existingCurrency.isPresent()) {
            System.out.println("GBP already exists. Insert skipped.");
            printCurrency(existingCurrency.get());
            return;
        }

        Currency currency = new Currency(
                null,
                "GBP",
                "British Pound",
                "£"
        );

        Currency created = currencyDao.insert(currency);

        System.out.println("Created: ");
        printCurrency(created);
    }

    private static void printCurrency(Currency currency) {
        System.out.printf(
                "id = %d, code = %s, name = %s, sign = %s%n",
                currency.getId(),
                currency.getCode(),
                currency.getName(),
                currency.getSign()
        );
    }
}
