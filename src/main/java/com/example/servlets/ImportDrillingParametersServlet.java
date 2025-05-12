package com.example.servlets;
import com.example.dao.DrillingParametersDAO;
import com.example.entities.DRILLING_PARAMETERS;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/importDrillingParameters")
public class ImportDrillingParametersServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ImportDrillingParametersServlet.class.getName());

    @Inject
    private DrillingParametersDAO drillingParametersDAO;

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
            JsonObject drillingParams = jsonObject.getAsJsonObject("drilling_parameters");

            if (drillingParams == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("error", "Section 'drilling_parameters' non trouvée dans le JSON");
                out.print(jsonResponse.toString());
                return;
            }

            // 3. Créer l'objet DRILLING_PARAMETERS
            DRILLING_PARAMETERS parameters = new DRILLING_PARAMETERS();

            // Extraction des données du JSON
            if (drillingParams.has("bit_number")) {
                parameters.setBitNumber(drillingParams.get("bit_number").getAsInt());
            }

            if (drillingParams.has("bit_size")) {
                parameters.setBitSize(drillingParams.get("bit_size").getAsString());
            }

            if (drillingParams.has("wob_min_t")) {
                parameters.setWobMin(drillingParams.get("wob_min_t").getAsDouble());
            }

            if (drillingParams.has("wob_max_t")) {
                parameters.setWobMax(drillingParams.get("wob_max_t").getAsDouble());
            }

            if (drillingParams.has("rpm_min")) {
                parameters.setRpmMin(drillingParams.get("rpm_min").getAsInt());
            }

            if (drillingParams.has("rpm_max")) {
                parameters.setRpmMax(drillingParams.get("rpm_max").getAsInt());
            }

            if (drillingParams.has("flow_gpm")) {
                parameters.setFlowRate(drillingParams.get("flow_gpm").getAsInt());
            }

            if (drillingParams.has("pressure_psi")) {
                parameters.setPressure(drillingParams.get("pressure_psi").getAsInt());
            }

            if (drillingParams.has("hsi_hp_sqin")) {
                parameters.setHsi(drillingParams.get("hsi_hp_sqin").getAsDouble());
            }

            // Utiliser la profondeur depuis le header si disponible
            if (jsonObject.has("header") && jsonObject.getAsJsonObject("header").has("depth_24h_ft")) {
                double depthFt = jsonObject.getAsJsonObject("header").get("depth_24h_ft").getAsDouble();
                // Conversion de pieds à mètres (1 pied = 0.3048 mètres)
                double depthMeters = depthFt * 0.3048;
                parameters.setDepth(depthMeters);
            }

            // 4. Sauvegarder dans la base de données
            try {
                drillingParametersDAO.save(parameters);

                // 5. Construction de la réponse
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("id", parameters.getId());
                jsonResponse.addProperty("message", "Paramètres de forage importés avec succès");
                out.print(jsonResponse.toString());

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erreur lors de la sauvegarde des paramètres de forage", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("error", "Erreur lors de la sauvegarde: " + e.getMessage());
                out.print(jsonResponse.toString());
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors du traitement du JSON", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Erreur inattendue: " + e.getMessage());
            out.print(jsonResponse.toString());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Rediriger vers la page d'importation ou afficher un formulaire
        request.getRequestDispatcher("/importDrillingParameters.jsp").forward(request, response);
    }
}
