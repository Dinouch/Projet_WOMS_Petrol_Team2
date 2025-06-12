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
import java.util.List;

// fct1 : afficher la liste des delai opr entiere
@WebServlet("/listDelaiOpr")
public class ListDelaiOprServlet extends HttpServlet {

    @Inject
    private DelaiOprDAO delaiOprDAO;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<DelaiOpr> delais = delaiOprDAO.findAll();
        request.setAttribute("delais", delais);
        request.getRequestDispatcher("/listeDelaiOpr.jsp").forward(request, response);
    }
}