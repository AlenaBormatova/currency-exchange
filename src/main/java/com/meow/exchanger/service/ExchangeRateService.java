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
        String normalizedBaseCode = normalizeCode(baseCode);
        String normalizedTargetCode = normalizeCode(targetCode);

        return exchangeRateDao.findByPair(normalizedBaseCode, normalizedTargetCode);
    }

    public Optional<ExchangeRate> findByPair(String currencyPair) throws SQLException {
        CurrencyPair pair = parseCurrencyPair(currencyPair);

        return exchangeRateDao.findByPair(pair.baseCode(), pair.targetCode());
    }

    public ExchangeRate insert(ExchangeRate exchangeRate) throws SQLException {
        ExchangeRate newExchangeRate = prepareExchangeRateForInsert(exchangeRate);
        validateExchangeRatePairIsUnique(newExchangeRate.getBaseCurrency().getCode(),
                newExchangeRate.getTargetCurrency().getCode());

        return exchangeRateDao.insert(newExchangeRate);
    }

    public ExchangeRate updateRate(String currencyPair, BigDecimal rate) throws SQLException {
        validateRate(rate);

        CurrencyPair pair = parseCurrencyPair(currencyPair);

        return exchangeRateDao.updateRate(pair.baseCode(), pair.targetCode(), rate)
                .orElseThrow(() -> new NotFoundException("Exchange rate not found"));
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

    private CurrencyPair parseCurrencyPair(String currencyPair) {
        if (currencyPair == null || currencyPair.length() != 6) {
            throw new BadRequestException("Invalid currency pair");
        }

        String baseCode = currencyPair.substring(0, 3);
        String targetCode = currencyPair.substring(3, 6);

        String normalizedBaseCode = normalizeCode(baseCode);
        String normalizedTargetCode = normalizeCode(targetCode);

        return new CurrencyPair(normalizedBaseCode, normalizedTargetCode);
    }

    private Currency findRequiredCurrency(String code) throws SQLException {
        String normalizedCode = normalizeCode(code);
        return currencyDao.findByCode(normalizedCode)
                .orElseThrow(() -> new NotFoundException("Currency not found"));
    }


    private void validateExchangeRate(ExchangeRate exchangeRate) {
        if (exchangeRate == null) {
            throw new BadRequestException("Exchange rate is missing");
        }

        if (exchangeRate.getBaseCurrency() == null || exchangeRate.getTargetCurrency() == null) {
            throw new BadRequestException("Currency is missing");
        }

        validateRate(exchangeRate.getRate());
    }

    private void validateRate(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid rate value");
        }
    }

    private void validateExchangeRatePairIsUnique(String normalizedBaseCode,
                                                  String normalizedTargetCode) throws SQLException {
        if (exchangeRateDao.findByPair(normalizedBaseCode, normalizedTargetCode).isPresent())
            throw new ConflictException("Exchange rate already exists");
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Currency code is missing");
        }

        String normalizedCode = code.trim().toUpperCase();

        if (!normalizedCode.matches(CURRENCY_CODE_PATTERN)) {
            throw new BadRequestException("Currency code must consist of 3 Latin letters");
        }

        return normalizedCode;
    }

    private record CurrencyPair(String baseCode, String targetCode) {
    }
}