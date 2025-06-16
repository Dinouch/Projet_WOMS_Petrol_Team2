package com.example.servlets;

import com.example.dao.DelaiOprDAO;
import com.example.entities.DelaiOpr;
import com.google.gson.*;
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
            // Mode génération aléatoire
            if ("random".equals(request.getParameter("mode"))) {
                int count = 50;
                try {
                    count = Integer.parseInt(request.getParameter("count"));
                } catch (Exception e) {
                    // Valeur par défaut conservée
                }

                delaiOprDAO.generateTestData(count);

                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("created_count", count);
                out.print(jsonResponse.toString());
                return;
            }

            // Mode traitement du fichier JSON
            InputStream is = getServletContext().getResourceAsStream("/WEB-INF/data/drilling_report_data.json");
            if (is == null) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("error", "Fichier introuvable");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(jsonResponse.toString());
                return;
            }

            // Parsing sécurisé du JSON
            JsonElement rootElement = JsonParser.parseReader(new InputStreamReader(is, "UTF-8"));

            if (!rootElement.isJsonObject()) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("error", "Format JSON invalide - objet attendu");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(jsonResponse.toString());
                return;
            }

            JsonObject rootObject = rootElement.getAsJsonObject();
            JsonElement operationsElement = rootObject.get("operations");

            // Adaptation pour la structure spéciale (double niveau)
            if (operationsElement != null && operationsElement.isJsonObject()) {
                JsonObject operationsWrapper = operationsElement.getAsJsonObject();
                operationsElement = operationsWrapper.get("operations");
            }

            if (operationsElement == null || !operationsElement.isJsonArray()) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("error", "Le champ 'operations' est absent ou n'est pas un tableau");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(jsonResponse.toString());
                return;
            }

            JsonArray operations = operationsElement.getAsJsonArray();
            if (operations.size() == 0) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("error", "Aucune opération à traiter");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(jsonResponse.toString());
                return;
            }

            // Traitement des opérations
            int createdCount = 0;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

            for (JsonElement element : operations) {
                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject operation = element.getAsJsonObject();
                DelaiOpr delaiOpr = new DelaiOpr();

                // Traitement des champs avec vérifications
                if (operation.has("description") && !operation.get("description").isJsonNull()) {
                    delaiOpr.setDespOpr(operation.get("description").getAsString());
                }

                // Durée aléatoire si nécessaire
                delaiOpr.setDureePr(String.format("%dh%02d",
                        1 + random.nextInt(5), random.nextInt(60)));

                // Traitement des heures
                try {
                    if (operation.has("start_time") && !operation.get("start_time").isJsonNull()) {
                        String startTime = operation.get("start_time").getAsString();
                        if (!startTime.isEmpty()) {
                            Date startDate = timeFormat.parse(startTime);
                            delaiOpr.setStartTime(new Time(startDate.getTime()));
                        }
                    }

                    if (operation.has("end_time") && !operation.get("end_time").isJsonNull()) {
                        String endTime = operation.get("end_time").getAsString();
                        if (!endTime.isEmpty()) {
                            Date endDate = timeFormat.parse(endTime);
                            delaiOpr.setEndTime(new Time(endDate.getTime()));
                        }
                    }
                } catch (ParseException e) {
                    System.err.println("Erreur de parsing du temps: " + e.getMessage());
                    continue;
                }

                delaiOpr.setStatutDelai(null);

                try {
                    delaiOprDAO.create(delaiOpr);
                    createdCount++;
                } catch (Exception e) {
                    System.err.println("Erreur lors de la création: " + e.getMessage());
                }
            }

            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("created_count", createdCount);
            out.print(jsonResponse.toString());

        } catch (JsonSyntaxException e) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Erreur de syntaxe JSON: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(jsonResponse.toString());
        } catch (Exception e) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Erreur inattendue: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(jsonResponse.toString());
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}