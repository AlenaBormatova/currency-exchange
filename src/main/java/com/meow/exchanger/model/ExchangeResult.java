package com.meow.exchanger.model;

import java.math.BigDecimal;

public record ExchangeResult(
        Currency baseCurrency,
        Currency targetCurrency,
        BigDecimal rate,
        BigDecimal amount,
        BigDecimal convertedAmount
) {
}