package com.meow.exchanger.servlet;

import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.ObjectNotFoundException;
import com.meow.exchanger.mapper.ResponseMapper;
import com.meow.exchanger.model.ExchangeRate;
import com.meow.exchanger.service.ExchangeRateService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static com.meow.exchanger.servlet.util.JsonResponseUtil.writeJsonResponseOk;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private ExchangeRateService exchangeRateService;

    @Override
    public void init() {
        exchangeRateService = (ExchangeRateService) getServletContext().getAttribute("exchangeRateService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            throw new BadRequestException("Currency pair is missing in the path");
        }

        String currencyPair = pathInfo.substring(1);

        ExchangeRate exchangeRate = exchangeRateService.findByPair(currencyPair)
                .orElseThrow(() -> new ObjectNotFoundException("Exchange rate not found"));

        writeJsonResponseOk(ResponseMapper.toExchangeRateResponse(exchangeRate), response);
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            throw new BadRequestException("Currency pair is missing in the path");
        }

        String currencyPair = pathInfo.substring(1);

        BigDecimal parsedRate = parseRateFromRequest(request);

        ExchangeRate exchangeRate = exchangeRateService.updateRate(currencyPair, parsedRate);

        writeJsonResponseOk(ResponseMapper.toExchangeRateResponse(exchangeRate), response);
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

    private BigDecimal parseRateFromRequest(HttpServletRequest request) throws IOException {
        BigDecimal parsedRate = null;

        String requestBody = request.getReader().readLine();

        if (requestBody == null || requestBody.isBlank()) {
            throw new BadRequestException("Invalid rate");
        }

        String[] parameters = requestBody.split("&");

        for (String parameter : parameters) {
            String[] parts = parameter.split("=", 2);

            try {
                String decodedParameterName = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);

                if (decodedParameterName.equals("rate") && parts.length >= 2) {
                    String decodedRate = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);

                    if (!decodedRate.isBlank()) {
                        parsedRate = new BigDecimal(decodedRate);
                    }
                }

            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid rate");
            }
        }

        if (parsedRate == null) {
            throw new BadRequestException("Invalid rate");
        }
        return parsedRate;
    }
}