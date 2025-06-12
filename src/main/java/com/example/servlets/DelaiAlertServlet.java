package com.example.servlets;

import com.example.dao.DelaiOprDAO;
import com.example.entities.DelaiOpr;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/delaiAlerts")
public class DelaiAlertServlet extends HttpServlet {

    @Inject
    private DelaiOprDAO delaiOprDAO;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Récupérer les délais en dépassement
        List<DelaiOpr> overrunDelays = delaiOprDAO.findOverruns();

        // Récupérer les délais à surveiller
        List<DelaiOpr> monitorDelays = delaiOprDAO.findToMonitor();

        // Calculer les statistiques
        int totalOverruns = overrunDelays.size();
        int totalToMonitor = monitorDelays.size();

        // Construire l'objet JSON


        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        jsonResponse.add("overrunDelays", gson.toJsonTree(overrunDelays));
        jsonResponse.add("monitorDelays", gson.toJsonTree(monitorDelays));
        jsonResponse.addProperty("totalOverruns", totalOverruns);
        jsonResponse.addProperty("totalToMonitor", totalToMonitor);

        // Configurer la réponse HTTP
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        for (DelaiOpr d : monitorDelays) {
            System.out.println("id: " + d.getIdDelaiOpr() + ", dureepr: " + d.getDureepr());
        }

        // Envoyer la réponse JSON
        response.getWriter().write(gson.toJson(jsonResponse));




    }
}
