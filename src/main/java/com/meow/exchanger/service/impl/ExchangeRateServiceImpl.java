package com.meow.exchanger.service.impl;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dao.ExchangeRateDao;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.ObjectNotFoundException;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.CurrencyCode;
import com.meow.exchanger.model.CurrencyPair;
import com.meow.exchanger.model.ExchangeRate;
import com.meow.exchanger.service.ExchangeRateService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
    public Optional<ExchangeRate> findByPair(CurrencyPair pair) {
        return exchangeRateDao.findByPair(pair.baseCode().value(), pair.targetCode().value());
    }

    @Override
    public ExchangeRate insert(CurrencyPair pair, BigDecimal rate) {
        validateRate(rate);

        Currency baseCurrency = findRequiredCurrency(pair.baseCode());
        Currency targetCurrency = findRequiredCurrency(pair.targetCode());

        ExchangeRate exchangeRate = new ExchangeRate(
                null,
                baseCurrency,
                targetCurrency,
                rate
        );

        return exchangeRateDao.insert(exchangeRate);
    }

    @Override
    public ExchangeRate updateRate(CurrencyPair pair, BigDecimal rate) {
        validateRate(rate);

        return exchangeRateDao.updateRate(pair.baseCode().value(), pair.targetCode().value(), rate)
                .orElseThrow(() -> new ObjectNotFoundException("Exchange rate not found"));
    }

    private Currency findRequiredCurrency(CurrencyCode code) {
        return currencyDao.findByCode(code.value())
                .orElseThrow(() -> new ObjectNotFoundException("Currency not found"));
    }

    private void validateRate(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid rate value");
        }
    }
}