package com.example.servlets;

import com.example.dao.ProblemeSolutionDAO;
import com.example.entities.ProblemeSolution;
import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Hibernate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/problemes-solutions")
public class ProblemeSolutionServlet extends HttpServlet {

    @EJB
    private ProblemeSolutionDAO problemeSolutionDAO;

    private final Gson gson = new Gson();

    /**
     * Configure les en-têtes CORS pour autoriser les requêtes cross-origin depuis React
     * avec les méthodes HTTP standard et les headers personnalisés
     */
    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
    }


    /**
     * Gère les requêtes GET pour :
     * - Récupérer un problème spécifique par son ID (paramètre 'id')
     * - Lister tous les problèmes avec options de filtrage :
     *   - withPuits: charge les données associées des puits
     *   - withSolutions: filtre les problèmes ayant des solutions
     * Retourne les données au format JSON
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String id = request.getParameter("id");
            String withPuits = request.getParameter("withPuits");
            String withSolutions = request.getParameter("withSolutions");

            if (id != null) {
                // Récupération d'un élément spécifique par ID
                ProblemeSolution ps = problemeSolutionDAO.findById(id);
                if (ps == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson("Aucun problème trouvé avec l'ID: " + id));
                    return;
                }
                out.print(gson.toJson(ps));
            } else {
                // Récupération de la liste selon les filtres
                List<ProblemeSolution> resultats;

                if (Boolean.parseBoolean(withSolutions)) {
                    resultats = problemeSolutionDAO.findWithSolutions();
                } else {
                    resultats = problemeSolutionDAO.findAll();
                }

                // Chargement des puits si demandé
                if (Boolean.parseBoolean(withPuits)) {
                    resultats.forEach(ps -> {
                        if (ps.getPuit() != null) {
                            // Force le chargement des données du puit
                            Hibernate.initialize(ps.getPuit());
                        }
                    });
                }

                out.print(gson.toJson(resultats));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("Erreur lors de la récupération: " + e.getMessage()));
        } finally {
            out.flush();
        }
    }

    /**
     * Gère les requêtes POST pour créer un nouveau problème :
     * - Valide la présence des champs obligatoires (descriptionProbleme, typeProbleme)
     * - Définit automatiquement la date d'ajout
     * - Persiste le nouveau problème en base de données
     * Retourne le problème créé avec son ID généré
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            ProblemeSolution ps = gson.fromJson(request.getReader(), ProblemeSolution.class);

            if (ps.getDescriptionProbleme() == null || ps.getTypeProbleme() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson("descriptionProbleme et typeProbleme sont requis"));
                return;
            }

            // Forcer la date d'ajout à la date système
            ps.setDateAjout(new Date());

            problemeSolutionDAO.create(ps);
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(ps));
        } finally {
            out.flush();
        }
    }


    /**
     * Gère les requêtes PUT pour mettre à jour la solution d'un problème :
     * - Requiert l'ID du problème et la description de la solution
     * - Met à jour uniquement la solution (descriptionSolution)
     * - Ne modifie pas les autres champs du problème
     * Retourne un message de confirmation
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Lecture du JSON depuis le corps
            ProblemeSolution updateRequest = gson.fromJson(request.getReader(), ProblemeSolution.class);

            String id = updateRequest.getId();
            String solution = updateRequest.getDescriptionSolution();

            if (id == null || solution == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson("id et solution sont requis"));
                return;
            }

            problemeSolutionDAO.updateSolution(id, solution);
            out.print(gson.toJson("Solution mise à jour avec succès"));
        } finally {
            out.flush();
        }
    }
}
