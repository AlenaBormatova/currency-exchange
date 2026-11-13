package com.meow.exchanger.servlet;

import com.meow.exchanger.dao.CurrencyDao;
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

import static com.meow.exchanger.util.JsonResponseUtil.sendJsonResponse;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private CurrencyService currencyService;

    @Override
    public void init() {
        CurrencyDao currencyDao = new CurrencyDao();
        currencyService = new CurrencyService(currencyDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<Currency> currencies = currencyService.findAll();

        List<CurrencyResponse> currencyResponses = currencies.stream()
                .map(ResponseMapper::toCurrencyResponse)
                .toList();

        sendJsonResponse(
                currencyResponses,
                response,
                HttpServletResponse.SC_OK
        );
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String sign = request.getParameter("sign");

        Currency currency = currencyService.insert(name, code, sign);

        CurrencyResponse currencyResponse = ResponseMapper.toCurrencyResponse(currency);

        sendJsonResponse(
                currencyResponse,
                response,
                HttpServletResponse.SC_CREATED
        );
    }
}