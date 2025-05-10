package com.example.dao;

import com.example.entities.Journal_qualite;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class JournalQualiteDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void create(Journal_qualite journalQualite) {
        em.persist(journalQualite);
        em.flush();
    }

    public Journal_qualite findById(Long id) {
        return em.find(Journal_qualite.class, id);
    }

    public List<Journal_qualite> findAll() {
        return em.createQuery("SELECT j FROM Journal_qualite j", Journal_qualite.class)
                .getResultList();
    }

    public void update(Journal_qualite journalQualite) {
        em.merge(journalQualite);
    }

    public void delete(Long id) {
        Journal_qualite journalQualite = findById(id);
        if (journalQualite != null) {
            em.remove(journalQualite);
        }
    }
}