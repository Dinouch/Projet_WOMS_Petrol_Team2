package com.example.dao;

import com.example.entities.DRILLING_PARAMETERS;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import java.util.List;

@Stateless
public class DrillingParametersDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    public void save(DRILLING_PARAMETERS params) {
        em.persist(params);
    }

    public List<DRILLING_PARAMETERS> getAllParameters() {
        return em.createQuery("SELECT p FROM DRILLING_PARAMETERS p", DRILLING_PARAMETERS.class)
                .getResultList();
    }

    public DRILLING_PARAMETERS getLatestParameters() {
        try {
            return em.createQuery("SELECT p FROM DRILLING_PARAMETERS p ORDER BY p.id DESC", DRILLING_PARAMETERS.class)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}