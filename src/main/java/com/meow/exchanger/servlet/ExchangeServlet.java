package com.meow.exchanger.servlet;

import com.google.gson.Gson;
import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dao.ExchangeRateDao;
import com.meow.exchanger.dto.ErrorResponse;
import com.meow.exchanger.dto.ExchangeResponse;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.NotFoundException;
import com.meow.exchanger.service.ExchangeRateService;
import com.meow.exchanger.service.ExchangeService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private ExchangeService exchangeService;

    @Override
    public void init() {
        ExchangeRateDao exchangeRateDao = new ExchangeRateDao();
        CurrencyDao currencyDao = new CurrencyDao();
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDao, currencyDao);
        exchangeService = new ExchangeService(exchangeRateService);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String amount = request.getParameter("amount");

        try {
            if (amount == null || amount.isBlank()) {
                throw new BadRequestException("Invalid amount");
            }

            BigDecimal parsedAmount;
            try {
                parsedAmount = new BigDecimal(amount);
            } catch (NumberFormatException e) {
                throw new BadRequestException("Invalid amount");
            }
            ExchangeResponse exchangeResponse = exchangeService.exchange(from, to, parsedAmount);
            sendJsonResponse(gson.toJson(exchangeResponse), response, HttpServletResponse.SC_OK);

        } catch (BadRequestException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            sendJsonResponse(gson.toJson(errorResponse), response, HttpServletResponse.SC_BAD_REQUEST);

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