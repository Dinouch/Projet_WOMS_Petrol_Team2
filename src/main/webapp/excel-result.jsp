<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.json.JSONObject" %>
<%
    JSONObject data = (JSONObject) request.getAttribute("excelData");
    String jsonFilePath = (String) request.getAttribute("jsonFilePath");
    String originalFileName = (String) request.getAttribute("originalFileName");
%>
<html>
<head>
    <title>Résultat Import</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .success { color: #4CAF50; }
        .error { color: #f44336; }
        pre {
            background: #f5f5f5;
            padding: 15px;
            border-radius: 4px;
            overflow-x: auto;
        }
        .btn {
            display: inline-block;
            background: #4CAF50;
            color: white;
            padding: 10px 15px;
            text-decoration: none;
            border-radius: 4px;
            margin: 10px 0;
        }
        .btn:hover { background: #45a049; }
    </style>
</head>
<body>
    <h1>Résultat de l'import</h1>

    <% if (data != null) { %>
        <p class="success">Fichier "<%= originalFileName %>" traité avec succès !</p>

        <h2>Données extraites :</h2>
        <pre><%= data.toString(4) %></pre>

        <a href="download-json?filePath=<%= java.net.URLEncoder.encode(jsonFilePath, "UTF-8") %>"
           class="btn">
            Télécharger le JSON
        </a>
    <% } else { %>
        <p class="error">Erreur lors du traitement du fichier</p>
    <% } %>

    <div>
        <a href="upload.jsp" class="btn">Importer un autre fichier</a>
        <a href="index.jsp" class="btn">Retour à l'accueil</a>
    </div>
</body>
</html>