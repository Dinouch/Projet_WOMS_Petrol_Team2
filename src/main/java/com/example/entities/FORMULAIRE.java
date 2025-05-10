package com.example.entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "FORMULAIRE")
@SequenceGenerator(name = "formulaire_seq", sequenceName = "FORMULAIRE_SEQ", allocationSize = 1)
public class FORMULAIRE {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "formulaire_seq")
    @Column(name = "id_formulaire")
    private Long idFormulaire;

    @Column(name = "titre_rapport")
    private String titreRapport;

    @Column(name = "description")
    private String description;

    @Column(name = "code")
    private String code;

    @Column(name = "date_creation")
    @Temporal(TemporalType.DATE)
    private Date date_creation;

    @Column(name = "probleme")
    private String probleme;

    @Column(name = "solution")
    private String solution;

    @Column(name = "photo")
    private String photo;

    @Column(name = "num_version")
    private Integer numVersion;

    // Constructeur par défaut
    public FORMULAIRE() {
    }

    // Getters and Setters
    public Long getIdFormulaire() {
        return idFormulaire;
    }

    public void setIdFormulaire(Long idFormulaire) {
        this.idFormulaire = idFormulaire;
    }

    public String getTitreRapport() {
        return titreRapport;
    }

    public void setTitreRapport(String titreRapport) {
        this.titreRapport = titreRapport;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getDate() {
        return date_creation;
    }

    public void setDate(Date date) {
        this.date_creation = date;
    }

    public String getProbleme() {
        return probleme;
    }

    public void setProbleme(String probleme) {
        this.probleme = probleme;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Integer getNumVersion() {
        return numVersion;
    }

    public void setNumVersion(Integer numVersion) {
        this.numVersion = numVersion;
    }
}