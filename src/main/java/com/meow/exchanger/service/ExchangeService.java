package com.meow.exchanger.service;

import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.NotFoundException;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.ExchangeRate;
import com.meow.exchanger.model.ExchangeResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static com.meow.exchanger.util.CurrencyCodeValidator.normalize;

public class ExchangeService {

    private final ExchangeRateService exchangeRateService;

    public ExchangeService(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    public ExchangeResult exchange(String fromCode, String toCode, BigDecimal amount) {
        fromCode = normalize(fromCode);
        toCode = normalize(toCode);

        if (fromCode.equals(toCode)) {
            throw new BadRequestException("Base and target currencies must be different");
        }

        String currencyPair = fromCode + toCode;

        Optional<ExchangeRate> directRate = exchangeRateService.findByPair(currencyPair);

        if (directRate.isPresent()) {
            return createDirectExchangeResult(directRate.get(), amount);
        }

        String reversePair = toCode + fromCode;

        Optional<ExchangeRate> reverseRate = exchangeRateService.findByPair(reversePair);

        if (reverseRate.isPresent()) {
            return createInverseExchangeResult(reverseRate.get(), amount);
        }

        if (fromCode.equals("USD") || toCode.equals("USD")) {
            throw new NotFoundException("Exchange rate not found");
        }

        String usdToBasePair = "USD" + fromCode;
        String usdToTargetPair = "USD" + toCode;

        Optional<ExchangeRate> usdToBaseRate = exchangeRateService.findByPair(usdToBasePair);
        Optional<ExchangeRate> usdToTargetRate = exchangeRateService.findByPair(usdToTargetPair);

        if (usdToBaseRate.isPresent() && usdToTargetRate.isPresent()) {
            return createCrossExchangeResult(
                    usdToBaseRate.get(),
                    usdToTargetRate.get(),
                    amount);
        }

        throw new NotFoundException("Exchange rate not found");
    }

    private ExchangeResult createDirectExchangeResult(ExchangeRate exchangeRate, BigDecimal amount) {

        return createExchangeResult(
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                exchangeRate.getRate(),
                amount
        );
    }

    private ExchangeResult createInverseExchangeResult(ExchangeRate exchangeRate, BigDecimal amount) {
        BigDecimal rate = exchangeRate.getRate();

        BigDecimal directRate = BigDecimal.ONE.divide(
                rate,
                6,
                RoundingMode.HALF_EVEN
        );

        return createExchangeResult(
                exchangeRate.getTargetCurrency(),
                exchangeRate.getBaseCurrency(),
                directRate,
                amount
        );
    }

    private ExchangeResult createCrossExchangeResult(
            ExchangeRate usdToBaseRate,
            ExchangeRate usdToTargetRate,
            BigDecimal amount
    ) {

        BigDecimal rateBase = usdToBaseRate.getRate();
        BigDecimal rateTarget = usdToTargetRate.getRate();

        BigDecimal directRate = rateTarget.divide(
                rateBase,
                6,
                RoundingMode.HALF_EVEN
        );

        return createExchangeResult(
                usdToBaseRate.getTargetCurrency(),
                usdToTargetRate.getTargetCurrency(),
                directRate,
                amount
        );
    }

    private ExchangeResult createExchangeResult(
            Currency baseCurrency,
            Currency targetCurrency,
            BigDecimal rate,
            BigDecimal amount
    ) {
        BigDecimal convertedAmount = amount
                .multiply(rate)
                .setScale(2, RoundingMode.HALF_EVEN);

        return new ExchangeResult(
                baseCurrency,
                targetCurrency,
                rate,
                amount,
                convertedAmount
        );
    }
}