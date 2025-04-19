package com.example.servlets;

import com.example.config.JpaUtil;
import com.example.dao.UserDAO;
import com.example.entities.APP_USERS;
import com.google.gson.Gson;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet("/users")
public class UserServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            BufferedReader reader = req.getReader();
            APP_USERS user = gson.fromJson(reader, APP_USERS.class);

            if (user.getName() == null || user.getEmail() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Name and email are required\"}");
                return;
            }

            JpaUtil.beginTransaction(em);
            new UserDAO(em).addUser(user);
            JpaUtil.commitTransaction(em);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"message\":\"User created successfully\"}");
        } catch (Exception e) {
            JpaUtil.rollbackTransaction(em);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        } finally {
            JpaUtil.closeEntityManager(em);
        }
    }
}