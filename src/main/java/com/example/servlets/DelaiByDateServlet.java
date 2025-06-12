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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;



@WebServlet("/delaiByDate")
public class DelaiByDateServlet extends HttpServlet {

    @Inject
    private DelaiOprDAO delaiOprDAO;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String dateStr = request.getParameter("date");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateStr);

            List<DelaiOpr> delais = delaiOprDAO.findByDate(date);
            request.setAttribute("delais", delais);
            request.getRequestDispatcher("/delaiByDate.jsp").forward(request, response);

        } catch (ParseException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Format de date invalide");
        }
    }
}
