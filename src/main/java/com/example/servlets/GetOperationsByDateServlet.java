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

@WebServlet("/getOperationsByDate")
public class GetOperationsByDateServlet extends HttpServlet {

    @Inject
    private CoutOprDAO coutOprDAO;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        String dateStr = request.getParameter("date");
        if (dateStr == null || dateStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("error", "Paramètre 'date' requis (format dd/MM/yy)");
            out.print(jsonResponse.toString());
            return;
        }

        Date date;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
            sdf.setLenient(false);
            date = sdf.parse(dateStr);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.addProperty("error", "Format de date invalide. Utiliser dd/MM/yy, ex: 01/10/13");
            out.print(jsonResponse.toString());
            return;
        }

        try {
            List<CoutOpr> operations = coutOprDAO.getOperationsByDate(date);

            JsonArray jsonArray = new JsonArray();
            SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd");

            for (CoutOpr op : operations) {
                JsonObject obj = new JsonObject();
                obj.addProperty("nomOpr", op.getNomOpr());
                obj.addProperty("coutReel", op.getCoutReel() != null ? op.getCoutReel().toString() : "null");
                obj.addProperty("date", op.getDate() != null ? sdfOutput.format(op.getDate()) : "null");
                obj.addProperty("nomPuit", op.getNom_puit() != null ? op.getNom_puit() : "null");
                jsonArray.add(obj);
            }

            jsonResponse.add("operations", jsonArray);
            jsonResponse.addProperty("success", true);
            out.print(jsonResponse.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Erreur lors de la récupération des opérations : " + e.getMessage());
            out.print(jsonResponse.toString());
        }
    }
}

