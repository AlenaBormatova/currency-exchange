package com.meow.exchanger.dto;

import com.meow.exchanger.model.Currency;

import java.math.BigDecimal;

public record ExchangeResponse(
        Currency baseCurrency,
        Currency targetCurrency,
        BigDecimal rate,
        BigDecimal amount,
        BigDecimal convertedAmount
) {}