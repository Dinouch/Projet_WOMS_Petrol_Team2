<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Créer une Zone</title>
</head>
<body>
    <h1>Création automatique d'une Zone avec valeurs nulles ou 0</h1>
    <button onclick="createZone()">Créer une Zone</button>
    <div id="result"></div>

    <script>
        function createZone() {
            const resultDiv = document.getElementById('result');
            resultDiv.innerHTML = "Création en cours...";

            fetch('createZone', {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    resultDiv.innerHTML = "Zone créée avec ID: " + data.id_zone;
                } else {
                    resultDiv.innerHTML = "Erreur: " + data.error;
                }
            })
            .catch(error => {
                resultDiv.innerHTML = "Erreur: " + error;
            });
        }
    </script>
</body>
</html>
