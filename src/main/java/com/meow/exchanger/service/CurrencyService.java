package com.meow.exchanger.service;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.ConflictException;
import com.meow.exchanger.exception.NotFoundException;
import com.meow.exchanger.model.Currency;

import java.sql.SQLException;
import java.util.List;

public class CurrencyService {

    private static final String CURRENCY_CODE_PATTERN = "[A-Z]{3}";

    private final CurrencyDao currencyDao;

    public CurrencyService() {
        this.currencyDao = new CurrencyDao();
    }

    public CurrencyService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    public List<Currency> findAll() throws SQLException {
        return currencyDao.findAll();
    }

    public Currency findByCode(String code) throws SQLException {
        String normalizedCode = normalizeCode(code, "Currency code is missing in the path");

        return currencyDao.findByCode(normalizedCode)
                .orElseThrow(() -> new NotFoundException("Currency not found"));
    }

    public Currency insert(String name, String code, String sign) throws SQLException {
        Currency currency = prepareCurrencyForInsert(name, code, sign);
        currencyCodeIsUnique(currency.getCode());

        return currencyDao.insert(currency);
    }

    private Currency prepareCurrencyForInsert(String name, String code, String sign) {
        String normalizedName = normalizeRequired(name, "Currency name is missing");
        String normalizedCode = normalizeCode(code, "Currency code is missing");
        String normalizedSign = normalizeRequired(sign, "Currency sign is missing");

        return new Currency(
                null,
                normalizedCode,
                normalizedName,
                normalizedSign
        );
    }

    private void currencyCodeIsUnique(String code) throws SQLException {
        if (currencyDao.findByCode(code).isPresent()) {
            throw new ConflictException("Currency with this code already exists");
        }
    }

    private String normalizeCode(String code, String blankMessage) {
        String normalizedCode = normalizeRequired(code, blankMessage).toUpperCase();

        if (!normalizedCode.matches(CURRENCY_CODE_PATTERN)) {
            throw new BadRequestException("Currency code must consist of 3 Latin letters");
        }

        return normalizedCode;
    }

    private String normalizeRequired(String value, String blankMessage) {
        if (isBlank(value)) {
            throw new BadRequestException(blankMessage);
        }

        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}