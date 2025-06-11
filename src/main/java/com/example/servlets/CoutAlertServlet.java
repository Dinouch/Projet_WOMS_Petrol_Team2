package com.example.servlets;

import com.example.dao.CoutOprDAO;
import com.example.entities.CoutOpr;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/coutAlerts")
public class CoutAlertServlet extends HttpServlet {

    @Inject
    private CoutOprDAO coutOprDAO;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Récupérer les coûts en dépassement
        List<CoutOpr> overrunCosts = coutOprDAO.findOverruns();

        // Récupérer les coûts à surveiller
        List<CoutOpr> monitorCosts = coutOprDAO.findToMonitor();

        // Calculer les statistiques
        int totalOverruns = overrunCosts.size();
        int totalToMonitor = monitorCosts.size();

        // Construire l'objet JSON
        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        jsonResponse.add("overrunCosts", gson.toJsonTree(overrunCosts));
        jsonResponse.add("monitorCosts", gson.toJsonTree(monitorCosts));
        jsonResponse.addProperty("totalOverruns", totalOverruns);
        jsonResponse.addProperty("totalToMonitor", totalToMonitor);

        // Configurer la réponse HTTP
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Envoyer la réponse JSON
        response.getWriter().write(gson.toJson(jsonResponse));
    }
}
