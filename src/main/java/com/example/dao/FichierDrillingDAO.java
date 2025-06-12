package com.example.dao;

import com.example.entities.FICHIER_DRILLING;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class FichierDrillingDAO {

    private static final Logger logger = Logger.getLogger(FichierDrillingDAO.class.getName());

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

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

    public FICHIER_DRILLING findById(Long id) {
        return em.find(FICHIER_DRILLING.class, id);
    }

    public List<FICHIER_DRILLING> findAll() {
        return em.createQuery("SELECT f FROM FICHIER_DRILLING f", FICHIER_DRILLING.class)
                .getResultList();
    }
}
