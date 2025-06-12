package com.example.servlets;

import com.example.dao.DelaiOprDAO;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


@WebServlet("/puitsDates")
public class PuitsDatesServlet extends HttpServlet {

    @Inject
    private DelaiOprDAO delaiOprDAO;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Object[]> puitsDates = delaiOprDAO.getPuitsWithDates();

        // Formatage des résultats
        List<Map<String, Object>> results = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Object[] row : puitsDates) {
            Map<String, Object> map = new HashMap<>();
            map.put("nomPuit", row[0]);
            map.put("dateDebut", sdf.format((Date)row[1]));
            map.put("dateActuelle", sdf.format((Date)row[2]));
            results.add(map);
        }

        request.setAttribute("puitsDates", results);
        request.getRequestDispatcher("/puitsDates.jsp").forward(request, response);
    }
}
