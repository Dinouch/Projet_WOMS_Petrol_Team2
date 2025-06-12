<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<html>
<head>
  <title>Résumé journalier</title>
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
  <h2 style="text-align:center;">Résumé journalier</h2>
  <table>
    <tr>
      <th>Date</th><th>Phase</th><th>Profondeur</th>
      <th>Progrès</th><th>NPT Journalier</th>
      <th>NPT Cumulé</th><th>Statut Global</th>
    </tr>
    <%
        Object summariesObj = request.getAttribute("summaries");
        if (summariesObj instanceof List) {
            List<?> summariesList = (List<?>) summariesObj;
            if (!summariesList.isEmpty() && summariesList.get(0) instanceof Map) {
                for (Map<String, Object> summary : (List<Map<String, Object>>) summariesList) {
    %>
      <tr>
        <td><%= summary.get("date") %></td>
        <td><%= summary.get("phase") %></td>
        <td><%= summary.get("profondeur") %></td>
        <td><%= summary.get("progress") %></td>
        <td><%= summary.get("dailyNpt") %></td>
        <td><%= summary.get("cumulativeNpt") %></td>
        <td><%= summary.get("statut") %></td>
      </tr>
    <%
                }
            } else {
    %>
      <tr><td colspan="7">Aucun résumé ou format invalide.</td></tr>
    <%
            }
        } else {
    %>
      <tr><td colspan="7">Erreur : Données non disponibles</td></tr>
    <%
        }
    %>
  </table>
</body>
</html>
