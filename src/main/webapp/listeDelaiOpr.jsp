<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.entities.DelaiOpr" %>
<html>
<head>
    <title>Liste des délais d’opération</title>
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
<h2 style="text-align:center;">Liste des délais d’opération</h2>
<table>
    <tr>
        <th>Date</th><th>Opération</th><th>Phase</th><th>Statut</th>
    </tr>
    <%
        Object delaisObj = request.getAttribute("delais");
        if (delaisObj instanceof List) {
            List<?> delaisList = (List<?>) delaisObj;
            if (!delaisList.isEmpty() && delaisList.get(0) instanceof DelaiOpr) {
                for (DelaiOpr delai : (List<DelaiOpr>) delaisList) {
    %>
        <tr>
            <td><%= delai.getDateCreation() %></td>
            <td><%= delai.getDespOpr() %></td>
            <td><%= delai.getPhase() %></td>
            <td><%= delai.getStatutDelai() %></td>
        </tr>
    <%
                }
            } else {
    %>
        <tr><td colspan="4">Aucun enregistrement ou format invalide.</td></tr>
    <%
            }
        } else {
    %>
        <tr><td colspan="4">Erreur : Données non disponibles</td></tr>
    <%
        }
    %>
</table>
</body>
</html>
