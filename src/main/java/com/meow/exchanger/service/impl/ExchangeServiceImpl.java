package com.meow.exchanger.service.impl;

import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.NotFoundException;
import com.meow.exchanger.model.ExchangeRate;
import com.meow.exchanger.model.ExchangeResult;
import com.meow.exchanger.service.ExchangeRateService;
import com.meow.exchanger.service.ExchangeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static com.meow.exchanger.util.CurrencyCodeValidator.normalize;
import static com.meow.exchanger.util.CurrencyCodeValidator.validateDifferentCurrencies;

public class ExchangeServiceImpl implements ExchangeService {

    private static final int MONEY_SCALE = 2;
    private static final int RATE_SCALE = 6;

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    private static final String REFERENCE_CURRENCY_CODE = "USD";

    private final ExchangeRateService exchangeRateService;

    public ExchangeServiceImpl(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public ExchangeResult exchange(String fromCode, String toCode, BigDecimal amount) {

        validateAmount(amount);

        fromCode = normalize(fromCode);
        toCode = normalize(toCode);

        validateDifferentCurrencies(fromCode, toCode);

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

        if (fromCode.equals(REFERENCE_CURRENCY_CODE) || toCode.equals(REFERENCE_CURRENCY_CODE)) {
            throw new NotFoundException("Exchange rate not found");
        }

        String usdToBasePair = REFERENCE_CURRENCY_CODE + fromCode;
        String usdToTargetPair = REFERENCE_CURRENCY_CODE + toCode;

        ExchangeRate usdToBaseRate = exchangeRateService.findByPair(usdToBasePair)
                .orElseThrow(() -> new NotFoundException("Exchange rate not found"));

        ExchangeRate usdToTargetRate = exchangeRateService.findByPair(usdToTargetPair)
                .orElseThrow(() -> new NotFoundException("Exchange rate not found"));

        return createCrossExchangeResult(
                usdToBaseRate,
                usdToTargetRate,
                amount
        );
    }

    private ExchangeResult createDirectExchangeResult(
            ExchangeRate exchangeRate,
            BigDecimal amount
    ) {
        BigDecimal rate = exchangeRate.getRate();

        BigDecimal convertedAmount = amount
                .multiply(rate)
                .setScale(MONEY_SCALE, ROUNDING_MODE);

        return new ExchangeResult(
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                rate,
                amount,
                convertedAmount
        );
    }

    private ExchangeResult createInverseExchangeResult(
            ExchangeRate exchangeRate,
            BigDecimal amount
    ) {
        BigDecimal rate = exchangeRate.getRate();

        BigDecimal calculatedRate = BigDecimal.ONE.divide(
                rate,
                RATE_SCALE,
                ROUNDING_MODE
        );

        BigDecimal convertedAmount = amount.divide(
                rate,
                MONEY_SCALE,
                ROUNDING_MODE
        );

        return new ExchangeResult(
                exchangeRate.getTargetCurrency(),
                exchangeRate.getBaseCurrency(),
                calculatedRate,
                amount,
                convertedAmount
        );
    }

    private ExchangeResult createCrossExchangeResult(
            ExchangeRate usdToBaseRate,
            ExchangeRate usdToTargetRate,
            BigDecimal amount
    ) {
        BigDecimal rateBase = usdToBaseRate.getRate();
        BigDecimal rateTarget = usdToTargetRate.getRate();

        BigDecimal calculatedRate = rateTarget.divide(
                rateBase,
                RATE_SCALE,
                ROUNDING_MODE
        );

        BigDecimal convertedAmount = amount
                .multiply(rateTarget)
                .divide(
                        rateBase,
                        MONEY_SCALE,
                        ROUNDING_MODE
                );

        return new ExchangeResult(
                usdToBaseRate.getTargetCurrency(),
                usdToTargetRate.getTargetCurrency(),
                calculatedRate,
                amount,
                convertedAmount
        );
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new BadRequestException("Amount must not be negative");
        }
    }
}