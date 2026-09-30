package com.example.util;

import com.example.model.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();

    public static Gson getGson() {
        return gson;
    }

    /**
     * Read the full request body and parse as JSON into the specified class.
     */
    public static <T> T fromJson(HttpServletRequest request, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String body = sb.toString().trim();
        if (body.isEmpty()) {
            return null;
        }
        return gson.fromJson(body, clazz);
    }

    /**
     * Write an object as JSON to the response.
     */
    public static void sendJson(HttpServletResponse response, int statusCode, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }

    /**
     * Write a successful ApiResponse.
     */
    public static <T> void sendOk(HttpServletResponse response, String message, T data) throws IOException {
        sendJson(response, HttpServletResponse.SC_OK, ApiResponse.ok(message, data));
    }

    /**
     * Write an error ApiResponse.
     */
    public static void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        sendJson(response, statusCode, ApiResponse.error(message));
    }
}
