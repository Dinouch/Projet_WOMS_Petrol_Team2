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
import java.util.Iterator;
import java.util.List;
import java.util.Collection;

/**
 * Servlet principal (Front Controller) qui gère toutes les requêtes entrantes.
 * Configure les routes et délègue le traitement aux méthodes appropriées.
 */
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

    // Répertoire pour stocker les fichiers uploadés
    private static final String UPLOAD_DIRECTORY = "uploads";

    // Injection des EJBs pour accéder aux données
    @EJB
    private UserDAO userDAO;

    @EJB
    private DrillingParametersDAO drillingParametersDAO;
    @EJB
    private FichierDrillingDAO fichierDrillingDAO;

    @EJB
    private DrillingReportParserEJB drillingReportParserEJB;

    /**
     * Gère les requêtes OPTIONS pour CORS (Cross-Origin Resource Sharing)
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Support CORS pour les requêtes OPTIONS (preflight)
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Configure les en-têtes CORS pour autoriser les requêtes cross-origin
     */
    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    /**
     * Gère les requêtes GET en fonction du chemin demandé
     */
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
                    // Page d'accueil
                    request.getRequestDispatcher("/index.jsp").forward(request, response);
                    break;
                case "/listusers":
                    // Liste des utilisateurs
                    handleListUsers(request, response);
                    break;
                case "/download-json":
                    // Téléchargement d'un fichier JSON
                    handleDownloadJson(request, response);
                    break;
                case "/importJson":
                    // Page d'import JSON
                    request.getRequestDispatcher("/importJson.jsp").forward(request, response);
                    break;
                case "/createZone":
                    // Page de création de zone
                    request.getRequestDispatcher("/createZone.jsp").forward(request, response);
                    break;
                case "/importPuits":
                    // Page d'import de puits
                    request.getRequestDispatcher("/importPuits.jsp").forward(request, response);
                    break;
                case "/importDelaiOpr":
                    // Page d'import des délais d'opération
                    request.getRequestDispatcher("/importDelaiOpr.jsp").forward(request, response);
                    break;
                case "/importJournalDelai":
                    // Page d'import du journal des délais
                    request.getRequestDispatcher("/importJournalDelai.jsp").forward(request, response);
                    break;
                case "/importCoutOpr":
                    // Page d'import des coûts d'opération
                    request.getRequestDispatcher("/importCoutOpr.jsp").forward(request, response);
                    break;
                case "/importJournalQualite":
                    // Page d'import du journal de qualité
                    request.getRequestDispatcher("/importJournalQualite.jsp").forward(request, response);
                    break;
                case "/drilling-parameters":
                    // Affichage des paramètres de forage
                    handleDrillingParameters(request, response);
                    break;
                case "/importDrillingParameters":
                    // Page d'import des paramètres de forage
                    request.getRequestDispatcher("/importDrillingParameters.jsp").forward(request, response);
                    break;
                case "/reports":
                    // Gestion des rapports
                    handleGetReports(request, response);
                    break;
                default:
                    // Chemin non trouvé
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Page not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error: " + e.getMessage());
        }
    }

    /**
     * Gère les requêtes POST en fonction du chemin demandé
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCorsHeaders(response);
        String servletPath = request.getServletPath();
        System.out.println("[FrontController] POST request to: " + servletPath);

        try {
            switch (servletPath) {
                case "/upload-excel":
                    // Upload de fichiers Excel depuis React et traitement standard
                    handleReactExcelUpload(request, response);
                    handleExcelUpload(request, response);
                    break;
                case "/importJson":
                    // Import de JSON
                    new ImportJsonServlet().doPost(request, response);
                    break;
                case "/createZone":
                    // Création de zone
                    new CreateZoneServlet().doPost(request, response);
                    break;
                case "/importPuits":
                    // Import de puits
                    new ImportPuitsServlet().doPost(request, response);
                    break;
                case "/importDelaiOpr":
                    // Import des délais d'opération
                    new ImportDelaiOprServlet().doPost(request, response);
                    break;
                case "/importCoutOpr":
                    // Import des coûts d'opération
                    new ImportCoutOprServlet().doPost(request, response);
                    break;
                case "/importJournalQualite":
                    // Import du journal de qualité
                    new ImportJournalQualiteServlet().doPost(request, response);
                    break;
                case "/importDrillingParameters":
                    // Import des paramètres de forage
                    new ImportDrillingParametersServlet().doPost(request, response);
                    break;
                default:
                    // Méthode non autorisée
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonError(response, "Error: " + e.getMessage());
            request.setAttribute("errorMessage", "Error: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    /**
     * Récupère et affiche les derniers paramètres de forage
     */
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

    /**
     * Affiche la liste des utilisateurs
     */
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
     * Gère l'upload de fichiers Excel depuis React
     * Traite les fichiers et appelle les parsers appropriés
     */
    private void handleReactExcelUpload(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Vérification du type de contenu
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

            // Récupération des fichiers uploadés
            Collection<Part> fileParts = request.getParts();
            if (fileParts.isEmpty()) {
                throw new ServletException("No files uploaded");
            }

            // Création du répertoire d'upload si inexistant
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

                    // Ignorer les fichiers non Excel
                    if (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls")) {
                        continue;
                    }

                    // Sauvegarde du fichier
                    String filePath = uploadPath + File.separator + System.currentTimeMillis() + "_" + fileName;
                    filePart.write(filePath);

                    JSONObject fileResult = new JSONObject();
                    fileResult.put("fileName", fileName);
                    fileResult.put("filePath", filePath);

                    // Chemins pour les fichiers JSON de sortie
                    String drillingJsonPath = uploadPath + File.separator + System.currentTimeMillis() + "_drilling_result.json";
                    String excelJsonPath = uploadPath + File.separator + System.currentTimeMillis() + "_cost_result.json";

                    // Variables pour les résultats des parsers
                    JSONObject drillingResult = null;
                    JSONObject excelResult = null;
                    boolean drillingSuccess = false;
                    boolean costSuccess = false;
                    String drillingError = null;
                    String costError = null;

                    // Parser pour les données de forage
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

                    // Parser pour les coûts journaliers
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

                    // Remplissage des résultats
                    fileResult.put("drillingData", drillingResult);
                    fileResult.put("dailyCostData", excelResult);
                    fileResult.put("drillingJsonPath", drillingJsonPath);
                    fileResult.put("excelJsonPath", excelJsonPath);

                    // Statuts des parsers
                    fileResult.put("drillingSuccess", drillingSuccess);
                    fileResult.put("costSuccess", costSuccess);

                    // Messages d'erreur
                    if (drillingError != null) {
                        fileResult.put("drillingError", drillingError);
                    }
                    if (costError != null) {
                        fileResult.put("costError", costError);
                    }

                    // Succès global (au moins un parser a réussi)
                    boolean overallSuccess = drillingSuccess || costSuccess;
                    fileResult.put("success", overallSuccess);

                    // Détermination du type de résultat
                    if (overallSuccess) {
                        if (drillingSuccess && costSuccess) {
                            fileResult.put("type", "combined");
                            System.out.println("[DEBUG] Both drilling and daily cost parsing completed for: " + fileName);
                        } else if (drillingSuccess) {
                            fileResult.put("type", "drilling_only");
                            System.out.println("[DEBUG] Only drilling parsing completed for: " + fileName);
                        } else {
                            fileResult.put("type", "cost_only");
                            System.out.println("[DEBUG] Only daily cost parsing completed for: " + fileName);
                        }
                    } else {
                        fileResult.put("type", "failed");
                        System.out.println("[DEBUG] Both parsers failed for: " + fileName);
                    }

                    processedFiles.put(fileResult);
                }
            }

            // Vérification des résultats globaux
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

            // Construction de la réponse finale
            finalResult.put("success", hasSuccessfulFiles);
            if (hasSuccessfulFiles && hasErrors) {
                finalResult.put("message", "Files processed with some errors - check individual file results");
            } else if (hasSuccessfulFiles) {
                finalResult.put("message", "All files processed successfully");
            } else {
                finalResult.put("message", "All files failed to process");
            }
            finalResult.put("files", processedFiles);

            // Envoi de la réponse
            PrintWriter out = response.getWriter();
            out.print(finalResult.toString());
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            sendJsonError(response, "Error processing files: " + e.getMessage());
        }
    }

    /**
     * Envoie une erreur au format JSON
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
     * Gère l'upload de fichiers Excel depuis un formulaire standard (JSP)
     */
    private void handleExcelUpload(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Vérification du type de contenu
            if (!request.getContentType().startsWith("multipart/form-data")) {
                throw new ServletException("Form must be multipart/form-data");
            }

            // Récupération du fichier
            Part filePart = request.getPart("excelFile");
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

            // Validation du fichier
            if (fileName == null || fileName.isEmpty()) {
                throw new ServletException("No file selected");
            }

            if (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls")) {
                throw new ServletException("Only .xlsx and .xls files are allowed");
            }

            // Création du répertoire d'upload
            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Sauvegarde du fichier
            String filePath = uploadPath + File.separator + fileName;
            filePart.write(filePath);

            // Chemins pour les fichiers JSON de sortie
            String jsonOutputPath = uploadPath + File.separator + System.currentTimeMillis() + "_result.json";
            String drillingJsonPath = jsonOutputPath.replace(".json", "_drilling.json");
            String excelJsonPath = jsonOutputPath.replace(".json", "_couts.json");

            // Appel des parsers
            JSONObject drillingResult = drillingReportParserEJB.importDrillingReport(filePath);
            JSONObject excelResult = ExcelDailyCostParser.extractDailyCostData(filePath, excelJsonPath);

            // Configuration des attributs de requête
            request.setAttribute("drillingResult", drillingResult);
            request.setAttribute("excelResult", excelResult);
            request.setAttribute("drillingJsonPath", drillingJsonPath);
            request.setAttribute("excelJsonPath", excelJsonPath);
            request.setAttribute("originalFileName", fileName);
            request.setAttribute("jsonString", drillingResult.toString());
            request.setAttribute("excelData", excelResult);

            // Redirection vers la page de résultats
            request.getRequestDispatcher("/combined-result.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Error processing Excel file: " + e.getMessage(), e);
        }
    }

    /**
     * Gère le téléchargement d'un fichier JSON
     */
    private void handleDownloadJson(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Récupération du chemin du fichier
            String filePath = request.getParameter("filePath");
            if (filePath == null || filePath.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing filePath parameter");
                return;
            }

            // Vérification de l'existence du fichier
            File file = new File(filePath);
            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "JSON file not found");
                return;
            }

            // Configuration de la réponse
            response.setContentType("application/json");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"export_" + System.currentTimeMillis() + ".json\"");

            // Envoi du fichier
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

    /**
     * Récupère les rapports avec filtres optionnels
     */
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
            // Récupération de tous les fichiers de forage
            List<FICHIER_DRILLING> fichiers = fichierDrillingDAO.findAll();
            JSONArray jsonArray = new JSONArray();

            // Traitement de chaque fichier
            for (FICHIER_DRILLING fichier : fichiers) {
                // Filtrage par date d'upload
                if (dateParam != null && !dateParam.isEmpty()) {
                    String dateUploadStr = fichier.getDateUpload().toString();
                    if (!dateUploadStr.equals(dateParam)) {
                        continue;
                    }
                }

                // Préparation de l'objet JSON résultat
                JSONObject obj = new JSONObject();
                obj.put("id", fichier.getId());
                obj.put("filename", fichier.getNomFichier());
                obj.put("dateUpload", fichier.getDateUpload().toString());

                String jsonData = fichier.getJsonData();
                boolean skip = false; // indicateur pour ignorer ce fichier

                if (jsonData != null && !jsonData.isEmpty()) {
                    try {
                        JSONObject jsonParsed = new JSONObject(jsonData);

                        // Extraction des informations depuis l'en-tête
                        String dateRapport = "";
                        String nomPuits = "";
                        String profondeurStr = "";

                        if (jsonParsed.has("header")) {
                            JSONObject header = jsonParsed.getJSONObject("header");

                            // Date du rapport
                            dateRapport = header.optString("report_date", "N/A");

                            // Nom du puits
                            nomPuits = header.optString("well_name", "");

                            // Profondeur
                            profondeurStr = header.optString("depth_24h_ft", "");
                        }

                        // Filtres : profondeur et puits
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

                        // Filtre : opération
                        String derniereOperation = "";
                        if (operationParam != null && !operationParam.isEmpty()) {
                            boolean operationFound = false;

                            if (jsonParsed.has("operations") && jsonParsed.getJSONObject("operations").has("operations")) {
                                JSONArray operations = jsonParsed.getJSONObject("operations").getJSONArray("operations");

                                // Recherche de la dernière opération non vide
                                for (int i = operations.length() - 1; i >= 0; i--) {
                                    JSONObject operation = operations.getJSONObject(i);
                                    String code = operation.optString("code", "").trim();
                                    String description = operation.optString("description", "").trim();

                                    if (!code.isEmpty() || !description.isEmpty()) {
                                        derniereOperation = code + (description.isEmpty() ? "" : " - " + description);

                                        // Vérification du filtre
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
                                skip = true; // pas d'opérations
                            }
                        } else {
                            // Récupération de la dernière opération pour affichage
                            if (jsonParsed.has("operations") && jsonParsed.getJSONObject("operations").has("operations")) {
                                JSONArray operations = jsonParsed.getJSONObject("operations").getJSONArray("operations");

                                // Recherche de la dernière opération non vide
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

                        // Ignorer ce fichier si nécessaire
                        if (skip) continue;

                        // Ajout des informations extraites
                        obj.put("dateRapport", dateRapport);
                        obj.put("profondeurTVD", profondeurStr);
                        obj.put("puits", nomPuits);
                        obj.put("derniereOperation", derniereOperation);

                        // Ajout d'informations supplémentaires sur la boue
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

            // Envoi de la réponse
            response.getWriter().write(jsonArray.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            response.getWriter().write(error.toString());
        }
    }




    private void handleGetFullReportDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String reportId = request.getParameter("id");
            if (reportId == null || reportId.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing report ID parameter");
                return;
            }

            Long id = Long.parseLong(reportId);
            FICHIER_DRILLING fichier = fichierDrillingDAO.findById(id);
            if (fichier == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Report not found");
                return;
            }

            // Créer l'objet JSON de réponse
            JSONObject result = new JSONObject();
            result.put("id", fichier.getId());
            result.put("filename", fichier.getNomFichier());
            result.put("dateUpload", fichier.getDateUpload().toString());

            // Parse le JSON data s'il existe
            String jsonData = fichier.getJsonData();
            if (jsonData != null && !jsonData.isEmpty()) {
                try {
                    JSONObject jsonParsed = new JSONObject(jsonData);

                    // Extraire toutes les sections du rapport
                    if (jsonParsed.has("header")) {
                        JSONObject header = jsonParsed.getJSONObject("header");
                        JSONObject cleanHeader = new JSONObject();

                        // Nettoyer les valeurs null
                        Iterator<String> keys = header.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (!header.isNull(key)) {
                                cleanHeader.put(key, header.get(key));
                            }
                        }
                        result.put("header", cleanHeader);
                    }

                    // Operations
                    if (jsonParsed.has("operations")) {
                        JSONObject operations = jsonParsed.getJSONObject("operations");
                        result.put("operations", operations);
                    }

                    // Mud information
                    if (jsonParsed.has("mud_information")) {
                        JSONObject mudInfo = jsonParsed.getJSONObject("mud_information");
                        result.put("mud_information", mudInfo);
                    }

                    // Lithology
                    if (jsonParsed.has("lithology")) {
                        JSONArray lithology = jsonParsed.getJSONArray("lithology");
                        result.put("lithology", lithology);
                    }

                    // Global info
                    if (jsonParsed.has("global_info")) {
                        JSONObject globalInfo = jsonParsed.getJSONObject("global_info");
                        result.put("global_info", globalInfo);
                    }

                    // Parameters
                    if (jsonParsed.has("parameters")) {
                        JSONObject parameters = jsonParsed.getJSONObject("parameters");
                        result.put("parameters", parameters);
                    }

                    // Mud products
                    if (jsonParsed.has("mud_products")) {
                        JSONObject mudProducts = jsonParsed.getJSONObject("mud_products");
                        result.put("mud_products", mudProducts);
                    }

                    // BHA components
                    if (jsonParsed.has("bha_components")) {
                        JSONObject bhaComponents = jsonParsed.getJSONObject("bha_components");
                        result.put("bha_components", bhaComponents);
                    }

                    // Remarks
                    if (jsonParsed.has("remarks")) {
                        JSONObject remarks = jsonParsed.getJSONObject("remarks");
                        result.put("remarks", remarks);
                    }

                } catch (Exception e) {
                    result.put("error_parsing_jsonData", e.getMessage());
                }
            }

            response.getWriter().write(result.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            response.getWriter().write(error.toString());
        }
    }
}
