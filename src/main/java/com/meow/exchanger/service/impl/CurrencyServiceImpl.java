package com.meow.exchanger.service.impl;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.ObjectNotFoundException;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.service.CurrencyService;

import java.util.List;

import static com.meow.exchanger.util.CurrencyCodeValidator.validateCurrencyCode;

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
    public Currency findByCode(String code) {
        String normalizedCode = validateCurrencyCode(code);

        return currencyDao.findByCode(normalizedCode)
                .orElseThrow(() -> new ObjectNotFoundException("Currency not found"));
    }

    @Override
    public Currency insert(String name, String code, String sign) {
        Currency currency = prepareCurrencyForInsert(name, code, sign);

        return currencyDao.insert(currency);
    }

    private Currency prepareCurrencyForInsert(String name, String code, String sign) {
        String normalizedName = normalizeRequired(name, "Currency name is missing");
        String normalizedCode = validateCurrencyCode(code);
        String normalizedSign = normalizeRequired(sign, "Currency sign is missing");

        return new Currency(
                null,
                normalizedCode,
                normalizedName,
                normalizedSign
        );
    }

    private String normalizeRequired(String value, String blankMessage) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(blankMessage);
        }

        return value.trim();
    }
}