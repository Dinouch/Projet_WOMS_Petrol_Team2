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

@WebServlet(urlPatterns = {"", "/", "/listusers", "/importJson","/importPuits","/importDelaiOpr","/importJournalDelai","/importCoutOpr","/importJournalQualite"})
public class FrontController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        System.out.println("ServletPath: " + servletPath);

        if (servletPath.equals("") || servletPath.equals("/")) {
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
        else if (servletPath.equals("/listusers")) {
            handleListUsers(request, response);
        } else if (servletPath.equals("/importJson")) {
            // Redirige vers la page d'import ou affiche un formulaire
            request.getRequestDispatcher("/importJson.jsp").forward(request, response);
        } else if (servletPath.equals("/createZone")) {
            // Redirige vers la page d'import ou affiche un formulaire
            request.getRequestDispatcher("/createZone.jsp").forward(request, response);
        }else if (servletPath.equals("/importPuits")) {
            // Redirige vers la page d'import ou affiche un formulaire
            request.getRequestDispatcher("/importPuits.jsp").forward(request, response);
        }else if (servletPath.equals("/importDelaiOpr")) {
            // Redirige vers la page d'import ou affiche un formulaire
            request.getRequestDispatcher("/importDelaiOpr.jsp").forward(request, response);
        }else if (servletPath.equals("/importJournalDelai")) {
            // Redirige vers la page d'import ou affiche un formulaire
            request.getRequestDispatcher("/importJournalDelai.jsp").forward(request, response);
        }else if (servletPath.equals("/importCoutOpr")) {
            // Redirige vers la page d'import ou affiche un formulaire
            request.getRequestDispatcher("/importCoutOpr.jsp").forward(request, response);
        }else if (servletPath.equals("/importJournalQualite")) {
            // Redirige vers la page d'import ou affiche un formulaire
            request.getRequestDispatcher("/importJournalQualite.jsp").forward(request, response);
        }else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Page non trouvée");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();

        if (servletPath.equals("/importJson")) {
            // Délègue le traitement à ImportJsonServlet
            new ImportJsonServlet().doPost(request, response);
        } else if (servletPath.equals("/createZone")) {
            // Délègue le traitement à ImportJsonServlet
            new CreateZoneServlet().doPost(request, response);
        } else if (servletPath.equals("/importPuits")) {
            // Délègue le traitement à ImportJsonServlet
            new ImportPuitsServlet().doPost(request, response);
        }else if (servletPath.equals("/importDelaiOpr")) {
            // Délègue le traitement à ImportJsonServlet
            new ImportDelaiOprServlet().doPost(request, response);
        }else if (servletPath.equals("/importJournalDelai")) {
            // Délègue le traitement à ImportJsonServlet
            new ImportJournalDelaiServlet().doPost(request, response);
        }else if (servletPath.equals("/importCoutOpr")) {
            // Délègue le traitement à ImportJsonServlet
            new ImportCoutOprServlet().doPost(request, response);
        }else if (servletPath.equals("/importJournalQualite")) {
            // Délègue le traitement à ImportJsonServlet
            new ImportJournalQualiteServlet().doPost(request, response);
        }else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void handleListUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EntityManager em = null;
        try {
            em = JpaUtil.getEntityManager();
            UserDAO userDAO = new UserDAO(em);
            List<APP_USERS> users = userDAO.getAllUsers();
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