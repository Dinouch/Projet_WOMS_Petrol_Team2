package com.example.servlets;

import com.example.utils.ExcelDailyCostParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@WebServlet("/upload-dailycost")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 1,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 15
)
public class ExcelUploadServlet extends HttpServlet {

    private static final String UPLOAD_DIRECTORY = "uploads";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Vérification du type de contenu
            if (!request.getContentType().startsWith("multipart/form-data")) {
                throw new Exception("Le formulaire doit être de type multipart/form-data");
            }

            // Récupération du fichier
            Part filePart = request.getPart("excelFile");
            if (filePart == null || filePart.getSize() == 0) {
                throw new Exception("Aucun fichier n'a été uploadé");
            }

            // Validation du fichier
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            if (fileName == null || fileName.isEmpty()) {
                throw new Exception("Nom de fichier invalide");
            }

            if (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls")) {
                throw new Exception("Seuls les fichiers Excel (.xlsx, .xls) sont autorisés");
            }

            // Création du répertoire d'upload
            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Sauvegarde du fichier avec un nom unique
            String filePath = uploadPath + File.separator + System.currentTimeMillis() + "_" + fileName;
            File excelFile = new File(filePath);
            filePart.write(filePath);

            // Vérification supplémentaire
            System.out.println("[DEBUG] Chemin absolu du fichier: " + excelFile.getAbsolutePath());
            System.out.println("[DEBUG] Fichier existe: " + excelFile.exists());
            System.out.println("[DEBUG] Taille fichier: " + excelFile.length());

            // Traitement du fichier Excel
            String jsonOutputPath = uploadPath + File.separator + System.currentTimeMillis() + "_result.json";

            // Debug supplémentaire
            System.out.println("[DEBUG] Tentative de lecture du fichier Excel...");

            JSONObject result = ExcelDailyCostParser.extractDailyCostData(excelFile.getAbsolutePath(), jsonOutputPath);

            if (result == null) {
                throw new Exception("Échec du traitement. Vérifiez que le fichier n'est pas corrompu et qu'il contient des données valides.");
            }

            request.setAttribute("excelData", result);
            request.setAttribute("jsonFilePath", jsonOutputPath);
            request.setAttribute("originalFileName", fileName);
            request.getRequestDispatcher("/excel-result.jsp").forward(request, response);

        } catch (Exception ex) {
            ex.printStackTrace();
            // Enrichir le message d'erreur avec plus de détails
            String errorMessage = "Erreur technique: " + ex.getMessage();
            if (ex.getCause() != null) {
                errorMessage += " Cause: " + ex.getCause().getMessage();
            }
            errorMessage += ". Veuillez vérifier que votre fichier Excel est valide et contient bien les données attendues.";

            request.setAttribute("errorMessage", errorMessage);
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}