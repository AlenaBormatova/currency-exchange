package com.meow.exchanger.dto;

import java.math.BigDecimal;

public record ExchangeResponse(
        CurrencyResponse baseCurrency,
        CurrencyResponse targetCurrency,
        BigDecimal rate,
        BigDecimal amount,
        BigDecimal convertedAmount
) {
}