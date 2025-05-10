package com.example.servlets;

import com.example.dao.PuitsDAO;
import com.example.entities.PUITS;
import com.example.entities.ZONE;

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

@WebServlet("/importPuits")
public class ImportPuitsServlet extends HttpServlet {

    @Inject
    private PuitsDAO puitsDAO;

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
            JsonObject header = jsonObject.getAsJsonObject("header");

            // 3. Création du puits
            PUITS puits = new PUITS();

            if (header != null) {
                puits.setNom_puit(header.get("well_name").getAsString());

                // Date (conversion depuis le format original)
                String dateStr = header.get("report_date").getAsString();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                Date parsedDate = sdf.parse(dateStr);
                puits.setDate(new java.sql.Date(parsedDate.getTime()));

                puits.setCode(null);
                puits.setStatut_cout(null);
                puits.setStatut_delai(null);
                puits.setDate_fin_prevu(null);
                puits.setDate_fin_reelle(null);
            }

            // 4. Associer le puits à une zone de manière séquentielle
            try {
                ZONE zone = puitsDAO.findZoneForNewPuit();
                puits.setZone(zone);

                // 5. Sauvegarde
                puitsDAO.save(puits);

                // 6. Réponse JSON
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("id", puits.getId_puit());
                jsonResponse.addProperty("zone_id", zone.getIdZone());
                jsonResponse.addProperty("message",
                        "Puits créé avec succès et associé à la zone " + zone.getIdZone());
                out.print(jsonResponse.toString());

            } catch (IllegalStateException e) {
                // Cas spécial : aucune zone disponible
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("error", e.getMessage());
                out.print(jsonResponse.toString());

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("error", "Erreur lors de la création du puits : " + e.getMessage());
                out.print(jsonResponse.toString());
                e.printStackTrace();
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Erreur inattendue : " + e.getMessage());
            out.print(jsonResponse.toString());
            e.printStackTrace();
        }
    }
}
