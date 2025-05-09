<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.json.JSONObject" %>
<%
    JSONObject data = (JSONObject) request.getAttribute("excelData");
    String jsonFilePath = (String) request.getAttribute("jsonFilePath");
    String originalFileName = (String) request.getAttribute("originalFileName");
%>
<html>
<head>
    <title>Rapport de Forage</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .section { margin-bottom: 30px; }
        pre { background-color: #f5f5f5; padding: 15px; border-radius: 5px; overflow: auto; }
        .btn {
            display: inline-block;
            padding: 8px 15px;
            background: #2196F3;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            margin-right: 10px;
        }
        h1, h2 { color: #333; }
        .error { color: #f44336; font-weight: bold; }
        .success { color: #4CAF50; font-weight: bold; }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
        }
        table, th, td {
            border: 1px solid #ddd;
        }
        th, td {
            padding: 10px;
            text-align: left;
        }
        th {
            background-color: #f2f2f2;
        }
    </style>
</head>
<body>
    <h1>Rapport de Forage Analysé</h1>

    <% if (data != null && !data.isEmpty()) { %>
        <p class="success">Fichier "<%= originalFileName %>" traité avec succès !</p>

        <div class="section">
            <h2>Données JSON</h2>
            <pre id="jsonDisplay"><%= data.toString(2) %></pre>
        </div>

        <div class="section">
            <a href="download-json?filePath=<%= java.net.URLEncoder.encode(jsonFilePath, "UTF-8") %>" class="btn">Télécharger JSON</a>
            <a href="index.jsp" class="btn">Retour</a>
        </div>
    <% } else { %>
        <p class="error">Aucune donnée n'a été extraite du fichier ou une erreur s'est produite.</p>

        <div class="section">
            <a href="index.jsp" class="btn">Retour</a>
        </div>
    <% } %>
</body>
</html>