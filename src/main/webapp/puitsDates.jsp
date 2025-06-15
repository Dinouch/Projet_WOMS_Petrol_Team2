<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<html>
<head>
    <title>Dates par Puits</title>
    <style>
        table {
            border-collapse: collapse;
            width: 80%;
            margin: 20px auto;
        }
        th, td {
            padding: 10px;
            border: 1px solid #ccc;
            text-align: center;
        }
        th {
            background-color: #f2f2f2;
        }
    </style>
</head>
<body>
<h2 style="text-align:center;">Dates des opérations par puits</h2>
<table>
    <tr>
        <th>Nom du Puits</th>
        <th>Date de Début</th>
        <th>Date Actuelle</th>
    </tr>
    <%
        Object puitsDatesObj = request.getAttribute("puitsDates");
        if (puitsDatesObj instanceof List) {
            List<?> puitsDatesList = (List<?>) puitsDatesObj;
            if (!puitsDatesList.isEmpty() && puitsDatesList.get(0) instanceof Map) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                for (Map<String, Object> puit : (List<Map<String, Object>>) puitsDatesList) {
    %>
        <tr>
            <td><%= puit.get("nomPuit") != null ? puit.get("nomPuit") : "N/A" %></td>
            <td><%= puit.get("dateDebut") != null ? puit.get("dateDebut") : "N/A" %></td>
            <td><%= puit.get("dateActuelle") != null ? puit.get("dateActuelle") : "N/A" %></td>
        </tr>
    <%
                }
            } else {
    %>
        <tr><td colspan="3">Aucun enregistrement ou format invalide.</td></tr>
    <%
            }
        } else {
    %>
        <tr><td colspan="3">Erreur : Données non disponibles</td></tr>
    <%
        }
    %>
</table>
</body>
</html>