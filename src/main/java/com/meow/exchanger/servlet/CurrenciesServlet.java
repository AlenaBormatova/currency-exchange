package com.meow.exchanger.servlet;

import com.meow.exchanger.dto.CurrencyResponse;
import com.meow.exchanger.mapper.ResponseMapper;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.service.CurrencyService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import static com.meow.exchanger.servlet.util.JsonResponseUtil.writeJsonResponseCreated;
import static com.meow.exchanger.servlet.util.JsonResponseUtil.writeJsonResponseOk;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private CurrencyService currencyService;

    @Override
    public void init() {
        currencyService = (CurrencyService) getServletContext().getAttribute("currencyService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<Currency> currencies = currencyService.findAll();

        List<CurrencyResponse> currencyResponses = currencies.stream()
                .map(ResponseMapper::toCurrencyResponse)
                .toList();

        writeJsonResponseOk(currencyResponses, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String sign = request.getParameter("sign");

        Currency currency = currencyService.insert(name, code, sign);

        CurrencyResponse currencyResponse = ResponseMapper.toCurrencyResponse(currency);

        writeJsonResponseCreated(currencyResponse, response);
    }
}