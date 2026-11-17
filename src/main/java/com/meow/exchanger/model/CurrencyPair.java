package com.meow.exchanger.model;

public record CurrencyPair(CurrencyCode baseCode, CurrencyCode targetCode) {

    public CurrencyPair {
        if (baseCode == null || targetCode == null) {
            throw new IllegalArgumentException("Currency codes are required");
        }

        if (baseCode.equals(targetCode)) {
            throw new IllegalArgumentException("Base and target currencies must be different");
        }
    }

    public static CurrencyPair from(String value) {
        if (value == null || value.length() != 6) {
            throw new IllegalArgumentException("Invalid currency pair");
        }

        return new CurrencyPair(
                new CurrencyCode(value.substring(0, 3)),
                new CurrencyCode(value.substring(3, 6))
        );
    }

    public CurrencyPair reverse() {
        return new CurrencyPair(targetCode, baseCode);
    }
}