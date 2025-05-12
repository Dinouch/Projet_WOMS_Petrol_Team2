package com.example.servlets;

import com.example.dao.UserDAO;
import com.example.entities.APP_USERS;
import com.example.PasswordHasher;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet("/users/*")
public class UserServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @EJB
    private UserDAO userDAO;

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            getAllUsers(req, resp);
        } else {
            String[] splits = pathInfo.split("/");
            if (splits.length == 2) {
                try {
                    Long userId = Long.parseLong(splits[1]);
                    getUserById(req, resp, userId);
                } catch (NumberFormatException e) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
                }
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        try {
            BufferedReader reader = req.getReader();
            APP_USERS user = gson.fromJson(reader, APP_USERS.class);

            if (user.getName() == null || user.getEmail() == null || user.getPassword() == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Name, email and password are required");
                return;
            }

            if (userDAO.getUserByEmail(user.getEmail()) != null) {
                sendError(resp, HttpServletResponse.SC_CONFLICT, "Email already exists");
                return;
            }

            user.setPassword(PasswordHasher.hashPassword(user.getPassword()));
            userDAO.addUser(user);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"message\":\"User created successfully\"}");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equals("/auth")) {
            authenticateUser(req, resp);
        } else if (pathInfo != null && pathInfo.startsWith("/logout/")) {
            String[] splits = pathInfo.split("/");
            if (splits.length == 3) {
                try {
                    Long userId = Long.parseLong(splits[2]);
                    logoutUser(req, resp, userId);
                } catch (NumberFormatException e) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
                }
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            }
        } else {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
        }
    }

    private void authenticateUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            BufferedReader reader = req.getReader();
            JsonObject authRequest = gson.fromJson(reader, JsonObject.class);

            if (authRequest == null || !authRequest.has("email") || !authRequest.has("password")) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Email and password are required");
                return;
            }

            String email = authRequest.get("email").getAsString();
            String password = authRequest.get("password").getAsString();

            APP_USERS user = userDAO.getUserByEmail(email);

            if (user == null || !PasswordHasher.verifyPassword(password, user.getPassword())) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password");
                return;
            }

            user.setConnected(true);
            userDAO.updateUser(user);

            user.setPassword(null);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(user));
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void logoutUser(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        try {
            APP_USERS user = userDAO.getUserById(userId);

            if (user == null) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            user.setConnected(false);
            userDAO.updateUser(user);

            resp.setContentType("application/json");
            resp.getWriter().write("{\"message\":\"User logged out successfully\"}");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void getAllUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<APP_USERS> users = userDAO.getAllUsers();
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(users));
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void getUserById(HttpServletRequest req, HttpServletResponse resp, Long id) throws IOException {
        try {
            APP_USERS user = userDAO.getUserById(id);

            if (user == null) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(user));
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}