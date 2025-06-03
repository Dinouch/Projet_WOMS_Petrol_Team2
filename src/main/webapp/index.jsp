<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Accueil</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .btn {
            display: inline-block;
            background: #2196F3;
            color: white;
            padding: 10px 15px;
            text-decoration: none;
            border-radius: 4px;
            margin: 5px;
        }
    </style>
</head>
<body>
    <h1>Bienvenue sur l'accueil</h1>
    <nav>
        <a href="listusers" class="btn">Voir les utilisateurs</a>
        <a href="upload.jsp" class="btn">Importer un Excel</a>
        <a href="drilling-parameters" class="btn">Voir les paramètres de forage</a>
        <ul>
            <li><a href="listusers">Voir les utilisateurs</a></li>
            <li><a href="#" onclick="importJson()">Importer les données du formulaire</a></li>
            <li><a href="#" onclick="importPuits()">Importer les puits</a></li> <!-- ✅ -->
            <li><a href="#" onclick="importDelaiOpr()">Importer les delai opr</a></li>
             <li><a href="#" onclick="importCoutOpr()">Importer les cout </a></li>
               <li><a href="#" onclick="importJournalQualite()">Importer le journal qualite  </a></li>
               <li><a href="#" onclick="importDrillingParameters()">Importer les paramètres de forage</a></li>
        </ul>
    </nav>

    <script>

        function importDrillingParameters() {
            fetch('importDrillingParameters', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => response.text())
            .then(data => alert(data))
            .catch(error => alert('Erreur: ' + error));
        }

    function importDrillingParameters() {
        fetch('importDrillingParameters', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => response.text())
        .then(data => alert(data))
        .catch(error => alert('Erreur: ' + error));
    }
    
        function importJson() {
            fetch('importJson', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => response.text())
            .then(data => alert(data))
            .catch(error => alert('Erreur: ' + error));
        }

        function importPuits() {
            fetch('importPuits', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => response.text())
            .then(data => alert(data))
            .catch(error => alert('Erreur: ' + error));
        }




        function importDelaiOpr() {
            fetch('importDelaiOpr', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => response.text())
            .then(data => alert(data))
            .catch(error => alert('Erreur: ' + error));
        }


           function importJournalDelai() {
                    fetch('importJournalDelai', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    })
                    .then(response => response.text())
                    .then(data => alert(data))
                    .catch(error => alert('Erreur: ' + error));
                }


           function importCoutOpr() {
              fetch('importCoutOpr', {
                    method: 'POST',
                                   headers: {
                                       'Content-Type': 'application/json'
                                   }
                               })
                               .then(response => response.text())
                               .then(data => alert(data))
                               .catch(error => alert('Erreur: ' + error));
                           }



            function importJournalQualite() {
                     fetch('importJournalQualite', {
                         method: 'POST',
                         headers: {
                             'Content-Type': 'application/json'
                         }
                     })
                     .then(response => response.text())
                     .then(data => alert(data))
                     .catch(error => alert('Erreur: ' + error));
                 }
    </script>
</body>
</html>
