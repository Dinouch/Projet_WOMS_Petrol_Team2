package com.example.utils;

import com.example.dao.FichierDrillingDAO;
import com.example.entities.FICHIER_DRILLING;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class DrillingReportParserEJB {

    private static final Logger logger = Logger.getLogger(DrillingReportParserEJB.class.getName());

    @EJB
    private FichierDrillingDAO fichierDrillingDAO;

    public JSONObject importDrillingReport(String filePath) {
        try {
            logger.log(Level.INFO, "Début de l'import du fichier: {0}", filePath);

            // 1. Extraire les données du fichier Excel
            JSONObject result = DrillingReportParser.parseDrillingReport(filePath, "drilling_report_data.json");

            // 2. Sauvegarder le fichier et les données dans la base de données
            saveFileToDatabase(filePath, result);

            logger.log(Level.INFO, "Fin de l'import du fichier");
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de l'import du fichier", e);
            throw new RuntimeException("Erreur pendant l'import du fichier: " + e.getMessage(), e);
        }
    }

    private void saveFileToDatabase(String filePath, JSONObject jsonData) throws IOException {
        logger.log(Level.INFO, "Début de la sauvegarde en base de données");

        // Créer une nouvelle entité
        FICHIER_DRILLING fichier = new FICHIER_DRILLING();

        // Définir les informations de l'entité
        File file = new File(filePath);
        fichier.setNomFichier(file.getName());
        fichier.setDateUpload(new Date());
        fichier.setJsonData(jsonData.toString());

        // Lire le contenu du fichier
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
        fichier.setContenuFichier(fileContent);

        // Sauvegarder dans la base de données
        logger.log(Level.INFO, "Tentative de sauvegarde du fichier dans la base de données");
        fichierDrillingDAO.save(fichier);
        logger.log(Level.INFO, "Sauvegarde réussie");
    }
}