package com.meow.exchanger.util;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class JsonResponseUtil {

    private static final Gson gson = new Gson();

    private JsonResponseUtil() {
    }

    public static void sendJsonResponse(
            Object data,
            HttpServletResponse response,
            int status
    ) throws IOException {

        response.setStatus(status);
        response.getWriter().write(gson.toJson(data));
    }
}