package com.example.dao;

import com.example.entities.PUITS;
import com.example.entities.ZONE;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Stateless
public class PuitsDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void save(PUITS puits) {
        em.persist(puits);
        em.flush();
    }

    public ZONE findZoneById(long idZone) {
        return em.find(ZONE.class, idZone);
    }

    public long countAllPuits() {
        return em.createQuery("SELECT COUNT(p) FROM PUITS p", Long.class)
                .getSingleResult();
    }





    public List<ZONE> findAllZonesOrdered() {
        return em.createQuery("SELECT z FROM ZONE z ORDER BY z.idZone", ZONE.class)
                .getResultList();
    }

    public ZONE findZoneForNewPuit() throws IllegalStateException {
        List<ZONE> zones = findAllZonesOrdered();

        if (zones.isEmpty()) {
            throw new IllegalStateException("La table ZONE est vide. Créez d'abord des zones.");
        }

        long nombrePuits = countAllPuits();
        int indexZone = (int)(nombrePuits % zones.size());
        return zones.get(indexZone);
    }
    public List<PUITS> findAllPuitsOrdered() {
        return em.createQuery("SELECT p FROM PUITS p ORDER BY p.id_puit", PUITS.class)
                .getResultList();
    }


    public PUITS findByDate(Date date) {
        if (date == null) {
            return null;
        }

        try {
            TypedQuery<PUITS> query = em.createQuery(
                    "SELECT p FROM Puits p WHERE p.date = :date",
                    PUITS.class
            );
            query.setParameter("date", date);

            List<PUITS> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);

        } catch (Exception e) {
            // Log l'erreur si nécessaire (ex: logger.error("Erreur recherche puits", e))
            return null;
        }
    }


}