<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.entities.DelaiOpr" %>
<html>
<head>
  <title>Détails des opérations</title>
  <style>
    table {
      border-collapse: collapse;
      width: 90%;
      margin: 20px auto;
    }
    th, td {
      padding: 10px;
      border: 1px solid #ccc;
      text-align: center;
    }
    th {
      background-color: #f0f0f0;
    }
  </style>
</head>
<body>
  <h2 style="text-align:center;">Opérations pour la date : <%= request.getParameter("date") %></h2>

  <table>
    <tr>
      <th>Heure Début</th>
      <th>Heure Fin</th>
      <th>Opération</th>
      <th>Durée PR</th>
      <th>Statut</th>
    </tr>

    <%
        Object delaisObj = request.getAttribute("delais");
        if (delaisObj instanceof List) {
            List<DelaiOpr> delais = (List<DelaiOpr>) delaisObj;
            if (!delais.isEmpty()) {
                for (DelaiOpr delai : delais) {
    %>
      <tr>
        <td><%= delai.getStartTime() %></td>
        <td><%= delai.getEndTime() %></td>
        <td><%= delai.getDespOpr() %></td>
        <td><%= delai.getDureePr() %></td>
        <td><%= delai.getStatutDelai() %></td>
      </tr>
    <%
                }
            } else {
    %>
      <tr><td colspan="5">Aucune opération trouvée pour cette date.</td></tr>
    <%
            }
        } else {
    %>
      <tr><td colspan="5">Erreur : données introuvables.</td></tr>
    <%
        }
    %>
  </table>
</body>
</html>
