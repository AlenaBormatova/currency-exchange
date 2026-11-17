package com.meow.exchanger.model;

public record CurrencyCode(String value) {

    private static final String CODE_PATTERN = "[A-Z]{3}";

    public CurrencyCode {
        if (value == null || !value.matches(CODE_PATTERN)) {
            throw new IllegalArgumentException("Currency code must consist of 3 uppercase Latin letters");
        }
    }
}