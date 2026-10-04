package com.example.util;

import com.example.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class AuthHelper {

    public static final String SESSION_USER_KEY = "user";
    public static final String SESSION_USER_ID_KEY = "userId";

    /**
     * Stores the authenticated user in the HttpSession upon login / registration.
     */
    public static void loginUser(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER_KEY, user);
        session.setAttribute(SESSION_USER_ID_KEY, user.getId());
    }

    /**
     * Invalidates the current HttpSession upon logout.
     */
    public static void logoutUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Retrieves the logged-in User object from the HttpSession.
     */
    public static User getLoggedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object obj = session.getAttribute(SESSION_USER_KEY);
            if (obj instanceof User) {
                return (User) obj;
            }
        }
        return null;
    }

    /**
     * Extracts the logged in user ID from the HttpSession.
     * Also supports fallback to X-User-Id header for testing convenience.
     */
    public static Long getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object idObj = session.getAttribute(SESSION_USER_ID_KEY);
            if (idObj instanceof Long) {
                return (Long) idObj;
            } else if (idObj instanceof Number) {
                return ((Number) idObj).longValue();
            }
        }

        // Fallback to X-User-Id header if provided
        String header = request.getHeader("X-User-Id");
        if (header != null && !header.trim().isEmpty()) {
            try {
                return Long.parseLong(header.trim());
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
