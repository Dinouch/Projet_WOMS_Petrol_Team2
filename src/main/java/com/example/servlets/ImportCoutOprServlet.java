package com.example.servlets;

import com.example.dao.CoutOprDAO;
import com.example.entities.CoutOpr;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@WebServlet("/importCoutOpr")
public class ImportCoutOprServlet extends HttpServlet {

    @Inject
    private CoutOprDAO coutOprDAO;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            // 1. Chargement du JSON des coûts
            InputStream costStream = getServletContext().getResourceAsStream("/WEB-INF/data/daily_cost_data.json");
            if (costStream == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("error", "Fichier JSON des coûts introuvable");
                out.print(jsonResponse.toString());
                return;
            }

            // 2. Chargement du JSON du rapport de forage pour les informations additionnelles
            InputStream reportStream = getServletContext().getResourceAsStream("/WEB-INF/data/drilling_report_data.json");
            if (reportStream == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("error", "Fichier JSON du rapport de forage introuvable");
                out.print(jsonResponse.toString());
                return;
            }

            // 3. Parsing des fichiers JSON
            JsonObject costObject = JsonParser.parseReader(new InputStreamReader(costStream, "UTF-8")).getAsJsonObject();
            JsonObject reportObject = JsonParser.parseReader(new InputStreamReader(reportStream, "UTF-8")).getAsJsonObject();

            JsonObject dailyCost = costObject.getAsJsonObject("daily_cost");
            JsonObject header = reportObject.getAsJsonObject("header");

            if (dailyCost == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("error", "Données de coûts journaliers introuvables dans le fichier JSON");
                out.print(jsonResponse.toString());
                return;
            }

            if (header == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("error", "Données d'entête introuvables dans le fichier de rapport");
                out.print(jsonResponse.toString());
                return;
            }

            // 4. Extraction des informations additionnelles du rapport de forage
            String wellName = header.has("well_name") ? header.get("well_name").getAsString() : null;
            String reportDateStr = header.has("report_date") ? header.get("report_date").getAsString() : null;
            String phase = header.has("last_casing") ? header.get("last_casing").getAsString() : null;

            // Conversion de la date
            Date reportDate = null;
            if (reportDateStr != null && !reportDateStr.isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    reportDate = dateFormat.parse(reportDateStr);
                } catch (ParseException e) {
                    System.out.println("Erreur lors de la conversion de la date: " + e.getMessage());
                }
            }

            // 5. Traitement de chaque catégorie d'opération
            int createdCount = 0;

            for (Map.Entry<String, JsonElement> entry : dailyCost.entrySet()) {
                String categoryName = entry.getKey();

                // Ignorer la clé "daily_cost_total"
                if ("daily_cost_total".equals(categoryName)) {
                    continue;
                }

                // Le categoryName est directement le nom de l'opération
                System.out.println("Traitement de la catégorie: " + categoryName);

                JsonObject categoryData = entry.getValue().getAsJsonObject();

                // 6. Création de l'entité CoutOpr
                CoutOpr coutOpr = new CoutOpr();
                coutOpr.setNomOpr(categoryName);

                // Ajout des informations additionnelles
                coutOpr.setNom_puit(wellName);
                coutOpr.setDate(reportDate);
                coutOpr.setPhase(phase);

                // Récupérer le total de la catégorie
                if (categoryData.has("total")) {
                    JsonElement totalElement = categoryData.get("total");
                    if (!totalElement.isJsonNull()) {
                        try {
                            // Si c'est un nombre directement
                            if (totalElement.isJsonPrimitive() && totalElement.getAsJsonPrimitive().isNumber()) {
                                coutOpr.setCoutReel(new BigDecimal(totalElement.getAsDouble()));
                            }
                            // Si c'est une chaîne non vide
                            else if (totalElement.isJsonPrimitive() && !totalElement.getAsString().isEmpty()) {
                                coutOpr.setCoutReel(new BigDecimal(totalElement.getAsString()));
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Erreur de conversion pour le total de " + categoryName + ": " + e.getMessage());
                            // Ignorer si la conversion échoue
                        }
                    }
                }

                // Les autres champs sont laissés à null comme spécifié
                coutOpr.setCoutPrevu(null);
                coutOpr.setStatutCout(null);

                // 7. Sauvegarde
                coutOprDAO.create(coutOpr);
                createdCount++;
            }

            // 8. Réponse JSON
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("created_count", createdCount);
            jsonResponse.addProperty("message", "Coûts d'opérations créés avec succès. Les données du puits, la date et la phase ont été ajoutées.");
            out.print(jsonResponse.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Erreur inattendue : " + e.getMessage());
            out.print(jsonResponse.toString());
            e.printStackTrace();
        }
    }
}