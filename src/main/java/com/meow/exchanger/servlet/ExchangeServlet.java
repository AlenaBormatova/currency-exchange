package com.meow.exchanger.servlet;

import com.google.gson.Gson;
import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dao.ExchangeRateDao;
import com.meow.exchanger.dto.ErrorResponse;
import com.meow.exchanger.dto.ExchangeResponse;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.DatabaseException;
import com.meow.exchanger.exception.NotFoundException;
import com.meow.exchanger.mapper.ResponseMapper;
import com.meow.exchanger.model.ExchangeResult;
import com.meow.exchanger.service.ExchangeRateService;
import com.meow.exchanger.service.ExchangeService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ExchangeServlet.class);

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

            ExchangeResult result = exchangeService.exchange(from, to, parsedAmount);

            ExchangeResponse exchangeResponse = ResponseMapper.toExchangeResponse(result);

            sendJsonResponse(
                    gson.toJson(exchangeResponse),
                    response,
                    HttpServletResponse.SC_OK
            );

        } catch (BadRequestException e) {
            sendJsonResponse(
                    gson.toJson(new ErrorResponse(e.getMessage())),
                    response,
                    HttpServletResponse.SC_BAD_REQUEST
            );

        } catch (NotFoundException e) {
            sendJsonResponse(
                    gson.toJson(new ErrorResponse(e.getMessage())),
                    response,
                    HttpServletResponse.SC_NOT_FOUND
            );

        } catch (DatabaseException e) {
            log.error("Failed to exchange currency", e);

            sendJsonResponse(
                    gson.toJson(new ErrorResponse("Internal server error")),
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
        }
    }

    private void sendJsonResponse(String json, HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
        response.getWriter().write(json);
    }
}