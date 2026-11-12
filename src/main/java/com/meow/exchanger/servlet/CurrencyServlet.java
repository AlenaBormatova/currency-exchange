package com.meow.exchanger.servlet;

import com.google.gson.Gson;
import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dto.ErrorResponse;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.DatabaseException;
import com.meow.exchanger.exception.NotFoundException;
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

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(CurrencyServlet.class);

    private final Gson gson = new Gson();
    private CurrencyService currencyService;

    @Override
    public void init() {
        CurrencyDao currencyDao = new CurrencyDao();
        currencyService = new CurrencyService(currencyDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                throw new BadRequestException("Currency code is missing in the path");
            }

            String code = pathInfo.substring(1);
            Currency currency = currencyService.findByCode(code);

            sendJsonResponse(
                    gson.toJson(ResponseMapper.toCurrencyResponse(currency)),
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
            log.error("Failed to get currency", e);

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