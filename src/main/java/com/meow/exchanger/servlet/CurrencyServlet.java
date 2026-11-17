package com.meow.exchanger.servlet;

import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.mapper.ResponseMapper;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.CurrencyCode;
import com.meow.exchanger.service.CurrencyService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.meow.exchanger.servlet.util.JsonResponseUtil.writeJsonResponseOk;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private CurrencyService currencyService;

    @Override
    public void init() {
        currencyService = (CurrencyService) getServletContext().getAttribute("currencyService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            throw new BadRequestException("Currency code is missing in the path");
        }

        CurrencyCode code = new CurrencyCode(pathInfo.substring(1));

        Currency currency = currencyService.findByCode(code);

        writeJsonResponseOk(ResponseMapper.toCurrencyResponse(currency), response);
    }
}