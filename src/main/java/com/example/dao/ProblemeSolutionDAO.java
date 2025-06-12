package com.example.dao;

import com.example.entities.ProblemeSolution;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 * DAO (Data Access Object) pour gérer les opérations CRUD sur les entités ProblemeSolution
 * Utilise JPA (Jakarta Persistence) pour l'accès aux données
 */
@Stateless
public class ProblemeSolutionDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;


    /**
     * Crée une nouvelle entrée de problème/solution dans la base de données
     */
    public void create(ProblemeSolution problemeSolution) {
        em.persist(problemeSolution);
    }

    /**
     * Récupère tous les problèmes/solutions sans filtre
     */
    public List<ProblemeSolution> findAll() {
        return em.createQuery("SELECT ps FROM ProblemeSolution ps", ProblemeSolution.class)
                .getResultList();
    }

    /**
     * Récupère uniquement les problèmes ayant une solution associée
     */
    public List<ProblemeSolution> findWithSolutions() {
        return em.createQuery(
                        "SELECT ps FROM ProblemeSolution ps WHERE ps.descriptionSolution IS NOT NULL",
                        ProblemeSolution.class)
                .getResultList();
    }

    public ProblemeSolution findById(String id) {
        return em.find(ProblemeSolution.class, id);
    }

    public void updateSolution(String id, String solution) {
        ProblemeSolution ps = findById(id);
        if (ps != null) {
            ps.setDescriptionSolution(solution);
            em.merge(ps);
        }
    }
}
