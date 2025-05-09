package com.example.entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "PUITS")
@SequenceGenerator(name = "puit_seq", sequenceName = "PUIT_SEQ", allocationSize = 1)
public class PUITS {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "puit_seq")
    @Column(name = "id_puit")

    private Long id_puit;

    @Temporal(TemporalType.DATE)
    private Date date_fin_prevu;

    @Temporal(TemporalType.DATE)
    private Date date_fin_reelle;

    private String statut_cout;
    private String statut_delai;
    private String nom_puit;
    private String code;

    @ManyToOne
    @JoinColumn(name = "id_zone")
    private ZONE zone;

    @Column(name = "date_creation")
    @Temporal(TemporalType.DATE)
    private Date date_creation;


    // Getters and Setters

    public Long getId_puit() {
        return id_puit;
    }

    public void setId_puit(Long id_puit) {
        this.id_puit = id_puit;
    }

    public Date getDate_fin_prevu() {
        return date_fin_prevu;
    }

    public void setDate_fin_prevu(Date date_fin_prevu) {
        this.date_fin_prevu = date_fin_prevu;
    }

    public Date getDate_fin_reelle() {
        return date_fin_reelle;
    }

    public void setDate_fin_reelle(Date date_fin_reelle) {
        this.date_fin_reelle = date_fin_reelle;
    }

    public String getStatut_cout() {
        return statut_cout;
    }

    public void setStatut_cout(String statut_cout) {
        this.statut_cout = statut_cout;
    }

    public String getStatut_delai() {
        return statut_delai;
    }

    public void setStatut_delai(String statut_delai) {
        this.statut_delai = statut_delai;
    }

    public String getNom_puit() {
        return nom_puit;
    }

    public void setNom_puit(String nom_puit) {
        this.nom_puit = nom_puit;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ZONE getZone() {
        return zone;
    }

    public void setZone(ZONE zone) {
        this.zone = zone;
    }


    public Date getDate() {
        return date_creation;
    }

    public void setDate(Date date) {
        this.date_creation = date;
    }
}

