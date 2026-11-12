package com.meow.exchanger.dto;

public record CurrencyResponse(
        Integer id,
        String name,
        String code,
        String sign
) {
}