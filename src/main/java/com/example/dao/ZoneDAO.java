package com.example.dao;

import com.example.entities.ZONE;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class ZoneDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    public void create(ZONE zone) {
        em.persist(zone);
    }
}
