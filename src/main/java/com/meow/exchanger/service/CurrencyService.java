package com.meow.exchanger.service;

import com.meow.exchanger.model.Currency;

import java.util.List;

public interface CurrencyService {
    List<Currency> findAll();

    Currency findByCode(String code);

    Currency insert(String name, String code, String sign);
}