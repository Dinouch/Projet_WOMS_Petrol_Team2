package com.example.servlets;

import com.example.dao.CoutOprDAO;
import com.example.entities.CoutOpr;
import com.google.gson.JsonObject;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet("/importCoutOpr")
public class ImportCoutOprServlet extends HttpServlet {

    @Inject
    private CoutOprDAO coutOprDAO;
    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    @Override

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        String nomPuit = request.getParameter("nomPuit");
        if (nomPuit == null || nomPuit.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("error", "Le nom du puits est requis.");
            out.print(jsonResponse.toString());
            return;
        }

        try {
            List<Object[]> resultats = coutOprDAO.getSommeCoutsParJourPourPuit(nomPuit);

            JsonArray dataArray = new JsonArray();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            for (Object[] row : resultats) {
                Date date = (Date) row[0];
                BigDecimal sommeReel = (BigDecimal) row[1];
                BigDecimal sommePrevu = (BigDecimal) row[2];
                String phase = (String) row[3];

                String statutCout;
                if (sommeReel.compareTo(sommePrevu) > 0) {
                    statutCout = "Dépassement";
                } else if (sommeReel.compareTo(sommePrevu) == 0) {
                    statutCout = "À surveiller";
                } else {
                    statutCout = "Sous contrôle";
                }

                JsonObject entry = new JsonObject();
                entry.addProperty("date", sdf.format(date));
                entry.addProperty("sommeReel", sommeReel.toString());
                entry.addProperty("sommePrevu", sommePrevu.toString());
                entry.addProperty("phase", phase);
                entry.addProperty("statutCout", statutCout);

                dataArray.add(entry);
            }


            jsonResponse.add("data", dataArray);
            jsonResponse.addProperty("success", true);
            out.print(jsonResponse.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Erreur lors de la récupération des sommes : " + e.getMessage());
            out.print(jsonResponse.toString());
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Garder ici le code existant de import des JSON (non modifié)
        // Je te le remets juste pour que le code soit complet :
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            InputStream costStream = getServletContext().getResourceAsStream("/WEB-INF/data/daily_cost_data.json");
            if (costStream == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("error", "Fichier JSON des coûts introuvable");
                out.print(jsonResponse.toString());
                return;
            }

            InputStream reportStream = getServletContext().getResourceAsStream("/WEB-INF/data/drilling_report_data.json");
            if (reportStream == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("error", "Fichier JSON du rapport de forage introuvable");
                out.print(jsonResponse.toString());
                return;
            }

            com.google.gson.JsonObject costObject = com.google.gson.JsonParser.parseReader(new InputStreamReader(costStream, "UTF-8")).getAsJsonObject();
            com.google.gson.JsonObject reportObject = com.google.gson.JsonParser.parseReader(new InputStreamReader(reportStream, "UTF-8")).getAsJsonObject();

            com.google.gson.JsonObject dailyCost = costObject.getAsJsonObject("daily_cost");
            com.google.gson.JsonObject header = reportObject.getAsJsonObject("header");

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

            String wellName = header.has("well_name") ? header.get("well_name").getAsString() : null;
            String reportDateStr = header.has("report_date") ? header.get("report_date").getAsString() : null;
            String phase = header.has("last_casing") ? header.get("last_casing").getAsString() : null;

            java.util.Date reportDate = null;
            if (reportDateStr != null && !reportDateStr.isEmpty()) {
                try {
                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy");
                    reportDate = dateFormat.parse(reportDateStr);
                } catch (java.text.ParseException e) {
                    System.out.println("Erreur lors de la conversion de la date: " + e.getMessage());
                }
            }

            int createdCount = 0;

            for (java.util.Map.Entry<String, com.google.gson.JsonElement> entry : dailyCost.entrySet()) {
                String categoryName = entry.getKey();

                if ("daily_cost_total".equals(categoryName)) {
                    continue;
                }

                com.google.gson.JsonObject categoryData = entry.getValue().getAsJsonObject();

                CoutOpr coutOpr = new CoutOpr();
                coutOpr.setNomOpr(categoryName);

                coutOpr.setNom_puit(wellName);
                coutOpr.setDate(reportDate);
                coutOpr.setPhase(phase);

                if (categoryData.has("total")) {
                    com.google.gson.JsonElement totalElement = categoryData.get("total");
                    if (!totalElement.isJsonNull()) {
                        try {
                            if (totalElement.isJsonPrimitive() && totalElement.getAsJsonPrimitive().isNumber()) {
                                coutOpr.setCoutReel(new BigDecimal(totalElement.getAsDouble()));
                            } else if (totalElement.isJsonPrimitive() && !totalElement.getAsString().isEmpty()) {
                                coutOpr.setCoutReel(new BigDecimal(totalElement.getAsString()));
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Erreur de conversion pour le total de " + categoryName + ": " + e.getMessage());
                        }
                    }
                }

                coutOpr.setCoutPrevu(null);
                coutOpr.setStatutCout(null);

                coutOprDAO.create(coutOpr);
                createdCount++;
            }

            coutOprDAO.remplirCoutsPrevusDepuisMap();
            coutOprDAO.mettreAJourStatutCout();


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


