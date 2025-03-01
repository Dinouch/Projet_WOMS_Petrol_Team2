package com.example.servlets;

import com.example.config.JpaUtil;
import com.example.dao.UserDAO;
import com.example.entities.User;
import com.google.gson.Gson;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"", "/", "/listusers"})
public class FrontController extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();

        System.out.println("URI: " + uri);
        System.out.println("ContextPath: " + contextPath);
        System.out.println("PathInfo: " + pathInfo);
        System.out.println("ServletPath: " + servletPath);

        // Si l'URL est /petrol ou /petrol/ (racine du contexte)
        if (servletPath.equals("") || servletPath.equals("/")) {
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
        // Si l'URL est /petrol/test
        else if (servletPath.equals("/listusers")) {
            EntityManager em = JpaUtil.getEntityManager();
            UserDAO userDAO = new UserDAO(em);
            List<User> users = userDAO.getAllUsers();
            em.close();

            // Passer les utilisateurs à la page JSP
            request.setAttribute("users", users);
            request.getRequestDispatcher("/testuser.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Page non trouvée");
        }
    }
}