package com.meow.exchanger.dao;

import com.meow.exchanger.model.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateDao {
    List<ExchangeRate> findAll();

    Optional<ExchangeRate> findByPair(String baseCode, String targetCode);

    ExchangeRate insert(ExchangeRate exchangeRate);

    Optional<ExchangeRate> updateRate(String baseCode, String targetCode, BigDecimal rate);
}