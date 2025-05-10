package com.example.servlets;

import com.example.dao.JournalDelaiDAO;

import com.example.entities.JournalDelai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;



@WebServlet("/importJournalDelai")
public class ImportJournalDelaiServlet extends HttpServlet {

    @Inject
    private JournalDelaiDAO journalDelaiDAO;


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            // 1. Chargement du fichier JSON
            InputStream is = getServletContext().getResourceAsStream("/WEB-INF/data/drilling_report_data.json");
            if (is == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("error", "Fichier JSON introuvable");
                out.print(jsonResponse.toString());
                return;
            }

            // 2. Parsing du JSON
            JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(is, "UTF-8")).getAsJsonObject();
            JsonObject header = jsonObject.getAsJsonObject("header");
            JsonObject remarks = jsonObject.getAsJsonObject("remarks");

            // 3. Création de l'entité JournalDelai
            JournalDelai journalDelai = new JournalDelai();

            if (header != null) {
                // Profondeur
                journalDelai.setProfondeur(header.get("depth_24h_ft").getAsString());

                // Progress
                journalDelai.setProgress(header.get("progress_ft").getAsString());

                // Date (conversion depuis le format original)
                String dateStr = header.get("report_date").getAsString();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                Date parsedDate = sdf.parse(dateStr);
                journalDelai.setDateRapport(new java.sql.Date(parsedDate.getTime()));

                // Phase
                journalDelai.setPhase(header.get("last_casing").getAsString());
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
                            journalDelai.setDailyNpt(dailySplit[0].trim());
                        }

                        String cumulativePart = remark.substring(remark.indexOf("Cumulative NPT =") + 16);
                        String[] cumulativeSplit = cumulativePart.split("days");
                        if (cumulativeSplit.length > 0) {
                            journalDelai.setCumulativeNpt(cumulativeSplit[0].trim());
                        }
                    }
                }
            }





            // 5. Persistance
            journalDelaiDAO.create(journalDelai);

            // 6. Réponse JSON
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("id", journalDelai.getIdJournal());
            jsonResponse.addProperty("message", "JournalDelai créé avec succès");
            out.print(jsonResponse.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Erreur lors de la création: " + e.getMessage());
            out.print(jsonResponse.toString());
            e.printStackTrace();
        }
    }
}