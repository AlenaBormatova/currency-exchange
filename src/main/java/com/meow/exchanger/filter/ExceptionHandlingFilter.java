package com.meow.exchanger.filter;

import com.meow.exchanger.dto.ErrorResponse;
import com.meow.exchanger.exception.BadRequestException;
import com.meow.exchanger.exception.ObjectAlreadyExistsException;
import com.meow.exchanger.exception.ObjectNotFoundException;
import com.meow.exchanger.exception.StorageException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.meow.exchanger.servlet.util.JsonResponseUtil.writeJsonResponse;

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

        } catch (IllegalArgumentException | BadRequestException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());

        } catch (ObjectNotFoundException e) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());

        } catch (ObjectAlreadyExistsException e) {
            sendError(response, HttpServletResponse.SC_CONFLICT, e.getMessage());

        } catch (StorageException e) {
            log.error("Storage error", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");

        } catch (Exception e) {
            log.error("Unexpected error", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
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

        writeJsonResponse(new ErrorResponse(message), response, status);
    }
}