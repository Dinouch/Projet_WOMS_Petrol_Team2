package com.example.utils;

import com.example.dao.FichierDrillingDAO;
import com.example.entities.FICHIER_DRILLING;

import java.io.File;
import java.io.FileWriter;

public class DrillingJsonExporter {

    public static void exportJsonFromDatabaseToFile(Long fichierId, FichierDrillingDAO dao) {
        try {
            FICHIER_DRILLING fichier = dao.findById(fichierId);
            if (fichier == null || fichier.getJsonData() == null) {
                System.out.println("⚠️ Fichier ou JSON introuvable en base.");
                return;
            }

            File dataDir = new File("data");
            if (!dataDir.exists()) dataDir.mkdirs();

            File jsonFile = new File(dataDir, "drilling_report_data.json");
            try (FileWriter writer = new FileWriter(jsonFile)) {
                writer.write(fichier.getJsonData());
            }

            System.out.println("✅ JSON exporté depuis la base vers le fichier : " + jsonFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'export JSON :");
            e.printStackTrace();
        }
    }
}

