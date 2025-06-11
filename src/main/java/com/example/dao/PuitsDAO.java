package com.example.dao;

import com.example.entities.PUITS;
import com.example.entities.ZONE;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@Stateless
public class PuitsDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    public PUITS save(PUITS puits) {
        if (puits.getId_puit() == null) {
            em.persist(puits);
            em.flush(); // Force la synchronisation pour obtenir l'ID généré
            return puits;
        } else {
            return em.merge(puits);
        }
    }

    public PUITS findById(Long id) {
        return em.find(PUITS.class, id);
    }

    public List<PUITS> findAll() {
        return em.createQuery("SELECT p FROM PUITS p", PUITS.class).getResultList();
    }

    public List<PUITS> findAllWithZone() {
        return em.createQuery("SELECT p FROM PUITS p LEFT JOIN FETCH p.zone", PUITS.class)
                .getResultList();
    }

    public PUITS update(PUITS puits) {
        return em.merge(puits);
    }


    public void delete(Long id) {
        PUITS puits = findById(id);
        if (puits != null) {
            em.remove(puits);
        }
    }
    public List<PUITS> findAllWithCompleteZone() {
        return em.createQuery(
                        "SELECT p FROM PUITS p LEFT JOIN FETCH p.zone z LEFT JOIN FETCH z.champsAdditionnels",
                        PUITS.class)
                .getResultList();
    }

    public ZONE findZoneForNewPuit() throws IllegalStateException {
        List<ZONE> zones = em.createQuery("SELECT z FROM ZONE z ORDER BY z.idZone", ZONE.class)
                .getResultList();

        if (zones.isEmpty()) {
            throw new IllegalStateException("La table ZONE est vide. Créez d'abord des zones.");
        }

        long nombrePuits = em.createQuery("SELECT COUNT(p) FROM PUITS p", Long.class)
                .getSingleResult();
        int indexZone = (int)(nombrePuits % zones.size());
        return zones.get(indexZone);
    }

    public PUITS findByDate(Date date) {
        try {
            return em.createQuery("SELECT p FROM PUITS p WHERE p.date_creation = :date", PUITS.class)
                    .setParameter("date", date)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }


}