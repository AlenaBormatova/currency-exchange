package com.meow.exchanger.service.impl;

import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.ObjectNotFoundException;
import com.meow.exchanger.model.CurrencyCode;
import com.meow.exchanger.model.CurrencyPair;
import com.meow.exchanger.model.ExchangeRate;
import com.meow.exchanger.model.ExchangeResult;
import com.meow.exchanger.service.ExchangeRateService;
import com.meow.exchanger.service.ExchangeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeServiceImpl implements ExchangeService {

    private static final int MONEY_SCALE = 2;
    private static final int RATE_SCALE = 6;

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    private static final CurrencyCode REFERENCE_CURRENCY_CODE = new CurrencyCode("USD");

    private final ExchangeRateService exchangeRateService;

    public ExchangeServiceImpl(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public ExchangeResult exchange(CurrencyPair pair, BigDecimal amount) {
        validateAmount(amount);

        Optional<ExchangeRate> directRate = exchangeRateService.findByPair(pair);

        if (directRate.isPresent()) {
            return createDirectExchangeResult(directRate.get(), amount);
        }

        Optional<ExchangeRate> reverseRate = exchangeRateService.findByPair(pair.reverse());

        if (reverseRate.isPresent()) {
            return createInverseExchangeResult(reverseRate.get(), amount);
        }

        if (pair.baseCode().equals(REFERENCE_CURRENCY_CODE) || pair.targetCode().equals(REFERENCE_CURRENCY_CODE)) {
            throw new ObjectNotFoundException("Exchange rate not found");
        }

        CurrencyPair usdToBasePair = new CurrencyPair(REFERENCE_CURRENCY_CODE, pair.baseCode());
        CurrencyPair usdToTargetPair = new CurrencyPair(REFERENCE_CURRENCY_CODE, pair.targetCode());

        ExchangeRate usdToBaseRate = exchangeRateService.findByPair(usdToBasePair)
                .orElseThrow(() -> new ObjectNotFoundException("Exchange rate not found"));

        ExchangeRate usdToTargetRate = exchangeRateService.findByPair(usdToTargetPair)
                .orElseThrow(() -> new ObjectNotFoundException("Exchange rate not found"));

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

        BigDecimal calculatedRate = BigDecimal.ONE.divide(rate, RATE_SCALE, ROUNDING_MODE);

        BigDecimal convertedAmount = amount.divide(rate, MONEY_SCALE, ROUNDING_MODE);

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

        BigDecimal calculatedRate = rateTarget.divide(rateBase, RATE_SCALE, ROUNDING_MODE);

        BigDecimal convertedAmount = amount
                .multiply(rateTarget)
                .divide(rateBase, MONEY_SCALE, ROUNDING_MODE);

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