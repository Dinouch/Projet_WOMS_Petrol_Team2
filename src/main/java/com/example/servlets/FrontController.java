package com.example.servlets;

import com.example.dao.DrillingParametersDAO;
import com.example.dao.FichierDrillingDAO;
import com.example.dao.UserDAO;
import com.example.entities.APP_USERS;
import com.example.entities.DRILLING_PARAMETERS;
import com.example.entities.FICHIER_DRILLING;
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
import org.json.JSONArray;
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
        "/importDrillingParameters",
        "/reports"
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

                case "/reports":
                    handleGetReports(request, response);
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




    private void handleGetReports(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Récupération des paramètres de filtre
        String dateParam = request.getParameter("date");               // Ex: 2024-12-31
        String profondeurParam = request.getParameter("profondeur");  // Ex: 3500
        String puitsParam = request.getParameter("puits");            // Ex: HBN-1
        String operationParam = request.getParameter("operation");    // Ex: Nettoyage

        try {
            List<FICHIER_DRILLING> fichiers = fichierDrillingDAO.findAll();
            JSONArray jsonArray = new JSONArray();

            for (FICHIER_DRILLING fichier : fichiers) {
                // --- Filtrage par dateUpload (champ de la table)
                if (dateParam != null && !dateParam.isEmpty()) {
                    String dateUploadStr = fichier.getDateUpload().toString();
                    if (!dateUploadStr.equals(dateParam)) {
                        continue;
                    }
                }

                // Préparer l'objet JSON résultat
                JSONObject obj = new JSONObject();
                obj.put("id", fichier.getId());
                obj.put("filename", fichier.getNomFichier());
                obj.put("dateUpload", fichier.getDateUpload().toString());

                String jsonData = fichier.getJsonData();
                boolean skip = false; // indicateur si on doit ignorer ce fichier

                if (jsonData != null && !jsonData.isEmpty()) {
                    try {
                        JSONObject jsonParsed = new JSONObject(jsonData);

                        // Extraire les informations depuis header
                        String dateRapport = "";
                        String nomPuits = "";
                        String profondeurStr = "";

                        if (jsonParsed.has("header")) {
                            JSONObject header = jsonParsed.getJSONObject("header");

                            // Date du rapport depuis header.report_date
                            dateRapport = header.optString("report_date", "N/A");

                            // Nom du puits depuis header.well_name
                            nomPuits = header.optString("well_name", "");

                            // Profondeur depuis header.depth_24h_ft
                            profondeurStr = header.optString("depth_24h_ft", "");
                        }

                        // --- Filtres : profondeur, puits
                        if (profondeurParam != null && !profondeurParam.isEmpty()) {
                            if (!profondeurStr.equals(profondeurParam)) {
                                skip = true;
                            }
                        }

                        if (puitsParam != null && !puitsParam.isEmpty()) {
                            if (!nomPuits.equalsIgnoreCase(puitsParam)) {
                                skip = true;
                            }
                        }

                        // --- Filtres : opération (dernière opération non vide)
                        String derniereOperation = "";
                        if (operationParam != null && !operationParam.isEmpty()) {
                            boolean operationFound = false;

                            if (jsonParsed.has("operations") && jsonParsed.getJSONObject("operations").has("operations")) {
                                JSONArray operations = jsonParsed.getJSONObject("operations").getJSONArray("operations");

                                // Chercher la dernière opération non vide
                                for (int i = operations.length() - 1; i >= 0; i--) {
                                    JSONObject operation = operations.getJSONObject(i);
                                    String code = operation.optString("code", "").trim();
                                    String description = operation.optString("description", "").trim();

                                    if (!code.isEmpty() || !description.isEmpty()) {
                                        derniereOperation = code + (description.isEmpty() ? "" : " - " + description);

                                        // Vérifier si l'opération correspond au filtre
                                        if (code.toLowerCase().contains(operationParam.toLowerCase()) ||
                                                description.toLowerCase().contains(operationParam.toLowerCase())) {
                                            operationFound = true;
                                        }
                                        break;
                                    }
                                }

                                if (!operationFound && !derniereOperation.isEmpty()) {
                                    skip = true;
                                }
                            } else {
                                skip = true; // pas d'opérations du tout
                            }
                        } else {
                            // Si pas de filtre opération, récupérer quand même la dernière opération pour l'affichage
                            if (jsonParsed.has("operations") && jsonParsed.getJSONObject("operations").has("operations")) {
                                JSONArray operations = jsonParsed.getJSONObject("operations").getJSONArray("operations");

                                // Chercher la dernière opération non vide
                                for (int i = operations.length() - 1; i >= 0; i--) {
                                    JSONObject operation = operations.getJSONObject(i);
                                    String code = operation.optString("code", "").trim();
                                    String description = operation.optString("description", "").trim();

                                    if (!code.isEmpty() || !description.isEmpty()) {
                                        derniereOperation = code + (description.isEmpty() ? "" : " - " + description);
                                        break;
                                    }
                                }
                            }
                        }

                        // Si on a décidé de skip, on passe au fichier suivant
                        if (skip) continue;

                        // Ajout des informations extraites
                        obj.put("dateRapport", dateRapport);
                        obj.put("profondeurTVD", profondeurStr);
                        obj.put("puits", nomPuits);
                        obj.put("derniereOperation", derniereOperation);

                        // Ajouter des informations supplémentaires depuis mud_information si disponibles
                        if (jsonParsed.has("mud_information")) {
                            JSONObject mudInfo = jsonParsed.getJSONObject("mud_information");

                            // Volume de boue active
                            if (mudInfo.has("VOLUMES_BBL")) {
                                JSONObject volumes = mudInfo.getJSONObject("VOLUMES_BBL");
                                obj.put("boueActive", volumes.optString("ACTIVE", "N/A"));
                            }


                        }



                    } catch (Exception e) {
                        obj.put("error_parsing_jsonData", e.getMessage());
                    }

                } else {
                    obj.put("jsonData", JSONObject.NULL);
                }

                jsonArray.put(obj);
            }

            response.getWriter().write(jsonArray.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            response.getWriter().write(error.toString());
        }
    }

}
