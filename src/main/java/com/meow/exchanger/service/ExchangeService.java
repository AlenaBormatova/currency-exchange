package com.meow.exchanger.service;

import com.meow.exchanger.model.CurrencyPair;
import com.meow.exchanger.model.ExchangeResult;

import java.math.BigDecimal;

public interface ExchangeService {

    ExchangeResult exchange(CurrencyPair pair, BigDecimal amount);
}