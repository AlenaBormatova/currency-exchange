package com.meow.exchanger.service;

import com.meow.exchanger.model.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateService {
    List<ExchangeRate> findAll();

    Optional<ExchangeRate> findByPair(String currencyPair);

    ExchangeRate insert(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate);

    ExchangeRate updateRate(String currencyPair, BigDecimal rate);
}