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
import java.util.List;
import java.util.Random;

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
        Random random = new Random();

        try {
            // Mode génération aléatoire uniquement
            if ("random".equals(request.getParameter("mode"))) {
                int count = 50; // Valeur par défaut
                try {
                    count = Integer.parseInt(request.getParameter("count"));
                } catch (Exception e) {
                    // Conserver la valeur par défaut
                }

                // Génération des données aléatoires
                delaiOprDAO.generateTestData(count);

                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("created_count", count);
                jsonResponse.addProperty("message", count + " enregistrements aléatoires générés avec succès");
                out.print(jsonResponse.toString());
                return;
            }

            // Mode traitement normal du fichier JSON
            InputStream is = getServletContext().getResourceAsStream("/WEB-INF/data/drilling_report_data.json");
            if (is == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("error", "Fichier JSON introuvable");
                out.print(jsonResponse.toString());
                return;
            }

            // Parsing du fichier
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

            // Traitement de chaque opération
            int createdCount = 0;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

            // Dans la méthode doPost, partie traitement des opérations
            for (int i = 0; i < operations.size(); i++) {
                JsonObject operation = operations.get(i).getAsJsonObject();

                DelaiOpr delaiOpr = new DelaiOpr();

                // Génération ALÉATOIRE OBLIGATOIRE (même si description vide)
                Random rand = new Random();
                delaiOpr.setDureePr(String.format("%dh%02d",
                        1 + rand.nextInt(5),  // 1-5 heures
                        rand.nextInt(60)));   // 0-59 minutes

                // Si opération valide
                if (!operation.get("description").getAsString().isEmpty()) {
                    delaiOpr.setDespOpr(operation.get("description").getAsString());

                    // Traitement des heures si disponibles
                    try {
                        if (!operation.get("start_time").getAsString().isEmpty()) {
                            delaiOpr.setStartTime(new Time(timeFormat.parse(
                                    operation.get("start_time").getAsString()).getTime()));
                        }
                        if (!operation.get("end_time").getAsString().isEmpty()) {
                            delaiOpr.setEndTime(new Time(timeFormat.parse(
                                    operation.get("end_time").getAsString()).getTime()));
                        }
                    } catch (ParseException e) {
                        System.err.println("Erreur parsing time: " + e.getMessage());
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
                // Sauvegarde SYSTEMATIQUE
                delaiOprDAO.create(delaiOpr);
                createdCount++;
            }

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Permet de générer des données aléatoires via une requête GET
        doPost(request, response);
    }
}

