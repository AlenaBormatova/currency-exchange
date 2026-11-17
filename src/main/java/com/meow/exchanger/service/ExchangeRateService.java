package com.meow.exchanger.service;

import com.meow.exchanger.model.CurrencyPair;
import com.meow.exchanger.model.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateService {

    List<ExchangeRate> findAll();

    Optional<ExchangeRate> findByPair(CurrencyPair pair);

    ExchangeRate insert(CurrencyPair pair, BigDecimal rate);

    ExchangeRate updateRate(CurrencyPair pair, BigDecimal rate);
}