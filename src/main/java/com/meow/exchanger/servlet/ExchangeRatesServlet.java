package com.meow.exchanger.servlet;

import com.google.gson.Gson;
import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dao.ExchangeRateDao;
import com.meow.exchanger.dto.ErrorResponse;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.ConflictException;
import com.meow.exchanger.exception.NotFoundException;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.ExchangeRate;
import com.meow.exchanger.service.ExchangeRateService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            List<ExchangeRate> exchangeRates = exchangeRateService.findAll();
            sendJsonResponse(gson.toJson(exchangeRates), response, HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            ErrorResponse errorResponse = new ErrorResponse("Internal server error");
            sendJsonResponse(gson.toJson(errorResponse), response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

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
            sendJsonResponse(gson.toJson(createdExchangeRate), response, HttpServletResponse.SC_CREATED);

        } catch (BadRequestException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            sendJsonResponse(gson.toJson(errorResponse), response, HttpServletResponse.SC_BAD_REQUEST);

        } catch (ConflictException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            sendJsonResponse(gson.toJson(errorResponse), response, HttpServletResponse.SC_CONFLICT);

        } catch (NotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            sendJsonResponse(gson.toJson(errorResponse), response, HttpServletResponse.SC_NOT_FOUND);

        } catch (SQLException e) {
            ErrorResponse errorResponse = new ErrorResponse("Internal server error");
            sendJsonResponse(gson.toJson(errorResponse), response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendJsonResponse(String json, HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
        response.getWriter().write(json);
    }
}