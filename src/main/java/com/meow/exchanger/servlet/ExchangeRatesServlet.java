package com.meow.exchanger.servlet;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dao.ExchangeRateDao;
import com.meow.exchanger.dto.ExchangeRateResponse;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.mapper.ResponseMapper;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.ExchangeRate;
import com.meow.exchanger.service.ExchangeRateService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static com.meow.exchanger.util.JsonResponseUtil.sendJsonResponse;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

    private ExchangeRateService exchangeRateService;

    @Override
    public void init() {
        CurrencyDao currencyDao = new CurrencyDao();
        ExchangeRateDao exchangeRateDao = new ExchangeRateDao();
        exchangeRateService = new ExchangeRateService(exchangeRateDao, currencyDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<ExchangeRate> exchangeRates = exchangeRateService.findAll();

        List<ExchangeRateResponse> exchangeRateResponses = exchangeRates.stream()
                .map(ResponseMapper::toExchangeRateResponse)
                .toList();

        sendJsonResponse(
                exchangeRateResponses,
                response,
                HttpServletResponse.SC_OK
        );
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String baseCurrencyCode = request.getParameter("baseCurrencyCode");
        String targetCurrencyCode = request.getParameter("targetCurrencyCode");
        String rate = request.getParameter("rate");

        if (rate == null || rate.isBlank()) {
            throw new BadRequestException("Invalid rate");
        }

        BigDecimal parsedRate;

        try {
            parsedRate = new BigDecimal(rate);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid rate");
        }

        Currency baseCurrency = new Currency(null, baseCurrencyCode, null, null);
        Currency targetCurrency = new Currency(null, targetCurrencyCode, null, null);
        ExchangeRate exchangeRate = new ExchangeRate(null, baseCurrency, targetCurrency, parsedRate);

        ExchangeRate createdExchangeRate = exchangeRateService.insert(exchangeRate);

        ExchangeRateResponse exchangeRateResponse = ResponseMapper.toExchangeRateResponse(createdExchangeRate);

        sendJsonResponse(
                exchangeRateResponse,
                response,
                HttpServletResponse.SC_CREATED
        );
    }
}