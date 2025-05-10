package com.example.entities;

import jakarta.persistence.*;
import java.util.Objects;

@Entity(name = "APP_USERS")
@Table(name = "APP_USERS")
public class APP_USERS {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1)
    @Column(name = "USER_ID")
    private Long id;

    @Column(name = "USER_NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "USER_EMAIL", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "USER_PASSWORD", nullable = false, length = 255)
    private String password;

    @Column(name = "USER_ROLE", nullable = false, length = 50)
    private String role = "USER";

    @Column(name = "IS_CONNECTED", nullable = false)
    private boolean isConnected = false;

    // Constructeurs
    public APP_USERS() {}

    public APP_USERS(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        APP_USERS appUsers = (APP_USERS) o;
        return Objects.equals(id, appUsers.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}