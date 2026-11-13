package com.meow.exchanger.servlet;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dao.ExchangeRateDao;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.mapper.ResponseMapper;
import com.meow.exchanger.model.ExchangeResult;
import com.meow.exchanger.service.ExchangeRateService;
import com.meow.exchanger.service.ExchangeService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

import static com.meow.exchanger.util.JsonResponseUtil.sendJsonResponse;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

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

        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String amount = request.getParameter("amount");

        if (amount == null || amount.isBlank()) {
            throw new BadRequestException("Invalid amount");
        }

        BigDecimal parsedAmount;

        try {
            parsedAmount = new BigDecimal(amount);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid amount");
        }

        ExchangeResult result = exchangeService.exchange(from, to, parsedAmount);

        sendJsonResponse(
                ResponseMapper.toExchangeResponse(result),
                response,
                HttpServletResponse.SC_OK
        );
    }
}