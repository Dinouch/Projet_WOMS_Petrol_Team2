<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Accueil</title>
</head>
<body>
    <h1>Bienvenue sur l'accueil</h1>
    <nav>
        <ul>
            <li><a href="listusers">Voir les utilisateurs</a></li>
            <li><a href="#" onclick="importJson()">Importer les données du formulaire</a></li>
            <li><a href="#" onclick="importPuits()">Importer les puits</a></li> <!-- ✅ -->
            <li><a href="#" onclick="importDelaiOpr()">Importer les delai opr</a></li>
             <li><a href="#" onclick="importCoutOpr()">Importer les cout </a></li>
               <li><a href="#" onclick="importJournalQualite()">Importer le journal qualite  </a></li>
<<<<<<< Updated upstream
=======
               <li><a href="#" onclick="importDrillingParameters()">Importer les paramètres de forage</a></li>
               <li><a href="#" onclick="coutAlerts()">alerte cout</a></li>
>>>>>>> Stashed changes
        </ul>
    </nav>

    <script>
<<<<<<< Updated upstream
=======

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

>>>>>>> Stashed changes
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


                  function coutAlerts() {
                                      fetch('coutAlerts', {
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
