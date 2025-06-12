package com.example.dao;

import com.example.entities.FICHIER_DRILLING;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;

import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class FichierDrillingDAO {

    /**
     * Logger pour le suivi des opérations et des erreurs
     */
    private static final Logger logger = Logger.getLogger(FichierDrillingDAO.class.getName());

    /**
     * EntityManager injecté pour les opérations de persistance
     */
    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    /**
     * Sauvegarde un fichier drilling dans la base de données
     */
    public void save(FICHIER_DRILLING fichier) {
        try {
            logger.log(Level.INFO, "Tentative de sauvegarde du fichier: {0}", fichier.getNomFichier());
            em.persist(fichier);
            em.flush(); // Force l'écriture immédiate
            logger.log(Level.INFO, "Fichier sauvegardé avec succès, ID: {0}", fichier.getId());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la sauvegarde du fichier", e);
            throw e;
        }
    }

    /**
     * Recherche un fichier drilling par son identifiant
     * @return L'entité FICHIER_DRILLING correspondante ou null si non trouvée
     */
    public FICHIER_DRILLING findById(Long id) {
        return em.find(FICHIER_DRILLING.class, id);
    }

    /**
     * Récupère la liste complète des fichiers drillings
     * @return Une liste de toutes les entités FICHIER_DRILLING
     */
    public List<FICHIER_DRILLING> findAll() {
        return em.createQuery("SELECT f FROM FICHIER_DRILLING f", FICHIER_DRILLING.class)
                .getResultList();
    }
}
