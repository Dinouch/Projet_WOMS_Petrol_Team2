package com.example.dao;

import com.example.entities.CoutOpr;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class CoutOprDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void create(CoutOpr coutOpr) {
        em.persist(coutOpr);
        em.flush();
    }

    // Nouvelle méthode pour récupérer les coûts par statut
    public List<CoutOpr> findByStatus(String status) {
        TypedQuery<CoutOpr> query = em.createQuery(
                "SELECT c FROM CoutOpr c WHERE c.statutCout = :status",
                CoutOpr.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    // Méthode pour récupérer les coûts à surveiller et en dépassement
    public List<CoutOpr> findAlerts() {
        TypedQuery<CoutOpr> query = em.createQuery(
                "SELECT c FROM CoutOpr c WHERE c.statutCout = 'Dépassement' OR c.statutCout = 'À surveiller'",
                CoutOpr.class);
        return query.getResultList();
    }

    // Méthode pour récupérer les coûts en dépassement
    public List<CoutOpr> findOverruns() {
        return findByStatus("Dépassement");
    }

    // Méthode pour récupérer les coûts à surveiller
    public List<CoutOpr> findToMonitor() {
        return findByStatus("À surveiller");
    }
}