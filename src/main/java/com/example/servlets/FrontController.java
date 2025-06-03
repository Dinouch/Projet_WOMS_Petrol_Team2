package com.example.servlets;

import com.example.dao.DrillingParametersDAO;
import com.example.dao.FichierDrillingDAO;
import com.example.dao.UserDAO;
import com.example.entities.APP_USERS;
import com.example.entities.DRILLING_PARAMETERS;
import com.example.utils.DrillingReportParserEJB;
import com.example.utils.ExcelDailyCostParser;
import com.example.utils.DrillingReportParser;
import jakarta.ejb.EJB;
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
        "/importDrillingParameters"
})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 1,    // 1 MB
        maxFileSize = 1024 * 1024 * 10,         // 10 MB
        maxRequestSize = 1024 * 1024 * 15       // 15 MB
)
public class FrontController extends HttpServlet {

    private static final String UPLOAD_DIRECTORY = "uploads";

    @EJB
    private UserDAO userDAO;

    @EJB
    private DrillingParametersDAO drillingParametersDAO;
    @EJB
    private FichierDrillingDAO fichierDrillingDAO;

    @EJB
    private DrillingReportParserEJB drillingReportParserEJB;


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
        }
    }

    private void handleListUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<APP_USERS> users = userDAO.getAllUsers();
            System.out.println("[DEBUG] Found " + users.size() + " users");

            request.setAttribute("users", users);
            request.getRequestDispatcher("/testuser.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Error fetching users: " + e.getMessage(), e);
        }
    }

    private void handleExcelUpload(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            if (!request.getContentType().startsWith("multipart/form-data")) {
                throw new ServletException("Form must be multipart/form-data");
            }

            Part filePart = request.getPart("excelFile");
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String reportType = request.getParameter("reportType");

            if (fileName == null || fileName.isEmpty()) {
                throw new ServletException("No file selected");
            }

            if (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls")) {
                throw new ServletException("Only .xlsx and .xls files are allowed");
            }

            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String filePath = uploadPath + File.separator + fileName;
            filePart.write(filePath);

            String jsonOutputPath = uploadPath + File.separator + System.currentTimeMillis() + "_result.json";
            JSONObject result = null;

            if ("drilling".equals(reportType)) {
                // L'import effectue à la fois l'enregistrement du fichier Excel en DB
                // et la sauvegarde du JSON dans le fichier
                result = drillingReportParserEJB.importDrillingReport(filePath);

                request.setAttribute("jsonFilePath", jsonOutputPath);
                request.setAttribute("originalFileName", fileName);
                request.setAttribute("jsonString", result.toString());
                request.getRequestDispatcher("/drilling-result.jsp").forward(request, response);
            } else {
                // Cas daily cost
                result = ExcelDailyCostParser.extractDailyCostData(filePath, jsonOutputPath);

                request.setAttribute("excelData", result);
                request.setAttribute("jsonFilePath", jsonOutputPath);
                request.setAttribute("originalFileName", fileName);
                request.getRequestDispatcher("/excel-result.jsp").forward(request, response);
            }
        } catch (Exception e) {
            throw new ServletException("Error processing Excel file: " + e.getMessage(), e);
        }
    }


    private void handleDownloadJson(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
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

            response.setContentType("application/json");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"export_" + System.currentTimeMillis() + ".json\"");

            try (InputStream in = new java.io.FileInputStream(file);
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        } catch (Exception e) {
            throw new ServletException("Error downloading JSON file: " + e.getMessage(), e);
        }
    }
}