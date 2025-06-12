package com.example.servlets;

import com.example.dao.PuitsDAO;
import com.example.entities.PUITS;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;

@WebServlet("/puits/*")
public class PuitsServlet extends HttpServlet {

    @EJB
    private PuitsDAO puitsDAO;

    private final Gson gson = new Gson();

    /**
     * Configuration des en-têtes CORS pour autoriser les requêtes cross-origin depuis React.
     * Les en-têtes permettent les méthodes GET, POST, PUT, DELETE et OPTIONS.
     */
    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
    }


    /**
     * Gère les requêtes GET pour :
     * - Récupérer un puit spécifique si un ID est fourni dans le path
     * - Lister tous les puits avec leurs zones si aucun ID n'est spécifié
     * Retourne les données au format JSON
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo(); // ex: "/5"

        try {
            if (pathInfo != null && pathInfo.length() > 1) {
                // Supprimer le slash initial
                String idStr = pathInfo.substring(1);
                Long id = Long.parseLong(idStr);

                PUITS puit = puitsDAO.findById(id);
                if (puit != null) {
                    out.print(gson.toJson(puit));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson("Puit non trouvé"));
                }
            } else {
                // Pas d'ID fourni → retourner tous les puits
                out.print(gson.toJson(puitsDAO.findAllWithZone()));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("ID invalide"));
        } finally {
            out.flush();
        }
    }

    /**
     * Gère les requêtes POST pour créer un nouveau puit :
     * - Parse le JSON reçu pour créer un objet PUITS
     * - Gère l'attribution automatique de zone si non spécifiée
     * - Définit la date de création automatiquement
     * - Enregistre le nouveau puit en base
     * Retourne le puit créé avec son ID généré
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            // Lire le JSON
            JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

            // Convertir en objet PUITS
            PUITS newPuit = gson.fromJson(jsonObject, PUITS.class);

            // Gérer la zone si spécifiée dans le JSON
            if (jsonObject.has("zoneId")) {
                Long zoneId = jsonObject.get("zoneId").getAsLong();
                newPuit.setZoneId(zoneId);
            }

            // Vérifier si le code existe déjà


            // Définir la date de création
            newPuit.setDate(new java.util.Date());

            // Si aucune zone n'est spécifiée, attribuer automatiquement
            if (newPuit.getZone() == null) {
                newPuit.setZone(puitsDAO.findZoneForNewPuit());
            }

            // Sauvegarder
            PUITS createdPuit = puitsDAO.save(newPuit);

            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(createdPuit));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(gson.toJson("Erreur: " + e.getMessage()));
        }
    }

    /**
     * Gère les requêtes PUT pour mettre à jour un puit existant :
     * - Récupère l'ID du puit depuis l'URL
     * - Met à jour uniquement les champs présents dans le JSON reçu
     * - Gère la mise à jour de la zone si spécifiée
     * - Gère la conversion des dates au format yyyy-MM-dd
     * Retourne le puit mis à jour
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            String pathInfo = request.getPathInfo(); // ex: /3
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson("ID du puit manquant dans l'URL"));
                return;
            }

            Long id = Long.parseLong(pathInfo.substring(1));
            PUITS existingPuit = puitsDAO.findById(id);

            if (existingPuit == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson("Puit non trouvé"));
                return;
            }

            JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

            // Mise à jour de tous les champs présents dans le JSON
            if (jsonObject.has("code")) existingPuit.setCode(jsonObject.get("code").getAsString());
            if (jsonObject.has("nom_puit")) existingPuit.setNom_puit(jsonObject.get("nom_puit").getAsString());
            if (jsonObject.has("statut_delai")) existingPuit.setStatut_delai(jsonObject.get("statut_delai").getAsString());
            if (jsonObject.has("statut_cout")) existingPuit.setStatut_cout(jsonObject.get("statut_cout").getAsString());

            if (jsonObject.has("date")) {
                existingPuit.setDate(Date.valueOf(jsonObject.get("date").getAsString())); // format: yyyy-MM-dd
            }
            if (jsonObject.has("date_fin_prevu")) {
                existingPuit.setDate_fin_prevu(Date.valueOf(jsonObject.get("date_fin_prevu").getAsString()));
            }
            if (jsonObject.has("date_fin_reelle")) {
                existingPuit.setDate_fin_reelle(Date.valueOf(jsonObject.get("date_fin_reelle").getAsString()));
            }

            if (jsonObject.has("zoneId")) {
                Long zoneId = jsonObject.get("zoneId").getAsLong();
                existingPuit.setZoneId(zoneId);
            }

            PUITS updatedPuit = puitsDAO.update(existingPuit);
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(updatedPuit));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(gson.toJson("Erreur: " + e.getMessage()));
        }
    }


}
