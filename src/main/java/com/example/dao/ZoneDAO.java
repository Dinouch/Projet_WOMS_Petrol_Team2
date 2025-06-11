package com.example.dao;

import com.example.entities.ZONE;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class ZoneDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    public void create(ZONE zone) {
        em.persist(zone);
    }

    public void save(ZONE zone) {
        em.persist(zone);
    }

    public ZONE findById(Long id) {
        return em.find(ZONE.class, id);
    }

    public List<ZONE> findAll() {
        return em.createQuery("SELECT z FROM ZONE z", ZONE.class).getResultList();
    }

    public void update(ZONE zone) {
        em.merge(zone);
    }

    public void delete(Long id) {
        ZONE zone = findById(id);
        if (zone != null) {
            em.remove(zone);
        }
    }
}