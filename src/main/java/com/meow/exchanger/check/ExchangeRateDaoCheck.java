package com.meow.exchanger.check;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dao.ExchangeRateDao;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.ExchangeRate;
import com.meow.exchanger.util.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDaoCheck {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateDaoCheck.class);

    public static void main(String[] args) {

        try {
            ConnectionFactory.init();

            ExchangeRateDao exchangeRateDao = new ExchangeRateDao();
            CurrencyDao currencyDao = new CurrencyDao();

            checkFindAll(exchangeRateDao);
            checkFindByPair(exchangeRateDao);
            checkInsert(exchangeRateDao, currencyDao);
            checkUpdateRate(exchangeRateDao);

            log.info("All ExchangeRateDao checks completed");

        } catch (SQLException e) {
            log.error("Database error", e);
        } finally {
            ConnectionFactory.shutdown();
        }
    }

    private static void checkFindAll(ExchangeRateDao exchangeRateDao) throws SQLException {
        log.info("=== findAll() ===");

        List<ExchangeRate> exchangeRates = exchangeRateDao.findAll();

        if (exchangeRates.isEmpty()) {
            log.info("No exchange rates found");
            return;
        }

        for (ExchangeRate exchangeRate : exchangeRates) {
            printExchangeRate(exchangeRate);
        }
    }

    private static void checkFindByPair(ExchangeRateDao exchangeRateDao) throws SQLException {
        log.info("=== findByPair(\"USD\", \"RUB\") ===");

        Optional<ExchangeRate> exchangeRate = exchangeRateDao.findByPair("USD", "RUB");

        if (exchangeRate.isPresent()) {
            printExchangeRate(exchangeRate.get());
        } else {
            log.info("Exchange rate USD/RUB not found");
        }
    }

    private static void checkInsert(ExchangeRateDao exchangeRateDao, CurrencyDao currencyDao) throws SQLException {
        log.info("=== insert(\"EUR\", \"RUB\") ===");

        Optional<ExchangeRate> existingExchangeRate = exchangeRateDao.findByPair("EUR", "RUB");

        if (existingExchangeRate.isPresent()) {
            log.info("EUR/RUB already exists. Insert skipped.");
            printExchangeRate(existingExchangeRate.get());
            return;
        }

        Currency baseCurrency = currencyDao.findByCode("EUR")
                .orElseThrow(() -> new SQLException("Currency EUR not found"));

        Currency targetCurrency = currencyDao.findByCode("RUB")
                .orElseThrow(() -> new SQLException("Currency RUB not found"));

        ExchangeRate exchangeRate = new ExchangeRate(
                null,
                baseCurrency,
                targetCurrency,
                new BigDecimal("98.000000")
        );

        ExchangeRate created = exchangeRateDao.insert(exchangeRate);

        log.info("Created exchange rate:");
        printExchangeRate(created);
    }

    private static void checkUpdateRate(ExchangeRateDao exchangeRateDao) throws SQLException {
        log.info("=== updateRate(\"USD\", \"RUB\", 91.000000) ===");

        Optional<ExchangeRate> beforeUpdate = exchangeRateDao.findByPair("USD", "RUB");

        if (beforeUpdate.isEmpty()) {
            log.info("USD/RUB not found. Update skipped.");
            return;
        }

        BigDecimal oldRate = beforeUpdate.get().getRate();
        BigDecimal newRate = new BigDecimal("91.000000");

        log.info("Old USD/RUB rate = {}", oldRate);

        Optional<ExchangeRate> updatedExchangeRate = exchangeRateDao.updateRate(
                "USD",
                "RUB",
                newRate
        );

        if (updatedExchangeRate.isEmpty()) {
            log.info("USD/RUB was found before update, but update returned empty result.");
            return;
        }

        log.info("Updated exchange rate:");
        printExchangeRate(updatedExchangeRate.get());

        exchangeRateDao.updateRate("USD", "RUB", oldRate);

        log.info("Old USD/RUB rate restored: {}", oldRate);
    }


    private static void printExchangeRate(ExchangeRate exchangeRate) {
        log.info(
                "id = {}, pair = {}/{},  rate = {}",
                exchangeRate.getId(),
                exchangeRate.getBaseCurrency().getCode(),
                exchangeRate.getTargetCurrency().getCode(),
                exchangeRate.getRate()
        );
    }
}