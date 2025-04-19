package com.example.servlets;

import com.example.config.JpaUtil;
import com.example.dao.UserDAO;
import com.example.entities.APP_USERS;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();

        // Debug des URLs
        System.out.println("ServletPath: " + servletPath);

        if (servletPath.equals("") || servletPath.equals("/")) {
            // Page d'accueil
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
        else if (servletPath.equals("/listusers")) {
            // Gestion des utilisateurs
            handleListUsers(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Page non trouvée");
        }
    }

    private void handleListUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        EntityManager em = null;
        try {
            em = JpaUtil.getEntityManager();
            UserDAO userDAO = new UserDAO(em);

            // Récupération des utilisateurs
            List<APP_USERS> users = userDAO.getAllUsers();

            // Debug
            System.out.println("[DEBUG] Nombre d'utilisateurs: " + users.size());

            // Passage à la JSP
            request.setAttribute("users", users);
            request.getRequestDispatcher("/testuser.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erreur serveur: " + e.getMessage());
        } finally {
            if (em != null) {
                JpaUtil.closeEntityManager(em);
            }
        }
    }
}