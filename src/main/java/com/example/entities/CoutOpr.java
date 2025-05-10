package com.example.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "COUT_OPR")
@SequenceGenerator(name = "cout_opr_seq", sequenceName = "COUT_OPR_SEQ", allocationSize = 1)
public class CoutOpr {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cout_opr_seq")
    @Column(name = "id_cout_opr")
    private Long idCoutOpr;

    @Column(name = "nom_opr")
    private String nomOpr;

    @Column(name = "cout_prevu")
    private BigDecimal coutPrevu;

    @Column(name = "cout_reel")
    private BigDecimal coutReel;

    @Column(name = "statut_cout")
    private String statutCout;

    @Column(name = "phase")
    private String phase;

    @Column(name = "date_creation")
    @Temporal(TemporalType.DATE)
    private Date date_creation;

    private String nom_puit;

    // Constructeur par défaut
    public CoutOpr() {
    }

    // Getters and Setters
    public Long getIdCoutOpr() {
        return idCoutOpr;
    }

    public void setIdCoutOpr(Long idCoutOpr) {
        this.idCoutOpr = idCoutOpr;
    }

    public String getNomOpr() {
        return nomOpr;
    }

    public void setNomOpr(String nomOpr) {
        this.nomOpr = nomOpr;
    }

    public BigDecimal getCoutPrevu() {
        return coutPrevu;
    }

    public void setCoutPrevu(BigDecimal coutPrevu) {
        this.coutPrevu = coutPrevu;
    }

    public BigDecimal getCoutReel() {
        return coutReel;
    }

    public void setCoutReel(BigDecimal coutReel) {
        this.coutReel = coutReel;
    }

    public String getStatutCout() {
        return statutCout;
    }

    public void setStatutCout(String statutCout) {
        this.statutCout = statutCout;
    }

    public Date getDate() {
        return date_creation;
    }

    public void setDate(Date date) {
        this.date_creation = date;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getNom_puit() {
        return nom_puit;
    }

    public void setNom_puit(String nom_puit) {
        this.nom_puit = nom_puit;
    }
}