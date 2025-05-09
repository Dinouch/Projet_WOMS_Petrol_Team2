package com.example.servlets;

import com.example.dao.JournalQualiteDAO;
import com.example.entities.Journal_qualite;

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

@WebServlet("/importJournalQualite")
public class ImportJournalQualiteServlet extends HttpServlet {

    @Inject
    private JournalQualiteDAO journalQualiteDAO;

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
            JsonObject drillingParameters = jsonObject.getAsJsonObject("drilling_parameters");

            // 3. Création de l'entité Journal_qualite
            Journal_qualite journalQualite = new Journal_qualite();

            // Extraction des données selon les spécifications
            if (header != null && drillingParameters != null) {


                journalQualite.setNom_puit(header.get("well_name").getAsString());

                // Date (conversion depuis le format original MM/DD/YYYY)
                String dateStr = header.get("report_date").getAsString();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                Date parsedDate = sdf.parse(dateStr);
                journalQualite.setDate(new java.sql.Date(parsedDate.getTime()));

                // RPM_min: drilling_parameters.rpm_min
                journalQualite.setRpmMin(drillingParameters.get("rpm_min").getAsInt());

                // RPM_max: drilling_parameters.rpm_max
                journalQualite.setRpmMax(drillingParameters.get("rpm_max").getAsInt());

                // pressure: drilling_parameters.pressure_psi
                journalQualite.setPressure(drillingParameters.get("pressure_psi").getAsInt());

                // progress: drilling_parameters.progress_ft
                journalQualite.setProgress(drillingParameters.get("progress_ft").getAsInt());
            }

            // 4. Persistance avec gestion de transaction
            journalQualiteDAO.create(journalQualite);

            // 5. Réponse JSON
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("id", journalQualite.getId());
            jsonResponse.addProperty("message", "Journal de qualité créé avec succès");
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