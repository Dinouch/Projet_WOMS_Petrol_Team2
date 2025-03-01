<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.entities.User" %>
<html>
<head>
    <title>Test - Liste des utilisateurs</title>
</head>
<body>
    <h1>Liste des utilisateurs</h1>
    <ul>
        <%
            List<User> users = (List<User>) request.getAttribute("users");
            if (users != null) {
                for (User user : users) {
        %>
                    <li><%= user.getName() %> - <%= user.getEmail() %></li>
        <%
                }
            } else {
        %>
                <li>Aucun utilisateur trouvé.</li>
        <%
            }
        %>
    </ul>
</body>
</html>
