package com.example.servlets;

import com.example.config.JpaUtil;
import com.example.dao.UserDAO;
import com.example.entities.APP_USERS;
import com.example.PasswordHasher;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.persistence.EntityManager;
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
        EntityManager em = JpaUtil.getEntityManager();
        try {
            BufferedReader reader = req.getReader();
            APP_USERS user = gson.fromJson(reader, APP_USERS.class);

            if (user.getName() == null || user.getEmail() == null || user.getPassword() == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Name, email and password are required");
                return;
            }

            UserDAO dao = new UserDAO(em);
            if (dao.getUserByEmail(user.getEmail()) != null) {
                sendError(resp, HttpServletResponse.SC_CONFLICT, "Email already exists");
                return;
            }

            user.setPassword(PasswordHasher.hashPassword(user.getPassword()));

            JpaUtil.beginTransaction(em);
            dao.addUser(user);
            JpaUtil.commitTransaction(em);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"message\":\"User created successfully\"}");
        } catch (Exception e) {
            JpaUtil.rollbackTransaction(em);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            JpaUtil.closeEntityManager(em);
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
        EntityManager em = JpaUtil.getEntityManager();
        try {
            BufferedReader reader = req.getReader();
            JsonObject authRequest = gson.fromJson(reader, JsonObject.class);

            if (authRequest == null || !authRequest.has("email") || !authRequest.has("password")) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Email and password are required");
                return;
            }

            String email = authRequest.get("email").getAsString();
            String password = authRequest.get("password").getAsString();

            UserDAO dao = new UserDAO(em);
            APP_USERS user = dao.getUserByEmail(email);

            if (user == null || !PasswordHasher.verifyPassword(password, user.getPassword())) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password");
                return;
            }

            JpaUtil.beginTransaction(em);
            user.setConnected(true);
            dao.updateUser(user);
            JpaUtil.commitTransaction(em);

            user.setPassword(null);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(user));
        } catch (Exception e) {
            JpaUtil.rollbackTransaction(em);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            JpaUtil.closeEntityManager(em);
        }
    }

    private void logoutUser(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            UserDAO dao = new UserDAO(em);
            APP_USERS user = dao.getUserById(userId);

            if (user == null) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            JpaUtil.beginTransaction(em);
            user.setConnected(false);
            dao.updateUser(user);
            JpaUtil.commitTransaction(em);

            resp.setContentType("application/json");
            resp.getWriter().write("{\"message\":\"User logged out successfully\"}");
        } catch (Exception e) {
            JpaUtil.rollbackTransaction(em);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            JpaUtil.closeEntityManager(em);
        }
    }

    private void getAllUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            UserDAO dao = new UserDAO(em);
            List<APP_USERS> users = dao.getAllUsers();
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(users));
        } finally {
            JpaUtil.closeEntityManager(em);
        }
    }

    private void getUserById(HttpServletRequest req, HttpServletResponse resp, Long id) throws IOException {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            UserDAO dao = new UserDAO(em);
            APP_USERS user = dao.getUserById(id);

            if (user == null) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(user));
        } finally {
            JpaUtil.closeEntityManager(em);
        }
    }

    private void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}