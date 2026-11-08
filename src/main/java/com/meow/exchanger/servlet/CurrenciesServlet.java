package com.meow.exchanger.servlet;

import com.google.gson.Gson;
import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dto.ErrorResponse;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.ConflictException;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.service.CurrencyService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private CurrencyService currencyService;

    @Override
    public void init() {
        CurrencyDao currencyDao = new CurrencyDao();
        currencyService = new CurrencyService(currencyDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            List<Currency> currencies = currencyService.findAll();
            sendJsonResponse(gson.toJson(currencies), response, HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            ErrorResponse errorResponse = new ErrorResponse("Internal server error");
            sendJsonResponse(gson.toJson(errorResponse), response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String sign = request.getParameter("sign");

        try {
            Currency currency = currencyService.insert(name, code, sign);
            sendJsonResponse(gson.toJson(currency), response, HttpServletResponse.SC_CREATED);

        } catch (BadRequestException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            sendJsonResponse(gson.toJson(errorResponse), response, HttpServletResponse.SC_BAD_REQUEST);

        } catch (ConflictException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            sendJsonResponse(gson.toJson(errorResponse), response, HttpServletResponse.SC_CONFLICT);

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