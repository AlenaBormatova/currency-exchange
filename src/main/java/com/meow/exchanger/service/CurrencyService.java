package com.meow.exchanger.service;

import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.CurrencyCode;

import java.util.List;

public interface CurrencyService {

    List<Currency> findAll();

    Currency findByCode(CurrencyCode code);

    Currency insert(String name, CurrencyCode code, String sign);
}