package com.example.dao;

import com.example.entities.JournalDelai;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class JournalDelaiDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void create(JournalDelai journalDelai) {
        em.persist(journalDelai);
        em.flush();
    }
}