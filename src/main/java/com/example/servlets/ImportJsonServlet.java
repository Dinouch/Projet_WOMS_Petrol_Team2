package com.example.servlets;

import com.example.dao.FormulaireDAO;
import com.example.entities.FORMULAIRE;

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

@WebServlet("/importJson")
public class ImportJsonServlet extends HttpServlet {

    @Inject
    private FormulaireDAO formulaireDAO;

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

            // 3. Création de l'entité
            FORMULAIRE formulaire = new FORMULAIRE();

            if (header != null) {
                // Titre
                formulaire.setTitreRapport(header.get("report_number").getAsString());

                // Date (conversion depuis le format original)
                String dateStr = header.get("report_date").getAsString();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                Date parsedDate = sdf.parse(dateStr);
                formulaire.setDate(new java.sql.Date(parsedDate.getTime()));
            }

            // 4. Persistance avec gestion de transaction
            formulaireDAO.create(formulaire);

            // 5. Réponse JSON
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("id", formulaire.getIdFormulaire());
            jsonResponse.addProperty("message", "Formulaire créé avec succès");
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