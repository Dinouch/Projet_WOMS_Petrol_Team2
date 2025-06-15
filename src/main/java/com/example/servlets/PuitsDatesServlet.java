package com.example.servlets;

import com.example.dao.DelaiOprDAO;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import com.google.gson.Gson;

@WebServlet("/puitsDates")
public class PuitsDatesServlet extends HttpServlet {

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");

        List<Object[]> puitsDates = delaiOprDAO.getPuitsWithDates();
        List<Map<String, Object>> results = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Object[] row : puitsDates) {
            Map<String, Object> map = new HashMap<>();
            map.put("nomPuit", row[0]);
            map.put("dateDebut", sdf.format((Date) row[1]));
            map.put("dateActuelle", sdf.format((Date) row[2]));
            results.add(map);
        }

        Gson gson = new Gson();
        String json = gson.toJson(results);
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}
