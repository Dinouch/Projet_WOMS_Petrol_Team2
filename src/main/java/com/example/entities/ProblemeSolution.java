package com.example.entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "PROBLEME_SOLUTION")
public class ProblemeSolution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", updatable = false, nullable = false)
    private String id;

    @Column(name = "DESCRIPTION_PROBLEME", nullable = false, columnDefinition = "TEXT")
    private String descriptionProbleme;

    @Column(name = "TYPE_PROBLEME", nullable = false)
    private String typeProbleme;

    @Column(name = "DESCRIPTION_SOLUTION", columnDefinition = "TEXT")
    private String descriptionSolution;

    @Column(name = "ETAT", columnDefinition = "TEXT")
    private String etat;

    @Column(name = "DATE_AJOUT", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAjout;

    @ManyToOne
    @JoinColumn(name = "ID_PUIT")
    private PUITS puit;

    // Constructeurs
    public ProblemeSolution() {
        this.dateAjout = new Date(); // automatique
    }

    public ProblemeSolution(String descriptionProbleme, String typeProbleme) {
        this.descriptionProbleme = descriptionProbleme;
        this.typeProbleme = typeProbleme;
        this.dateAjout = new Date(); // automatique
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getDescriptionProbleme() {
        return descriptionProbleme;
    }

    public void setDescriptionProbleme(String descriptionProbleme) {
        this.descriptionProbleme = descriptionProbleme;
    }

    public String getTypeProbleme() {
        return typeProbleme;
    }

    public void setTypeProbleme(String typeProbleme) {
        this.typeProbleme = typeProbleme;
    }

    public String getDescriptionSolution() {
        return descriptionSolution;
    }

    public void setDescriptionSolution(String descriptionSolution) {
        this.descriptionSolution = descriptionSolution;
    }

    public PUITS getPuit() {
        return puit;
    }

    public void setPuit(PUITS puit) {
        this.puit = puit;
    }

    public Date getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(Date dateAjout) {
        this.dateAjout = dateAjout;
    }
}
