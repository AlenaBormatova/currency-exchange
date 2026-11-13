package com.meow.exchanger.servlet;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.mapper.ResponseMapper;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.service.CurrencyService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.meow.exchanger.util.JsonResponseUtil.sendJsonResponse;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private CurrencyService currencyService;

    @Override
    public void init() {
        CurrencyDao currencyDao = new CurrencyDao();
        currencyService = new CurrencyService(currencyDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            throw new BadRequestException("Currency code is missing in the path");
        }

        String code = pathInfo.substring(1);

        Currency currency = currencyService.findByCode(code);

        sendJsonResponse(
                ResponseMapper.toCurrencyResponse(currency),
                response,
                HttpServletResponse.SC_OK
        );
    }
}