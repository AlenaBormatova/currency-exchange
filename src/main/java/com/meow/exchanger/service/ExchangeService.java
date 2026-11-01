package com.meow.exchanger.service;

import com.meow.exchanger.dto.ExchangeResponse;
import com.meow.exchanger.exception.NotFoundException;
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
        BigDecimal rate = exchangeRate.getRate();

        BigDecimal convertedAmount = amount
                .multiply(rate)
                .setScale(2, RoundingMode.HALF_EVEN);

        return new ExchangeResponse(
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                rate,
                amount,
                convertedAmount
        );
    }

    private ExchangeResponse createInverseExchangeResponse(ExchangeRate exchangeRate, BigDecimal amount) {
        BigDecimal rate = exchangeRate.getRate();

        BigDecimal directRate = BigDecimal.ONE.divide(
                rate,
                6,
                RoundingMode.HALF_EVEN
        );

        BigDecimal convertedAmount = amount
                .multiply(directRate)
                .setScale(2, RoundingMode.HALF_EVEN);

        return new ExchangeResponse(
                exchangeRate.getTargetCurrency(),
                exchangeRate.getBaseCurrency(),
                directRate,
                amount,
                convertedAmount
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

        BigDecimal convertedAmount = amount
                .multiply(directRate)
                .setScale(2, RoundingMode.HALF_EVEN);

        return new ExchangeResponse(
                usdToBaseRate.getTargetCurrency(),
                usdToTargetRate.getTargetCurrency(),
                directRate,
                amount,
                convertedAmount
        );
    }
}