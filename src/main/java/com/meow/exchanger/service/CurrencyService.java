package com.meow.exchanger.service;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.NotFoundException;
import com.meow.exchanger.model.Currency;

import java.util.List;

import static com.meow.exchanger.util.CurrencyCodeValidator.normalize;

public class CurrencyService {

    private final CurrencyDao currencyDao;

    public CurrencyService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    public List<Currency> findAll() {
        return currencyDao.findAll();
    }

    public Currency findByCode(String code) {
        String normalizedCode = normalize(code);

        return currencyDao.findByCode(normalizedCode)
                .orElseThrow(() -> new NotFoundException("Currency not found"));
    }

    public Currency insert(String name, String code, String sign) {
        Currency currency = prepareCurrencyForInsert(name, code, sign);

        return currencyDao.insert(currency);
    }

    private Currency prepareCurrencyForInsert(String name, String code, String sign) {
        String normalizedName = normalizeRequired(name, "Currency name is missing");
        String normalizedCode = normalize(code);
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