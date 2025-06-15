package com.example.servlets;

import com.example.dao.CoutOprDAO;
import com.example.entities.CoutOpr;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
// Pop up cout

@WebServlet("/operationsByPuitAndDate")
public class OperationsByPuitAndDateServlet extends HttpServlet {

    @Inject
    private CoutOprDAO coutOprDAO;

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        // Récupération des paramètres
        String nomPuit = request.getParameter("nomPuit");
        String dateStr = request.getParameter("date");

        // Validation des paramètres
        if (nomPuit == null || nomPuit.isEmpty() || dateStr == null || dateStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("error", "Les paramètres nomPuit et date sont requis.");
            out.print(jsonResponse.toString());
            return;
        }

        try {
            // Conversion de la date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateStr);

            // Récupération des opérations depuis le DAO
            List<CoutOpr> operations = coutOprDAO.findByPuitAndDate(nomPuit, date);

            // Construction de la réponse JSON
            JsonArray operationsArray = new JsonArray();

            for (CoutOpr operation : operations) {
                JsonObject opJson = new JsonObject();
                opJson.addProperty("nomOperation", operation.getNomOpr());
                opJson.addProperty("phase", operation.getPhase());
                opJson.addProperty("coutReel", operation.getCoutReel() != null ? operation.getCoutReel().toString() : "null");
                opJson.addProperty("coutPrevu", operation.getCoutPrevu() != null ? operation.getCoutPrevu().toString() : "null");
                opJson.addProperty("statutCout", operation.getStatutCout() != null ? operation.getStatutCout() : "null");

                operationsArray.add(opJson);
            }

            jsonResponse.add("operations", operationsArray);
            jsonResponse.addProperty("success", true);
            out.print(jsonResponse.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Erreur lors de la récupération des opérations: " + e.getMessage());
            out.print(jsonResponse.toString());
            e.printStackTrace();
        }
    }
}