package com.example.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;

@WebServlet("/download-json")
public class JsonDownloadServlet extends HttpServlet {

    /**
     * Gère les requêtes GET pour le téléchargement de fichiers JSON :
     * - Vérifie la présence du paramètre filePath dans la requête
     * - Contrôle l'existence du fichier demandé
     * - Configure les en-têtes de réponse pour forcer le téléchargement
     * - Transfère le contenu du fichier JSON vers le flux de sortie de la réponse
     * - Gère les erreurs (paramètre manquant, fichier introuvable)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String filePath = request.getParameter("filePath");
        if (filePath == null || filePath.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètre manquant");
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Fichier non trouvé");
            return;
        }

        response.setContentType("application/json");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"export_" + System.currentTimeMillis() + ".json\"");

        try (InputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
}
