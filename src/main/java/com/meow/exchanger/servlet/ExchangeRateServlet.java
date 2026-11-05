package com.meow.exchanger.servlet;

import com.google.gson.Gson;
import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dao.ExchangeRateDao;
import com.meow.exchanger.dto.ErrorResponse;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.NotFoundException;
import com.meow.exchanger.model.ExchangeRate;
import com.meow.exchanger.service.ExchangeRateService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

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
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                throw new BadRequestException("Currency pair is missing in the path");
            }

            String currencyPair = pathInfo.substring(1);
            ExchangeRate exchangeRate = exchangeRateService.findByPair(currencyPair)
                    .orElseThrow(() -> new NotFoundException("Exchange rate not found"));
            sendJsonResponse(gson.toJson(exchangeRate), response, HttpServletResponse.SC_OK);

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

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                throw new BadRequestException("Currency pair is missing in the path");
            }

            String currencyPair = pathInfo.substring(1);

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

            ExchangeRate exchangeRate = exchangeRateService.updateRate(currencyPair, parsedRate);
            sendJsonResponse(gson.toJson(exchangeRate), response, HttpServletResponse.SC_OK);

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

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if ("PATCH".equals(request.getMethod())) {
            doPatch(request, response);
        } else {
            super.service(request, response);
        }
    }

    private void sendJsonResponse(String json, HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
        response.getWriter().write(json);
    }
}