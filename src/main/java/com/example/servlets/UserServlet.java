package com.example.servlets;

import com.example.config.JpaUtil;
import com.example.dao.UserDAO;
import com.example.entities.User;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
    private final Gson gson = new Gson(); // Objet pour gérer JSON

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EntityManager em = JpaUtil.getEntityManager();
        UserDAO userDAO = new UserDAO(em);
        List<User> users = userDAO.getAllUsers();
        em.close();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(users)); // Convertit la liste en JSON et l'envoie
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Lire le JSON envoyé
            BufferedReader reader = request.getReader();
            User user = gson.fromJson(reader, User.class);

            // Vérifier si les champs requis sont présents
            if (user.getName() == null || user.getEmail() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("{\"error\": \"Nom et email requis !\"}");
                return;
            }

            // Ajouter l'utilisateur dans la base
            EntityManager em = JpaUtil.getEntityManager();
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            em.close();

            // Réponse JSON
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("application/json");
            response.getWriter().println("{\"message\": \"Utilisateur ajouté avec succès !\"}");

        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("{\"error\": \"Format JSON invalide !\"}");
        }
    }
}
