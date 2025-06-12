package com.example.dao;

import com.example.entities.APP_USERS;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import java.util.List;

@Stateless
public class UserDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager entityManager;

    public void addUser(APP_USERS user) {
        entityManager.persist(user);
    }

    /**
     * Récupère un utilisateur par son identifiant unique
     */
    public APP_USERS getUserById(Long id) {
        return entityManager.find(APP_USERS.class, id);
    }

    /**
     * Récupère la liste complète de tous les utilisateurs
     */
    public List<APP_USERS> getAllUsers() {
        return entityManager.createQuery("SELECT u FROM APP_USERS u", APP_USERS.class)
                .getResultList();
    }

    public APP_USERS getUserByEmail(String email) {
        try {
            return entityManager.createQuery(
                            "SELECT u FROM APP_USERS u WHERE u.email = :email",
                            APP_USERS.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void updateUser(APP_USERS user) {
        entityManager.merge(user);
    }

    public void deleteUser(Long id) {
        APP_USERS user = getUserById(id);
        if (user != null) {
            entityManager.remove(user);
        }
    }

    /**
     * Modifie le statut de connexion d'un utilisateur
     */
    public void setUserConnectedStatus(Long id, boolean isConnected) {
        APP_USERS user = getUserById(id);
        if (user != null) {
            user.setConnected(isConnected);
            entityManager.merge(user);
        }
    }
}