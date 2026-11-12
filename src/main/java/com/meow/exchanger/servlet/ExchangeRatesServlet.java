package com.meow.exchanger.servlet;

import com.google.gson.Gson;
import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dao.ExchangeRateDao;
import com.meow.exchanger.dto.ErrorResponse;
import com.meow.exchanger.dto.ExchangeRateResponse;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.ConflictException;
import com.meow.exchanger.exception.DatabaseException;
import com.meow.exchanger.exception.NotFoundException;
import com.meow.exchanger.mapper.ResponseMapper;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.ExchangeRate;
import com.meow.exchanger.service.ExchangeRateService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRatesServlet.class);

    private final Gson gson = new Gson();
    private ExchangeRateService exchangeRateService;

    @Override
    public void init() {
        CurrencyDao currencyDao = new CurrencyDao();
        ExchangeRateDao exchangeRateDao = new ExchangeRateDao();
        exchangeRateService = new ExchangeRateService(exchangeRateDao, currencyDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            List<ExchangeRate> exchangeRates = exchangeRateService.findAll();

            List<ExchangeRateResponse> exchangeRateResponses = exchangeRates.stream()
                    .map(ResponseMapper::toExchangeRateResponse)
                    .toList();

            sendJsonResponse(
                    gson.toJson(exchangeRateResponses),
                    response,
                    HttpServletResponse.SC_OK
            );

        } catch (DatabaseException e) {
            log.error("Failed to get exchange rates", e);

            sendJsonResponse(
                    gson.toJson(new ErrorResponse("Internal server error")),
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String baseCurrencyCode = request.getParameter("baseCurrencyCode");
        String targetCurrencyCode = request.getParameter("targetCurrencyCode");
        String rate = request.getParameter("rate");

        try {
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
                    gson.toJson(exchangeRateResponse),
                    response,
                    HttpServletResponse.SC_CREATED
            );

        } catch (BadRequestException e) {
            sendJsonResponse(
                    gson.toJson(new ErrorResponse(e.getMessage())),
                    response,
                    HttpServletResponse.SC_BAD_REQUEST
            );

        } catch (ConflictException e) {
            sendJsonResponse(
                    gson.toJson(new ErrorResponse(e.getMessage())),
                    response,
                    HttpServletResponse.SC_CONFLICT
            );

        } catch (NotFoundException e) {
            sendJsonResponse(
                    gson.toJson(new ErrorResponse(e.getMessage())),
                    response,
                    HttpServletResponse.SC_NOT_FOUND
            );

        } catch (DatabaseException e) {
            log.error("Failed to create exchange rate", e);

            sendJsonResponse(
                    gson.toJson(new ErrorResponse("Internal server error")),
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
        }
    }

    private void sendJsonResponse(String json, HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
        response.getWriter().write(json);
    }
}