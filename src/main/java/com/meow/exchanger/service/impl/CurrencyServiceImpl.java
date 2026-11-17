package com.meow.exchanger.service.impl;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.exception.ObjectNotFoundException;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.CurrencyCode;
import com.meow.exchanger.service.CurrencyService;

import java.util.List;

public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyDao currencyDao;

    public CurrencyServiceImpl(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    @Override
    public List<Currency> findAll() {
        return currencyDao.findAll();
    }

    @Override
    public Currency findByCode(CurrencyCode code) {
        return currencyDao.findByCode(code.value())
                .orElseThrow(() -> new ObjectNotFoundException("Currency not found"));
    }

    @Override
    public Currency insert(String name, CurrencyCode code, String sign) {
        String normalizedName = normalizeRequired(name, "Currency name is required");
        String normalizedSign = normalizeRequired(sign, "Currency sign is required");

        Currency currency = new Currency(
                null,
                code.value(),
                normalizedName,
                normalizedSign
        );

        return currencyDao.insert(currency);
    }

    private String normalizeRequired(String value, String blankMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(blankMessage);
        }

        return value.trim();
    }
}