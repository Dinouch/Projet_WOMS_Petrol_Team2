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
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Collection;

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
        maxRequestSize = 1024 * 1024 * 50       // 50 MB pour supporter plusieurs fichiers
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
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Support CORS pour les requêtes OPTIONS (preflight)
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCorsHeaders(response);
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

        setCorsHeaders(response);
        String servletPath = request.getServletPath();
        System.out.println("[FrontController] POST request to: " + servletPath);

        try {
            switch (servletPath) {
                case "/upload-excel":
                    handleReactExcelUpload(request, response);
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
            sendJsonError(response, "Error: " + e.getMessage());
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

    /**
     * Gestion spécifique pour les uploads depuis React
     * Cette méthode traite les fichiers Excel et appelle les parsers appropriés
     */
    private void handleReactExcelUpload(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            if (!request.getContentType().startsWith("multipart/form-data")) {
                throw new ServletException("Form must be multipart/form-data");
            }

            // Récupération des paramètres du formulaire React
            String titre = request.getParameter("titre");
            String ref = request.getParameter("ref");
            String description = request.getParameter("description");
            String date = request.getParameter("date");
            String problemsJson = request.getParameter("problems");

            System.out.println("[DEBUG] React form data:");
            System.out.println("- Titre: " + titre);
            System.out.println("- Ref: " + ref);
            System.out.println("- Description: " + description);
            System.out.println("- Date: " + date);
            System.out.println("- Problems: " + problemsJson);

            // Récupération des fichiers
            Collection<Part> fileParts = request.getParts();
            if (fileParts.isEmpty()) {
                throw new ServletException("No files uploaded");
            }

            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            JSONObject finalResult = new JSONObject();
            JSONArray processedFiles = new JSONArray();

            // Traitement de chaque fichier uploadé
            for (Part filePart : fileParts) {
                if (filePart.getName().equals("excelFile") && filePart.getSize() > 0) {
                    String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

                    if (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls")) {
                        continue; // Skip non-Excel files
                    }

                    String filePath = uploadPath + File.separator + System.currentTimeMillis() + "_" + fileName;
                    filePart.write(filePath);

                    JSONObject fileResult = new JSONObject();
                    fileResult.put("fileName", fileName);
                    fileResult.put("filePath", filePath);

                    // Définir deux chemins JSON distincts
                    String drillingJsonPath = uploadPath + File.separator + System.currentTimeMillis() + "_drilling_result.json";
                    String excelJsonPath = uploadPath + File.separator + System.currentTimeMillis() + "_cost_result.json";

                    // Variables pour stocker les résultats des deux parsers
                    JSONObject drillingResult = null;
                    JSONObject excelResult = null;
                    boolean drillingSuccess = false;
                    boolean costSuccess = false;
                    String drillingError = null;
                    String costError = null;

                    // Parser drilling (séparé)
                    try {
                        System.out.println("[DEBUG] Starting drilling parser for: " + fileName);
                        drillingResult = drillingReportParserEJB.importDrillingReport(filePath);
                        drillingSuccess = true;
                        System.out.println("[DEBUG] Drilling parsing completed successfully for: " + fileName);
                    } catch (Exception drillingException) {
                        drillingError = drillingException.getMessage();
                        System.err.println("[ERROR] Drilling parsing failed for " + fileName + ": " + drillingError);
                        drillingException.printStackTrace();
                    }

                    // Parser daily cost (séparé)
                    try {
                        System.out.println("[DEBUG] Starting daily cost parser for: " + fileName);
                        System.out.println("[DEBUG] Excel JSON path: " + excelJsonPath);
                        excelResult = ExcelDailyCostParser.extractDailyCostData(filePath, excelJsonPath);
                        costSuccess = true;
                        System.out.println("[DEBUG] Daily cost parsing completed successfully for: " + fileName);
                    } catch (Exception costException) {
                        costError = costException.getMessage();
                        System.err.println("[ERROR] Daily cost parsing failed for " + fileName + ": " + costError);
                        costException.printStackTrace();
                    }

                    // Configuration des résultats dans fileResult
                    fileResult.put("drillingData", drillingResult);
                    fileResult.put("dailyCostData", excelResult);
                    fileResult.put("drillingJsonPath", drillingJsonPath);
                    fileResult.put("excelJsonPath", excelJsonPath);

                    // Informations détaillées sur le succès de chaque parser
                    fileResult.put("drillingSuccess", drillingSuccess);
                    fileResult.put("costSuccess", costSuccess);

                    // Erreurs détaillées si présentes
                    if (drillingError != null) {
                        fileResult.put("drillingError", drillingError);
                    }
                    if (costError != null) {
                        fileResult.put("costError", costError);
                    }

                    // Succès global : au moins un des deux parsers doit réussir
                    boolean overallSuccess = drillingSuccess || costSuccess;
                    fileResult.put("success", overallSuccess);

                    if (overallSuccess) {
                        if (drillingSuccess && costSuccess) {
                            fileResult.put("type", "combined"); // Les deux ont réussi
                            System.out.println("[DEBUG] Both drilling and daily cost parsing completed for: " + fileName);
                        } else if (drillingSuccess) {
                            fileResult.put("type", "drilling_only"); // Seul drilling a réussi
                            System.out.println("[DEBUG] Only drilling parsing completed for: " + fileName);
                        } else {
                            fileResult.put("type", "cost_only"); // Seul daily cost a réussi
                            System.out.println("[DEBUG] Only daily cost parsing completed for: " + fileName);
                        }
                    } else {
                        fileResult.put("type", "failed"); // Aucun n'a réussi
                        System.out.println("[DEBUG] Both parsers failed for: " + fileName);
                    }

                    processedFiles.put(fileResult);
                }
            }

            // Vérification du succès global
            boolean hasSuccessfulFiles = false;
            boolean hasErrors = false;

            for (int i = 0; i < processedFiles.length(); i++) {
                JSONObject file = processedFiles.getJSONObject(i);
                if (file.optBoolean("success", false)) {
                    hasSuccessfulFiles = true;
                } else {
                    hasErrors = true;
                }
            }

            finalResult.put("success", hasSuccessfulFiles);
            if (hasSuccessfulFiles && hasErrors) {
                finalResult.put("message", "Files processed with some errors - check individual file results");
            } else if (hasSuccessfulFiles) {
                finalResult.put("message", "All files processed successfully");
            } else {
                finalResult.put("message", "All files failed to process");
            }
            finalResult.put("files", processedFiles);

            PrintWriter out = response.getWriter();
            out.print(finalResult.toString());
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            sendJsonError(response, "Error processing files: " + e.getMessage());
        }
    }

    /**
     * Méthode utilitaire pour envoyer les erreurs en format JSON
     */
    private void sendJsonError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        JSONObject error = new JSONObject();
        error.put("success", false);
        error.put("message", message);

        PrintWriter out = response.getWriter();
        out.print(error.toString());
        out.flush();
    }

    /**
     * Méthode existante pour les uploads JSP (conservée pour compatibilité)
     */
    private void handleExcelUpload(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            if (!request.getContentType().startsWith("multipart/form-data")) {
                throw new ServletException("Form must be multipart/form-data");
            }

            Part filePart = request.getPart("excelFile");
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

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

            // Définir deux chemins JSON distincts
            String drillingJsonPath = jsonOutputPath.replace(".json", "_drilling.json");
            String excelJsonPath = jsonOutputPath.replace(".json", "_couts.json");

            // Appel des deux parsers drilling et couts
            JSONObject drillingResult = drillingReportParserEJB.importDrillingReport(filePath);
            JSONObject excelResult = ExcelDailyCostParser.extractDailyCostData(filePath, excelJsonPath);

            // Configuration des attributs pour les deux résultats
            request.setAttribute("drillingResult", drillingResult);
            request.setAttribute("excelResult", excelResult);
            request.setAttribute("drillingJsonPath", drillingJsonPath);
            request.setAttribute("excelJsonPath", excelJsonPath);
            request.setAttribute("originalFileName", fileName);
            request.setAttribute("jsonString", drillingResult.toString());
            request.setAttribute("excelData", excelResult);

            // Redirection vers une page qui peut traiter les deux types de résultats
            // JSP qui gère les deux types
            request.getRequestDispatcher("/combined-result.jsp").forward(request, response);
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

            try (InputStream in = new FileInputStream(file);
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
