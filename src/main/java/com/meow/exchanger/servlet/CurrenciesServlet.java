package com.meow.exchanger.servlet;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dto.CurrencyResponse;
import com.meow.exchanger.dto.ErrorResponse;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.ConflictException;
import com.meow.exchanger.exception.DatabaseException;
import com.meow.exchanger.mapper.ResponseMapper;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.service.CurrencyService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.meow.exchanger.util.JsonResponseUtil.sendJsonResponse;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(CurrenciesServlet.class);

    private CurrencyService currencyService;

    @Override
    public void init() {
        CurrencyDao currencyDao = new CurrencyDao();
        currencyService = new CurrencyService(currencyDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            List<Currency> currencies = currencyService.findAll();

            List<CurrencyResponse> currencyResponses = currencies.stream()
                    .map(ResponseMapper::toCurrencyResponse)
                    .toList();

            sendJsonResponse(
                    currencyResponses,
                    response,
                    HttpServletResponse.SC_OK
            );

        } catch (DatabaseException e) {
            log.error("Failed to get currencies", e);

            sendJsonResponse(
                    new ErrorResponse("Internal server error"),
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String sign = request.getParameter("sign");

        try {
            Currency currency = currencyService.insert(name, code, sign);

            CurrencyResponse currencyResponse = ResponseMapper.toCurrencyResponse(currency);

            sendJsonResponse(
                    currencyResponse,
                    response,
                    HttpServletResponse.SC_CREATED
            );

        } catch (BadRequestException e) {
            sendJsonResponse(
                    new ErrorResponse(e.getMessage()),
                    response,
                    HttpServletResponse.SC_BAD_REQUEST
            );

        } catch (ConflictException e) {
            sendJsonResponse(
                    new ErrorResponse(e.getMessage()),
                    response,
                    HttpServletResponse.SC_CONFLICT
            );

        } catch (DatabaseException e) {
            log.error("Failed to create currency", e);

            sendJsonResponse(
                    new ErrorResponse("Internal server error"),
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
        }
    }
}