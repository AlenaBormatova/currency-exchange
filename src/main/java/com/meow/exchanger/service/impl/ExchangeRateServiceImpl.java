package com.meow.exchanger.service.impl;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dao.ExchangeRateDao;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.NotFoundException;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.ExchangeRate;
import com.meow.exchanger.service.ExchangeRateService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.meow.exchanger.util.CurrencyCodeValidator.normalize;

public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateDao exchangeRateDao;
    private final CurrencyDao currencyDao;

    public ExchangeRateServiceImpl(ExchangeRateDao exchangeRateDao, CurrencyDao currencyDao) {
        this.exchangeRateDao = exchangeRateDao;
        this.currencyDao = currencyDao;
    }

    @Override
    public List<ExchangeRate> findAll() {
        return exchangeRateDao.findAll();
    }

    @Override
    public Optional<ExchangeRate> findByPair(String currencyPair) {
        CurrencyPair pair = parseCurrencyPair(currencyPair);

        return exchangeRateDao.findByPair(pair.baseCode(), pair.targetCode());
    }

    @Override
    public ExchangeRate insert(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {

        String normalizedBaseCode = normalize(baseCurrencyCode);
        String normalizedTargetCode = normalize(targetCurrencyCode);

        validateDifferentCurrencies(normalizedBaseCode, normalizedTargetCode);

        validateRate(rate);

        ExchangeRate exchangeRate = prepareExchangeRateForInsert(
                normalizedBaseCode,
                normalizedTargetCode,
                rate
        );

        return exchangeRateDao.insert(exchangeRate);
    }

    @Override
    public ExchangeRate updateRate(String currencyPair, BigDecimal rate) {
        CurrencyPair pair = parseCurrencyPair(currencyPair);

        validateRate(rate);

        return exchangeRateDao.updateRate(pair.baseCode(), pair.targetCode(), rate)
                .orElseThrow(() -> new NotFoundException("Exchange rate not found"));
    }

    private ExchangeRate prepareExchangeRateForInsert(
            String baseCurrencyCode,
            String targetCurrencyCode,
            BigDecimal rate
    ) {
        Currency baseCurrency = findRequiredCurrency(baseCurrencyCode);
        Currency targetCurrency = findRequiredCurrency(targetCurrencyCode);

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

        String normalizedBaseCode = normalize(baseCode);
        String normalizedTargetCode = normalize(targetCode);

        validateDifferentCurrencies(normalizedBaseCode, normalizedTargetCode);

        return new CurrencyPair(normalizedBaseCode, normalizedTargetCode);
    }

    private Currency findRequiredCurrency(String code) {
        return currencyDao.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Currency not found"));
    }

    private void validateRate(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid rate value");
        }
    }

    private void validateDifferentCurrencies(String normalizedBaseCode, String normalizedTargetCode) {
        if (normalizedBaseCode.equals(normalizedTargetCode)) {
            throw new BadRequestException("Base and target currencies must be different");
        }
    }

    private record CurrencyPair(String baseCode, String targetCode) {
    }
}