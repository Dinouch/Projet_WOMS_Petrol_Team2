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

<<<<<<< Updated upstream
@WebServlet(urlPatterns = {"", "/", "/listusers", "/importJson","/importPuits","/importDelaiOpr","/importJournalDelai","/importCoutOpr","/importJournalQualite"})
=======
@WebServlet(urlPatterns = {
        "", "/",
        "/listusers",
        "/upload-excel",
        "/download-json",
        "/importJson",
        "/importPuits",
        "/importDelaiOpr",
        "/importJournalDelai",
        "/importCoutOpr",
        "/importJournalQualite",
        "/createZone",
        "/drilling-parameters",
        "/importDrillingParameters",
        "/coutAlerts"
})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 1,    // 1 MB
        maxFileSize = 1024 * 1024 * 10,         // 10 MB
        maxRequestSize = 1024 * 1024 * 15       // 15 MB
)
>>>>>>> Stashed changes
public class FrontController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        System.out.println("ServletPath: " + servletPath);

<<<<<<< Updated upstream
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
=======
        try {
            switch (servletPath) {
                case "":
                case "/":
                    request.getRequestDispatcher("/index.jsp").forward(request, response);
                    break;
                case "/listusers":
                    handleListUsers(request, response);
                    break;
                case "/download-json":
                    handleDownloadJson(request, response);
                    break;
                case "/importJson":
                    request.getRequestDispatcher("/importJson.jsp").forward(request, response);
                    break;
                case "/createZone":
                    request.getRequestDispatcher("/createZone.jsp").forward(request, response);
                    break;
                case "/importPuits":
                    request.getRequestDispatcher("/importPuits.jsp").forward(request, response);
                    break;
                case "/importDelaiOpr":
                    request.getRequestDispatcher("/importDelaiOpr.jsp").forward(request, response);
                    break;
                case "/importJournalDelai":
                    request.getRequestDispatcher("/importJournalDelai.jsp").forward(request, response);
                    break;
                case "/importCoutOpr":
                    request.getRequestDispatcher("/importCoutOpr.jsp").forward(request, response);
                    break;
                case "/importJournalQualite":
                    request.getRequestDispatcher("/importJournalQualite.jsp").forward(request, response);
                    break;
                case "/drilling-parameters":
                    handleDrillingParameters(request, response);
                    break;
                case "/importDrillingParameters":
                    request.getRequestDispatcher("/importDrillingParameters.jsp").forward(request, response);
                    break;
                case "/CoutAlertServlet":
                    request.getRequestDispatcher("/coutAlerts.jsp").forward(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Page not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error: " + e.getMessage());
>>>>>>> Stashed changes
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();

<<<<<<< Updated upstream
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
=======
        try {
            switch (servletPath) {
                case "/upload-excel":
                    handleExcelUpload(request, response);
                    break;
                case "/importJson":
                    new ImportJsonServlet().doPost(request, response);
                    break;
                case "/createZone":
                    new CreateZoneServlet().doPost(request, response);
                    break;
                case "/importPuits":
                    new ImportPuitsServlet().doPost(request, response);
                    break;
                case "/importDelaiOpr":
                    new ImportDelaiOprServlet().doPost(request, response);
                    break;
                case "/importJournalDelai":
                    new ImportJournalDelaiServlet().doPost(request, response);
                    break;
                case "/importCoutOpr":
                    new ImportCoutOprServlet().doPost(request, response);
                    break;
                case "/importJournalQualite":
                    new ImportJournalQualiteServlet().doPost(request, response);
                    break;
                case "/importDrillingParameters":
                    new ImportDrillingParametersServlet().doPost(request, response);
                    break;
                case "/coutAlerts":
                    new CoutAlertServlet().doGet(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void handleDrillingParameters(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            DRILLING_PARAMETERS latestParams = drillingParametersDAO.getLatestParameters();
            request.setAttribute("drillingParams", latestParams);
            request.getRequestDispatcher("/drillingDashboard.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Error fetching drilling parameters: " + e.getMessage(), e);
>>>>>>> Stashed changes
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