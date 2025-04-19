package com.example.entities;

import jakarta.persistence.*;

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

    // Constructeurs
    public APP_USERS() {}

    public APP_USERS(String name, String email) {
        this.name = name;
        this.email = email;
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
}