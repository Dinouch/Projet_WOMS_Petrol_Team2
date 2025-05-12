<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Importation des Paramètres de Forage</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css">
    <style>
        .container {
            max-width: 800px;
            margin-top: 40px;
        }
        .header {
            margin-bottom: 30px;
            text-align: center;
        }
        .btn-import {
            margin-top: 20px;
        }
        .alert {
            margin-top: 20px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Importation des Paramètres de Forage</h1>
            <p class="text-muted">Importez les données du fichier JSON dans la base de données</p>
        </div>

        <div class="card">
            <div class="card-body">
                <h5 class="card-title">Paramètres de forage</h5>
                <p class="card-text">
                    Cette action va extraire les paramètres de forage du fichier JSON et les enregistrer
                    dans la base de données. Les données suivantes seront importées :
                </p>
                <ul>
                    <li>Numéro de foret (Bit Number)</li>
                    <li>Taille du foret (Bit Size)</li>
                    <li>Poids sur l'outil min/max (WOB Min/Max)</li>
                    <li>Vitesse de rotation min/max (RPM Min/Max)</li>
                    <li>Débit (Flow Rate)</li>
                    <li>Pression (Pressure)</li>
                    <li>HSI (Horsepower per square inch)</li>
                    <li>Profondeur (Depth)</li>
                </ul>

                <form id="importForm" action="importDrillingParameters" method="post">
                    <div class="d-grid gap-2">
                        <button type="submit" class="btn btn-primary btn-import">
                            Importer les paramètres de forage
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <div id="status" style="display: none;" class="alert mt-3">
            <!-- Les messages de statut seront affichés ici -->
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.getElementById('importForm').addEventListener('submit', function(e) {
            e.preventDefault();

            const statusDiv = document.getElementById('status');
            statusDiv.className = 'alert mt-3';
            statusDiv.innerHTML = '<div class="spinner-border spinner-border-sm" role="status"></div> Importation en cours...';
            statusDiv.style.display = 'block';
            statusDiv.classList.add('alert-info');

            fetch('importDrillingParameters', {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    statusDiv.innerHTML = '<strong>Succès!</strong> ' + data.message;
                    statusDiv.classList.remove('alert-info');
                    statusDiv.classList.add('alert-success');
                } else {
                    statusDiv.innerHTML = '<strong>Erreur:</strong> ' + data.error;
                    statusDiv.classList.remove('alert-info');
                    statusDiv.classList.add('alert-danger');
                }
            })
            .catch(error => {
                statusDiv.innerHTML = '<strong>Erreur:</strong> Une erreur est survenue lors de la communication avec le serveur.';
                statusDiv.classList.remove('alert-info');
                statusDiv.classList.add('alert-danger');
                console.error('Error:', error);
            });
        });
    </script>
</body>
</html>