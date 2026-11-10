package com.meow.exchanger.service;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dao.ExchangeRateDao;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.NotFoundException;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.meow.exchanger.util.CurrencyCodeValidator.normalize;

public class ExchangeRateService {

    private final ExchangeRateDao exchangeRateDao;
    private final CurrencyDao currencyDao;

    public ExchangeRateService(ExchangeRateDao exchangeRateDao, CurrencyDao currencyDao) {
        this.exchangeRateDao = exchangeRateDao;
        this.currencyDao = currencyDao;
    }

    public List<ExchangeRate> findAll() {
        return exchangeRateDao.findAll();
    }

    public Optional<ExchangeRate> findByPair(String currencyPair) {
        CurrencyPair pair = parseCurrencyPair(currencyPair);

        return exchangeRateDao.findByPair(pair.baseCode(), pair.targetCode());
    }

    public ExchangeRate insert(ExchangeRate exchangeRate) {
        ExchangeRate newExchangeRate = prepareExchangeRateForInsert(exchangeRate);

        return exchangeRateDao.insert(newExchangeRate);
    }

    public ExchangeRate updateRate(String currencyPair, BigDecimal rate) {
        CurrencyPair pair = parseCurrencyPair(currencyPair);

        validateRate(rate);

        return exchangeRateDao.updateRate(pair.baseCode(), pair.targetCode(), rate)
                .orElseThrow(() -> new NotFoundException("Exchange rate not found"));
    }

    private ExchangeRate prepareExchangeRateForInsert(ExchangeRate exchangeRate) {
        validateExchangeRate(exchangeRate);

        String normalizedBaseCode = normalize(exchangeRate.getBaseCurrency().getCode());
        String normalizedTargetCode = normalize(exchangeRate.getTargetCurrency().getCode());

        validateDifferentCurrencies(normalizedBaseCode, normalizedTargetCode);

        BigDecimal rate = exchangeRate.getRate();

        Currency baseCurrency = findRequiredCurrency(normalizedBaseCode);
        Currency targetCurrency = findRequiredCurrency(normalizedTargetCode);

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
        String normalizedCode = normalize(code);

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

    private void validateDifferentCurrencies(String normalizedBaseCode, String normalizedTargetCode) {
        if (normalizedBaseCode.equals(normalizedTargetCode)) {
            throw new BadRequestException("Base and target currencies must be different");
        }
    }

    private record CurrencyPair(String baseCode, String targetCode) {
    }
}