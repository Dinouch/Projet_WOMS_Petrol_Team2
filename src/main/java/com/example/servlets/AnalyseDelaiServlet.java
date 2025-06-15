package com.example.servlets;

import com.example.dao.DelaiOprDAO;
import com.google.gson.JsonObject;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/analyseDelais")
public class AnalyseDelaiServlet extends HttpServlet {

    @Inject
    private DelaiOprDAO delaiOprDAO;

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setCorsHeaders(response);
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
            if ("statistiquesGlobales".equals(action)) {
                Map<String, Object> stats = delaiOprDAO.getStatistiquesGlobalesDelai(nomPuit);

                jsonResponse.addProperty("statutGlobalDelai", (String) stats.get("statutGlobalDelai"));
                jsonResponse.addProperty("nbrJourX", (Integer) stats.get("nbrJourX"));
                jsonResponse.addProperty("totalJour", (Integer) stats.get("totalJour"));
                jsonResponse.addProperty("totalPrevuReste", (Integer) stats.get("totalPrevuReste"));
                jsonResponse.addProperty("totalNonPrevu", (Integer) stats.get("totalNonPrevu"));

                jsonResponse.addProperty("success", true);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("error", "Action non reconnue.");
            }

            out.print(jsonResponse.toString());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Erreur serveur : " + e.getMessage());
            out.print(jsonResponse.toString());
        }
    }
}