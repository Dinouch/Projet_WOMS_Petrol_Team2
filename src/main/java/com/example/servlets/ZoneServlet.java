package com.example.servlets;

import com.example.dao.ZoneDAO;
import com.example.entities.ZONE;
import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/zones")
public class ZoneServlet extends HttpServlet {

    @EJB
    private ZoneDAO zoneDAO;

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String idParam = request.getParameter("id");
        PrintWriter out = response.getWriter();

        try {
            if (idParam != null) {
                Long id = Long.parseLong(idParam);
                ZONE zone = zoneDAO.findById(id);

                if (zone != null) {
                    out.print(gson.toJson(zone));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson("Zone non trouvée"));
                }
            } else {
                out.print(gson.toJson(zoneDAO.findAll()));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("ID invalide"));
        } finally {
            out.flush();
        }
    }
}