package com.meow.exchanger.service;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dao.ExchangeRateDao;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.ConflictException;
import com.meow.exchanger.exception.NotFoundException;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.ExchangeRate;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.meow.exchanger.service.CurrencyService.CURRENCY_CODE_PATTERN;

public class ExchangeRateService {

    private final ExchangeRateDao exchangeRateDao;
    private final CurrencyDao currencyDao;

    public ExchangeRateService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
        this.exchangeRateDao = new ExchangeRateDao();
    }

    public ExchangeRateService(ExchangeRateDao exchangeRateDao, CurrencyDao currencyDao) {
        this.exchangeRateDao = exchangeRateDao;
        this.currencyDao = currencyDao;
    }

    public List<ExchangeRate> findAll() throws SQLException {
        return exchangeRateDao.findAll();
    }

    public Optional<ExchangeRate> findByPair(String baseCode, String targetCode) throws SQLException {
        String normalizedBaseCode = normalizeCode(baseCode, "Currency code is missing in the path");
        String normalizedTargetCode = normalizeCode(targetCode, "Currency code is missing in the path");

        return exchangeRateDao.findByPair(normalizedBaseCode, normalizedTargetCode);
    }

    public Optional<ExchangeRate> findByPair(String currencyPair) throws SQLException {
        if (currencyPair == null || currencyPair.length() != 6) {
            throw new IllegalArgumentException("Invalid currency pair");
        }

        String baseCode = currencyPair.substring(0, 3);
        String targetCode = currencyPair.substring(3, 6);

        return exchangeRateDao.findByPair(baseCode, targetCode);
    }

    public ExchangeRate insert(ExchangeRate exchangeRate) throws SQLException {
        ExchangeRate newExchangeRate = prepareExchangeRateForInsert(exchangeRate);
        validateExchangeRatePairIsUnique(newExchangeRate.getBaseCurrency().getCode(),
                newExchangeRate.getTargetCurrency().getCode());

        return exchangeRateDao.insert(newExchangeRate);
    }

    private ExchangeRate prepareExchangeRateForInsert(ExchangeRate exchangeRate) throws SQLException {
        validateExchangeRate(exchangeRate);
        BigDecimal rate = exchangeRate.getRate();

        Currency baseCurrency = findRequiredCurrency(exchangeRate.getBaseCurrency().getCode());
        Currency targetCurrency = findRequiredCurrency(exchangeRate.getTargetCurrency().getCode());

        return new ExchangeRate(
                null,
                baseCurrency,
                targetCurrency,
                rate
        );
    }

    private void validateExchangeRate(ExchangeRate exchangeRate) {
        if (exchangeRate == null) {
            throw new BadRequestException("Exchange rate is missing");
        }

        if (exchangeRate.getBaseCurrency() == null || exchangeRate.getTargetCurrency() == null) {
            throw new BadRequestException("Currency is missing");
        }

        BigDecimal rate = exchangeRate.getRate();
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid rate value");
        }
    }

    private Currency findRequiredCurrency(String code) throws SQLException {
        String normalizedCode = normalizeCode(code, "Currency code is missing");
        return currencyDao.findByCode(normalizedCode)
                .orElseThrow(() -> new NotFoundException("Currency not found"));
    }

    private void validateExchangeRatePairIsUnique(String normalizedBaseCode,
                                                  String normalizedTargetCode) throws SQLException {
        if (exchangeRateDao.findByPair(normalizedBaseCode, normalizedTargetCode).isPresent())
            throw new ConflictException("Exchange rate already exists");
    }

    private String normalizeCode(String code, String blankMessage) {
        String normalizedCode = normalizeRequired(code, blankMessage).trim().toUpperCase();

        if (!normalizedCode.matches(CURRENCY_CODE_PATTERN)) {
            throw new BadRequestException("Currency code must consist of 3 Latin letters");
        }

        return normalizedCode;
    }

    private String normalizeRequired(String value, String blankMessage) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(blankMessage);
        }

        return value.trim();
    }
}