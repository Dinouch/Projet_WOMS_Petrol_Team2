package com.example.servlets;
import com.example.dao.DelaiOprDAO;
import com.example.entities.DelaiOpr;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// fct2 :  afficher pour chaque date, la phase, la profondeur,le progres,daily NPT,cumulative NPT, et son statut
@WebServlet("/summaryByDate")
public class SummaryByDateServlet extends HttpServlet {

    @Inject
    private DelaiOprDAO delaiOprDAO;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Object[]> summaries = delaiOprDAO.getSummaryByDate();
        List<Map<String, Object>> formattedSummaries = summaries.stream().map(summary -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", summary[0]);
            map.put("phase", summary[1]);
            map.put("profondeur", summary[2]);
            map.put("progress", summary[3]);
            map.put("dailyNpt", summary[4]);
            map.put("cumulativeNpt", summary[5]);

            int statusCode = (int) summary[6];
            map.put("statut",
                    statusCode == 2 ? "Dépassement" :
                            statusCode == 1 ? "A Surveiller" : "Sous Contrôle");

            return map;
        }).collect(Collectors.toList());

        request.setAttribute("summaries", formattedSummaries);
        request.getRequestDispatcher("/summaryByDate.jsp").forward(request, response);
    }
}