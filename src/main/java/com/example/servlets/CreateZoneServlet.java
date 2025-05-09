package com.example.servlets;

import com.example.dao.ZoneDAO;
import com.example.entities.ZONE;
import com.google.gson.JsonObject;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/createZone")
public class CreateZoneServlet extends HttpServlet {

    @Inject
    private ZoneDAO zoneDAO;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            // Crée une zone avec toutes les valeurs null ou 0
            ZONE zone = new ZONE();
            zone.setX(0.0);
            zone.setY(0.0);
            zone.setElevation(0.0);



            // Persist la zone
            zoneDAO.create(zone);

            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("id_zone", zone.getIdZone());
            jsonResponse.addProperty("message", "Zone créée avec valeurs null/0");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Erreur: " + e.getMessage());
        }

        out.print(jsonResponse.toString());
    }
}
