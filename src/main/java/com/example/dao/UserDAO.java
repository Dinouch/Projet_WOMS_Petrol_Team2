package com.example.dao;

import com.example.entities.APP_USERS;
import jakarta.persistence.EntityManager;
import java.util.List;

public class UserDAO {
    private final EntityManager entityManager;

    public UserDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void addUser(APP_USERS user) {
        entityManager.persist(user);
    }

    public APP_USERS getUserById(Long id) {
        return entityManager.find(APP_USERS.class, id);
    }

    public List<APP_USERS> getAllUsers() {
        return entityManager.createQuery("SELECT u FROM APP_USERS u", APP_USERS.class).getResultList();
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
}