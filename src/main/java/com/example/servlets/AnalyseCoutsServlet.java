package com.example.servlets;

import com.example.dao.CoutOprDAO;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/analyseCouts")
public class AnalyseCoutsServlet extends HttpServlet {

    @Inject
    private CoutOprDAO coutOprDAO;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        String action = request.getParameter("action");
        String nomPuit = request.getParameter("nomPuit");

        if (action == null || nomPuit == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("error", "Paramètres 'action' et 'nomPuit' requis.");
            out.print(jsonResponse.toString());
            return;
        }

        try {
            switch (action) {
                case "sommeParPhase":
                    List<Object[]> resultPhase = coutOprDAO.getSommeParPhase(nomPuit);
                    JsonArray arrayPhase = new JsonArray();
                    for (Object[] row : resultPhase) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("phase", (String) row[0]);
                        obj.addProperty("sommeReel", row[1] != null ? row[1].toString() : "0");
                        obj.addProperty("sommePrevu", row[2] != null ? row[2].toString() : "0");
                        arrayPhase.add(obj);
                    }
                    jsonResponse.add("data", arrayPhase);
                    break;

                case "sommeParSemaine":
                    List<Object[]> resultSemaine = coutOprDAO.getSommeCoutReelParSemaine(nomPuit);
                    JsonArray arraySemaine = new JsonArray();
                    for (Object[] row : resultSemaine) {
                        JsonObject obj = new JsonObject();
                        // semaine_relative est un Number (trunc renvoie un nombre)
                        obj.addProperty("semaineRelative", ((Number) row[0]).intValue());
                        obj.addProperty("sommeReel", row[1] != null ? row[1].toString() : "0");
                        arraySemaine.add(obj);
                    }
                    jsonResponse.add("data", arraySemaine);
                    break;


                case "sommeParMois":
                    List<Object[]> resultMois = coutOprDAO.getSommeParMois(nomPuit);
                    JsonArray arrayMois = new JsonArray();
                    for (Object[] row : resultMois) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("annee", ((Number) row[0]).intValue());
                       // si je garde en numero obj.addProperty("mois", ((Number) row[1]).intValue());

                        int moisNum = ((Number) row[1]).intValue();
                        String[] moisNoms = {
                                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
                        };
                        String moisNom = (moisNum >= 1 && moisNum <= 12) ? moisNoms[moisNum - 1] : "Inconnu";
                        obj.addProperty("mois", moisNom);

                        obj.addProperty("sommeReel", row[2] != null ? row[2].toString() : "0");
                        obj.addProperty("sommePrevu", row[3] != null ? row[3].toString() : "0");
                        arrayMois.add(obj);
                    }
                    jsonResponse.add("data", arrayMois);
                    break;


                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    jsonResponse.addProperty("error", "Action non reconnue.");
                    out.print(jsonResponse.toString());
                    return;
            }


            jsonResponse.addProperty("success", true);
            out.print(jsonResponse.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Erreur serveur : " + e.getMessage());
            out.print(jsonResponse.toString());
        }
    }
}
