package com.meow.exchanger.filter;

import com.meow.exchanger.dto.ErrorResponse;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.ConflictException;
import com.meow.exchanger.exception.DatabaseException;
import com.meow.exchanger.exception.NotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.meow.exchanger.util.JsonResponseUtil.sendJsonResponse;

public class ExceptionHandlingFilter extends HttpFilter {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlingFilter.class);

    @Override
    protected void doFilter(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException {

        try {
            chain.doFilter(request, response);

        } catch (BadRequestException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (NotFoundException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    e.getMessage()
            );

        } catch (ConflictException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_CONFLICT,
                    e.getMessage()
            );

        } catch (DatabaseException e) {
            log.error("Database error", e);
            sendError(response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error"
            );

        } catch (Exception e) {
            log.error("Unexpected error", e);
            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error"
            );
        }
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        if (response.isCommitted()) {
            throw new IllegalStateException("Cannot send error: response already committed");
        }

        response.reset();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        sendJsonResponse(
                new ErrorResponse(message),
                response,
                status
        );
    }
}