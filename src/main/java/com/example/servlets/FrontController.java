package com.example.servlets;

import com.example.config.JpaUtil;
import com.example.dao.UserDAO;
import com.example.entities.APP_USERS;
import com.example.utils.ExcelDailyCostParser;
import com.example.utils.DrillingReportParser;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.List;

@WebServlet(urlPatterns = {"", "/", "/listusers", "/upload-excel", "/download-json"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 1,    // 1 MB
        maxFileSize = 1024 * 1024 * 10,         // 10 MB
        maxRequestSize = 1024 * 1024 * 15       // 15 MB
)
public class FrontController extends HttpServlet {

    private static final String UPLOAD_DIRECTORY = "uploads";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        System.out.println("[FrontController] Accessing path: " + servletPath);

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
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Page not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        System.out.println("[FrontController] POST request to: " + servletPath);

        try {
            if ("/upload-excel".equals(servletPath)) {
                handleExcelUpload(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void handleListUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        EntityManager em = null;
        try {
            em = JpaUtil.getEntityManager();
            UserDAO userDAO = new UserDAO(em);

            List<APP_USERS> users = userDAO.getAllUsers();
            System.out.println("[DEBUG] Found " + users.size() + " users");

            request.setAttribute("users", users);
            request.getRequestDispatcher("/testuser.jsp").forward(request, response);

        } finally {
            if (em != null) {
                JpaUtil.closeEntityManager(em);
            }
        }
    }

    private void handleExcelUpload(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, Exception {

        // Vérifie qu'il s'agit bien d'un upload de fichier
        if (!request.getContentType().startsWith("multipart/form-data")) {
            throw new Exception("Form must be multipart/form-data");
        }

        // Récupère le fichier uploadé
        Part filePart = request.getPart("excelFile");
        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

        // Récupère le type de rapport sélectionné
        String reportType = request.getParameter("reportType");

        // Validation du fichier
        if (fileName == null || fileName.isEmpty()) {
            throw new Exception("No file selected");
        }

        if (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls")) {
            throw new Exception("Only .xlsx and .xls files are allowed");
        }

        // Crée le répertoire d'upload si inexistant
        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Sauvegarde le fichier
        String filePath = uploadPath + File.separator + fileName;
        filePart.write(filePath);

        // Nom du fichier JSON de sortie
        String jsonOutputPath = uploadPath + File.separator + System.currentTimeMillis() + "_result.json";
        JSONObject result = null;

        // Traite le fichier Excel selon le type de rapport
        if ("drilling".equals(reportType)) {
            // Parser pour rapport de forage
            result = DrillingReportParser.parseDrillingReport(filePath, jsonOutputPath);

            // Prépare la réponse pour le drilling report
            request.setAttribute("excelData", result);
            request.setAttribute("jsonFilePath", jsonOutputPath);
            request.setAttribute("originalFileName", fileName);
            request.setAttribute("jsonString", result.toString());

            // Forward vers la JSP de rapport de forage
            request.getRequestDispatcher("/drilling-result.jsp").forward(request, response);
        } else {
            // Parser par défaut pour coûts journaliers
            result = ExcelDailyCostParser.extractDailyCostData(filePath, jsonOutputPath);

            // Prépare la réponse pour le daily cost report
            request.setAttribute("excelData", result);
            request.setAttribute("jsonFilePath", jsonOutputPath);
            request.setAttribute("originalFileName", fileName);

            // Forward vers la JSP des coûts journaliers
            request.getRequestDispatcher("/excel-result.jsp").forward(request, response);
        }
    }

    private void handleDownloadJson(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String filePath = request.getParameter("filePath");
        if (filePath == null || filePath.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing filePath parameter");
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "JSON file not found");
            return;
        }

        // Configure la réponse pour le téléchargement
        response.setContentType("application/json");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"export_" + System.currentTimeMillis() + ".json\"");

        // Stream le fichier vers la réponse
        try (InputStream in = new java.io.FileInputStream(file);
             OutputStream out = response.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
}