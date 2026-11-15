package com.meow.exchanger.dao;

import com.meow.exchanger.model.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyDao {
    List<Currency> findAll();

    Optional<Currency> findByCode(String code);

    Currency insert(Currency currency);
}