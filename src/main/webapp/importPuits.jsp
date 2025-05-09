<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Import JSON</title>
</head>
<body>
    <h1>Importer des données puits</h1>
    <button onclick="importData()">Importer les données</button>
    <div id="result"></div>

    <script>
        function importData() {
            const resultDiv = document.getElementById('result');
            resultDiv.innerHTML = 'Import en cours...';

            fetch('importPuits', {
                method: 'POST'
            })
            .then(response => response.text())
            .then(data => {
                resultDiv.innerHTML = data;
            })
            .catch(error => {
                resultDiv.innerHTML = 'Erreur: ' + error;
            });
        }
    </script>
</body>
</html>