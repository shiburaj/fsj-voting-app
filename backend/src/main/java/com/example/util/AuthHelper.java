package com.example.util;

import jakarta.servlet.http.HttpServletRequest;

public class AuthHelper {

    /**
     * Extracts the logged in user ID from standard X-User-Id header.
     * Returns null if not provided or invalid.
     */
    public static Long getUserId(HttpServletRequest request) {
        String header = request.getHeader("X-User-Id");
        if (header != null && !header.trim().isEmpty()) {
            try {
                return Long.parseLong(header.trim());
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
