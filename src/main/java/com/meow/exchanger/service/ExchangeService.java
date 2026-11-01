package com.meow.exchanger.service;

import com.meow.exchanger.dto.ExchangeResponse;
import com.meow.exchanger.exception.NotFoundException;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Optional;

public class ExchangeService {
    private final ExchangeRateService exchangeRateService;

    public ExchangeService(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    public ExchangeResponse exchange(String fromCode, String toCode, BigDecimal amount) throws SQLException {

        String currencyPair = fromCode + toCode;

        Optional<ExchangeRate> exchangeRate = exchangeRateService.findByPair(currencyPair);

        if (exchangeRate.isPresent()) {
            return createDirectExchangeResponse(exchangeRate.get(), amount);
        }

        String reversePair = toCode + fromCode;

        Optional<ExchangeRate> reverseRate = exchangeRateService.findByPair(reversePair);

        if (reverseRate.isPresent()) {
            return createInverseExchangeResponse(reverseRate.get(), amount);
        }

        String usdToBasePair = "USD" + fromCode;
        String usdToTargetPair = "USD" + toCode;

        Optional<ExchangeRate> usdToBaseRate = exchangeRateService.findByPair(usdToBasePair);
        Optional<ExchangeRate> usdToTargetRate = exchangeRateService.findByPair(usdToTargetPair);

        if (usdToBaseRate.isPresent() && usdToTargetRate.isPresent()) {
            return createCrossExchangeResponse(usdToBaseRate.get(), usdToTargetRate.get(), amount);
        }

        throw new NotFoundException("Exchange rate not found");
    }

    private ExchangeResponse createDirectExchangeResponse(ExchangeRate exchangeRate, BigDecimal amount) {

        return createExchangeResponse(
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                exchangeRate.getRate(),
                amount
        );
    }

    private ExchangeResponse createInverseExchangeResponse(ExchangeRate exchangeRate, BigDecimal amount) {
        BigDecimal rate = exchangeRate.getRate();

        BigDecimal directRate = BigDecimal.ONE.divide(
                rate,
                6,
                RoundingMode.HALF_EVEN
        );

        return createExchangeResponse(
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                directRate,
                amount
        );

    }

    private ExchangeResponse createCrossExchangeResponse(
            ExchangeRate usdToBaseRate,
            ExchangeRate usdToTargetRate,
            BigDecimal amount) {

        BigDecimal rateBase = usdToBaseRate.getRate();
        BigDecimal rateTarget = usdToTargetRate.getRate();

        BigDecimal directRate = rateTarget.divide(
                rateBase,
                6,
                RoundingMode.HALF_EVEN
        );

        return createExchangeResponse(
                usdToBaseRate.getTargetCurrency(),
                usdToTargetRate.getTargetCurrency(),
                directRate,
                amount
        );
    }

    private ExchangeResponse createExchangeResponse(Currency baseCurrency,
                                                    Currency targetCurrency,
                                                    BigDecimal rate,
                                                    BigDecimal amount) {
        BigDecimal convertedAmount = amount
                .multiply(rate)
                .setScale(2, RoundingMode.HALF_EVEN);

        return new ExchangeResponse(
                baseCurrency,
                targetCurrency,
                rate,
                amount,
                convertedAmount
        );
    }
}