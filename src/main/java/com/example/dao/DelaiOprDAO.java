package com.example.dao;

import com.example.entities.DelaiOpr;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class DelaiOprDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void create(DelaiOpr delaiOpr) {
        em.persist(delaiOpr);
        em.flush();
    }

    // Nouvelle méthode pour récupérer les délais par statut
    public List<DelaiOpr> findByStatus(String status) {
        TypedQuery<DelaiOpr> query = em.createQuery(
                "SELECT d FROM DelaiOpr d WHERE d.statutDelai = :status",
                DelaiOpr.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    // Méthode pour récupérer les délais à surveiller et en dépassement
    public List<DelaiOpr> findAlerts() {
        TypedQuery<DelaiOpr> query = em.createQuery(
                "SELECT d FROM DelaiOpr d WHERE d.statutDelai = 'Dépassement' OR d.statutDelai = 'À surveiller'",
                DelaiOpr.class);
        return query.getResultList();
    }

    // Méthode pour récupérer les délais en dépassement
    public List<DelaiOpr> findOverruns() {
        return findByStatus("Dépassement");
    }

    // Méthode pour récupérer les délais à surveiller
    public List<DelaiOpr> findToMonitor() {
        return findByStatus("À surveiller");
    }
}