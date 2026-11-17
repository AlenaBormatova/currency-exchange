package com.meow.exchanger.servlet;

import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.mapper.ResponseMapper;
import com.meow.exchanger.model.CurrencyCode;
import com.meow.exchanger.model.CurrencyPair;
import com.meow.exchanger.model.ExchangeResult;
import com.meow.exchanger.service.ExchangeService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

import static com.meow.exchanger.servlet.util.JsonResponseUtil.writeJsonResponseOk;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private ExchangeService exchangeService;

    @Override
    public void init() {
        exchangeService = (ExchangeService) getServletContext().getAttribute("exchangeService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CurrencyPair pair = new CurrencyPair(
                new CurrencyCode(request.getParameter("from")),
                new CurrencyCode(request.getParameter("to"))
        );

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

        ExchangeResult result = exchangeService.exchange(pair, parsedAmount);

        writeJsonResponseOk(ResponseMapper.toExchangeResponse(result), response);
    }
}