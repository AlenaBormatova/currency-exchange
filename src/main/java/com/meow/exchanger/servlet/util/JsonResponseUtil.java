package com.meow.exchanger.servlet.util;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class JsonResponseUtil {

    private static final Gson gson = new Gson();

    private JsonResponseUtil() {
    }

    public static void writeJsonResponseOk(
            Object data,
            HttpServletResponse response
    ) throws IOException {
        writeJsonResponse(data, response, HttpServletResponse.SC_OK);
    }

    public static void writeJsonResponseCreated(
            Object data,
            HttpServletResponse response
    ) throws IOException {
        writeJsonResponse(data, response, HttpServletResponse.SC_CREATED);
    }

    public static void writeJsonResponse(
            Object data,
            HttpServletResponse response,
            int status
    ) throws IOException {
        response.setStatus(status);
        response.getWriter().write(gson.toJson(data));
    }
}