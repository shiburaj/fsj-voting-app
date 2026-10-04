package com.example.servlet;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.util.AuthHelper;
import com.example.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/api/auth/register", "/api/auth/login", "/api/auth/logout", "/api/auth/me"})
public class AuthServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getServletPath();

        if ("/api/auth/register".equals(path)) {
            handleRegister(request, response);
        } else if ("/api/auth/login".equals(path)) {
            handleLogin(request, response);
        } else if ("/api/auth/logout".equals(path)) {
            handleLogout(request, response);
        } else {
            JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getServletPath();

        if ("/api/auth/me".equals(path)) {
            handleMe(request, response);
        } else {
            JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User req = JsonUtil.fromJson(request, User.class);
        if (req == null || req.getName() == null || req.getEmail() == null || req.getPassword() == null) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Name, email, and password are required");
            return;
        }

        String name = req.getName().trim();
        String email = req.getEmail().trim().toLowerCase();
        String password = req.getPassword().trim();

        if (name.isEmpty() || email.isEmpty() || password.length() < 4) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid input. Password must be at least 4 characters.");
            return;
        }

        try {
            User existing = userDAO.findByEmail(email);
            if (existing != null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_CONFLICT, "An account with this email already exists.");
                return;
            }

            User user = new User(name, email, password);
            User registered = userDAO.register(user);
            registered.setPassword(null);

            // Store user in HttpSession upon successful registration
            AuthHelper.loginUser(request, registered);

            JsonUtil.sendOk(response, "Registration successful", registered);
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User req = JsonUtil.fromJson(request, User.class);
        if (req == null || req.getEmail() == null || req.getPassword() == null) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Email and password are required");
            return;
        }

        try {
            User user = userDAO.findByEmail(req.getEmail().trim());
            if (user == null || !userDAO.checkPassword(req.getPassword(), user.getPassword())) {
                JsonUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password");
                return;
            }

            user.setPassword(null); // Clear password hash

            // Store user in HttpSession upon successful login
            AuthHelper.loginUser(request, user);

            JsonUtil.sendOk(response, "Login successful", user);
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AuthHelper.logoutUser(request);
        JsonUtil.sendOk(response, "Logout successful", null);
    }

    private void handleMe(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = AuthHelper.getLoggedInUser(request);
        if (user == null) {
            Long userId = AuthHelper.getUserId(request);
            if (userId != null) {
                try {
                    user = userDAO.findById(userId);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (user == null) {
            JsonUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated");
            return;
        }

        user.setPassword(null);
        JsonUtil.sendOk(response, "User profile", user);
    }
}
