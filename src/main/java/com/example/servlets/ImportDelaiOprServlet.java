package com.example.servlets;

import com.example.dao.DelaiOprDAO;
import com.example.entities.DelaiOpr;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/importDelaiOpr")
public class ImportDelaiOprServlet extends HttpServlet {

    @Inject
    private DelaiOprDAO delaiOprDAO;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            // 1. Chargement du JSON
            InputStream is = getServletContext().getResourceAsStream("/WEB-INF/data/drilling_report_data.json");
            if (is == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("error", "Fichier JSON introuvable");
                out.print(jsonResponse.toString());
                return;
            }

            // 2. Parsing du fichier
            JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(is, "UTF-8")).getAsJsonObject();
            JsonArray operations = jsonObject.getAsJsonArray("operations");
            JsonObject header = jsonObject.getAsJsonObject("header");
            JsonObject remarks = jsonObject.getAsJsonObject("remarks");




            if (operations == null || operations.size() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("error", "Aucune opération trouvée dans le fichier JSON");
                out.print(jsonResponse.toString());
                return;
            }

            // 3. Traitement de chaque opération
            int createdCount = 0;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

            for (int i = 0; i < operations.size(); i++) {
                JsonObject operation = operations.get(i).getAsJsonObject();

                // Vérifier si l'opération a des données valides
                if (operation.get("description").getAsString().isEmpty() &&
                        operation.get("start_time").getAsString().isEmpty() &&
                        operation.get("end_time").getAsString().isEmpty()) {
                    continue;
                }

                // 4. Création de l'entité DelaiOpr
                DelaiOpr delaiOpr = new DelaiOpr();
                delaiOpr.setDespOpr(operation.get("description").getAsString());

                if (header != null) {




                    delaiOpr.setNom_puit(header.get("well_name").getAsString());

                    // Profondeur
                    delaiOpr.setProfondeur(header.get("depth_24h_ft").getAsString());

                    // Progress
                    delaiOpr.setProgress(header.get("progress_ft").getAsString());

                    delaiOpr.setPhase(header.get("last_casing").getAsString());

                    // Date (conversion depuis le format original)
                    String dateStr = header.get("report_date").getAsString();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    Date parsedDate = sdf.parse(dateStr);
                    delaiOpr.setDate(new java.sql.Date(parsedDate.getTime()));
                }

                // Extraction des NPT depuis les remarques
                if (remarks != null && remarks.has("remarks")) {
                    for (com.google.gson.JsonElement element : remarks.getAsJsonArray("remarks")) {
                        String remark = element.getAsString();
                        if (remark.contains("Daily NPT")) {
                            // Exemple de format: "Daily NPT = 22 hrs / Cumulative NPT = 0.24 days"
                            String dailyPart = remark.substring(remark.indexOf("Daily NPT =") + 11);
                            String[] dailySplit = dailyPart.split("hrs");
                            if (dailySplit.length > 0) {
                                delaiOpr.setDailyNpt(dailySplit[0].trim());
                            }

                            String cumulativePart = remark.substring(remark.indexOf("Cumulative NPT =") + 16);
                            String[] cumulativeSplit = cumulativePart.split("days");
                            if (cumulativeSplit.length > 0) {
                                delaiOpr.setCumulativeNpt(cumulativeSplit[0].trim());
                            }
                        }
                    }
                }



                try {
                    if (!operation.get("start_time").getAsString().isEmpty()) {
                        delaiOpr.setStartTime(new Time(timeFormat.parse(operation.get("start_time").getAsString()).getTime()));
                    }
                    if (!operation.get("end_time").getAsString().isEmpty()) {
                        delaiOpr.setEndTime(new Time(timeFormat.parse(operation.get("end_time").getAsString()).getTime()));
                    }
                } catch (ParseException e) {
                    // Ignorer les erreurs de parsing de temps
                    continue;
                }

                delaiOpr.setStatutDelai(null); // Valeur null comme spécifié
                // Les autres champs sont laissés à null comme spécifié
                delaiOpr.setDureepr(null);

                // 5. Sauvegarde
                delaiOprDAO.create(delaiOpr);
                createdCount++;
            }

            // 6. Réponse JSON
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("created_count", createdCount);
            jsonResponse.addProperty("message", "Délais d'opérations créés avec succès");
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
