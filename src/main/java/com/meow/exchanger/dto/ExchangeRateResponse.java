package com.meow.exchanger.dto;

import java.math.BigDecimal;

public record ExchangeRateResponse(
        Integer id,
        CurrencyResponse baseCurrency,
        CurrencyResponse targetCurrency,
        BigDecimal rate
) {
}