package com.example.dao;

import com.example.entities.FORMULAIRE;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class FormulaireDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void create(FORMULAIRE formulaire) {
        em.persist(formulaire);
        em.flush();
    }
}

