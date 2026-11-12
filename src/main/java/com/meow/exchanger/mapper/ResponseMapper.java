package com.meow.exchanger.mapper;

import com.meow.exchanger.dto.CurrencyResponse;
import com.meow.exchanger.dto.ExchangeRateResponse;
import com.meow.exchanger.dto.ExchangeResponse;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.ExchangeRate;
import com.meow.exchanger.model.ExchangeResult;

public final class ResponseMapper {

    private ResponseMapper() {
    }

    public static CurrencyResponse toCurrencyResponse(Currency currency) {
        return new CurrencyResponse(
                currency.getId(),
                currency.getName(),
                currency.getCode(),
                currency.getSign()
        );
    }

    public static ExchangeRateResponse toExchangeRateResponse(ExchangeRate exchangeRate) {
        return new ExchangeRateResponse(
                exchangeRate.getId(),
                toCurrencyResponse(exchangeRate.getBaseCurrency()),
                toCurrencyResponse(exchangeRate.getTargetCurrency()),
                exchangeRate.getRate()
        );
    }

    public static ExchangeResponse toExchangeResponse(ExchangeResult result) {
        return new ExchangeResponse(
                toCurrencyResponse(result.baseCurrency()),
                toCurrencyResponse(result.targetCurrency()),
                result.rate(),
                result.amount(),
                result.convertedAmount()
        );
    }
}