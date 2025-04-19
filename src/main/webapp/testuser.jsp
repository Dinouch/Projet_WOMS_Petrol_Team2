<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.example.entities.APP_USERS"%>
<%@page import="java.util.List"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Liste des Utilisateurs</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        h1 { color: #333; }
        ul { list-style-type: none; padding: 0; }
        li { padding: 8px; margin: 5px 0; background: #f5f5f5; border-radius: 4px; }
        .error { color: red; }
    </style>
</head>
<body>
    <h1>Liste des Utilisateurs</h1>

    <%
        // Récupération de la liste avec vérification de type
        Object usersObj = request.getAttribute("users");
        if (usersObj instanceof List) {
            List<?> users = (List<?>) usersObj;
            if (!users.isEmpty() && users.get(0) instanceof APP_USERS) {
    %>
                <ul>
                    <% for (APP_USERS user : (List<APP_USERS>) users) { %>
                        <li>
                            <%= user.getName() %>
                            (<%= user.getEmail() %>)
                        </li>
                    <% } %>
                </ul>
    <%
            } else {
    %>
                <p class="error">Aucun utilisateur trouvé ou format invalide</p>
    <%
            }
        } else {
    %>
            <p class="error">Erreur: Données utilisateurs non disponibles</p>
    <%
        }
    %>

    <p><a href="${pageContext.request.contextPath}/">Retour à l'accueil</a></p>
</body>
</html>