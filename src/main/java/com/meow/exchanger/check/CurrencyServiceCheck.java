package com.meow.exchanger.check;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.ConflictException;
import com.meow.exchanger.exception.NotFoundException;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.service.CurrencyService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CurrencyServiceCheck {
    public static void main(String[] args) throws SQLException {
        checkFindAll();
        checkInsertSuccess();
        checkInsertWithEmptyName();
        checkInsertWithInvalidCode();
        checkInsertWithExistingCode();
        checkFindByCodeSuccess();
        checkFindByCodeNotFound();

        System.out.println("All CurrencyService checks passed");
    }

    private static void checkFindAll() throws SQLException {
        FakeCurrencyDao currencyDao = new FakeCurrencyDao();

        currencyDao.saveExisting(new Currency(1, "USD", "United States dollar", "$"));
        currencyDao.saveExisting(new Currency(2, "EUR", "Euro", "€"));
        currencyDao.saveExisting(new Currency(3, "RUB", "Russian Ruble", "₽"));

        CurrencyService currencyService = new CurrencyService(currencyDao);

        List<Currency> currencies = currencyService.findAll();

        assertEquals(3, currencies.size(), "currencies size");

        Currency first = currencies.get(0);
        Currency second = currencies.get(1);
        Currency third = currencies.get(2);

        assertEquals("USD", first.getCode(), "first currency code");
        assertEquals("EUR", second.getCode(), "second currency code");
        assertEquals("RUB", third.getCode(), "third currency code");
    }

    private static void checkInsertSuccess() throws SQLException {
        FakeCurrencyDao currencyDao = new FakeCurrencyDao(); // замена БД для проверки CurrencyService, а не SQLite/JDBC
        CurrencyService currencyService = new CurrencyService(currencyDao);

        Currency created = currencyService.insert(" Euro ", " eur ", " € ");

        assertEquals(1, created.getId(), "id");
        assertEquals("EUR", created.getCode(), "code");
        assertEquals("Euro", created.getName(), "name");
        assertEquals("€", created.getSign(), "sign");
    }

    private static void checkInsertWithEmptyName() {
        FakeCurrencyDao currencyDao = new FakeCurrencyDao();
        CurrencyService currencyService = new CurrencyService(currencyDao);

        assertThrows(
                BadRequestException.class,
                () -> currencyService.insert("   ", "EUR", "€"),
                "Currency name is missing"
        );
    }

    private static void checkInsertWithInvalidCode() {
        FakeCurrencyDao currencyDao = new FakeCurrencyDao();
        CurrencyService currencyService = new CurrencyService(currencyDao);

        assertThrows(
                BadRequestException.class,
                () -> currencyService.insert("Euro", "EURO", "€"),
                "Currency code must consist of 3 Latin letters"
        );
    }

    private static void checkInsertWithExistingCode() {
        FakeCurrencyDao currencyDao = new FakeCurrencyDao();
        currencyDao.saveExisting(new Currency(1, "EUR", "Euro", "€"));

        CurrencyService currencyService = new CurrencyService(currencyDao);

        assertThrows(
                ConflictException.class,
                () -> currencyService.insert("Euro", "EUR", "€"),
                "Currency with this code already exists"
        );
    }

    private static void checkFindByCodeSuccess() throws SQLException {
        FakeCurrencyDao currencyDao = new FakeCurrencyDao();
        currencyDao.saveExisting(new Currency(1, "USD", "United States dollar", "$"));

        CurrencyService currencyService = new CurrencyService(currencyDao);

        Currency currency = currencyService.findByCode(" usd ");

        assertEquals(1, currency.getId(), "id");
        assertEquals("USD", currency.getCode(), "code");
        assertEquals("United States dollar", currency.getName(), "name");
        assertEquals("$", currency.getSign(), "sign");
    }

    private static void checkFindByCodeNotFound() {
        FakeCurrencyDao currencyDao = new FakeCurrencyDao();
        CurrencyService currencyService = new CurrencyService(currencyDao);

        assertThrows(
                NotFoundException.class,
                () -> currencyService.findByCode("EUR"),
                "Currency not found"
        );
    }

    private static void assertEquals(Object expected, Object actual, String fieldName) {
        if (!expected.equals(actual)) {
            throw new AssertionError(
                    fieldName + ": expected <" + expected + ">, but was <" + actual + ">"
            );
        }
    }

    private static void assertThrows(
            Class<? extends RuntimeException> expectedExceptionClass,
            ThrowingAction action,
            String expectedMessage
    ) {
        try {
            action.run();
        } catch (RuntimeException e) {
            if (!expectedExceptionClass.isInstance(e)) {
                throw new AssertionError(
                        "Expected exception " + expectedExceptionClass.getSimpleName()
                                + ", but was " + e.getClass().getSimpleName()
                );
            }

            if (!expectedMessage.equals(e.getMessage())) {
                throw new AssertionError(
                        "Expected message <" + expectedMessage + ">, but was <" + e.getMessage() + ">"
                );
            }

            return;
        } catch (Exception e) {
            throw new AssertionError("Unexpected checked exception: " + e.getClass().getSimpleName());
        }

        throw new AssertionError(
                "Expected exception " + expectedExceptionClass.getSimpleName()
                        + ", but nothing was thrown"
        );
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }

    private static class FakeCurrencyDao extends CurrencyDao {

        private final Map<String, Currency> currencies = new LinkedHashMap<>();
        private int nextId = 1;

        @Override
        public List<Currency> findAll() {
            return new ArrayList<>(currencies.values());
        }

        @Override
        public Optional<Currency> findByCode(String code) {
            return Optional.ofNullable(currencies.get(code));
        }

        @Override
        public Currency insert(Currency currency) {
            Currency created = new Currency(
                    nextId++,
                    currency.getCode(),
                    currency.getName(),
                    currency.getSign()
            );

            currencies.put(created.getCode(), created);

            return created;
        }

        private void saveExisting(Currency currency) {
            currencies.put(currency.getCode(), currency);
        }
    }
}