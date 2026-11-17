package com.meow.exchanger.util;

public final class CurrencyCodeValidator {

    private static final String CURRENCY_CODE_PATTERN = "[A-Z]{3}";

    private CurrencyCodeValidator() {
    }

    public static String validateCurrencyCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Currency code is required");
        }

        if (!code.matches(CURRENCY_CODE_PATTERN)) {
            throw new IllegalArgumentException(
                    "Currency code must consist of 3 uppercase Latin letters"
            );
        }

        return code;
    }

    public static void validateDifferentCurrencies(String baseCode, String targetCode) {
        if (baseCode.equals(targetCode)) {
            throw new IllegalArgumentException("Base and target currencies must be different");
        }
    }
}