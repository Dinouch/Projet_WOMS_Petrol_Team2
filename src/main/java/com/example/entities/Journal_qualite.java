package com.example.entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "JOURNAL_QUALITE")
@SequenceGenerator(name = "journal_qualite_seq", sequenceName = "JOURNAL_QUALITE_SEQ", allocationSize = 1)
public class Journal_qualite {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "journal_qualite_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "report_date")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "RPM_min")
    private Integer rpmMin;

    @Column(name = "RPM_max")
    private Integer rpmMax;

    @Column(name = "pressure")
    private Integer pressure;

    @Column(name = "progress")
    private Integer progress;

    private String nom_puit;

    // Constructeur par défaut
    public Journal_qualite() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getRpmMin() {
        return rpmMin;
    }

    public void setRpmMin(Integer rpmMin) {
        this.rpmMin = rpmMin;
    }

    public Integer getRpmMax() {
        return rpmMax;
    }

    public void setRpmMax(Integer rpmMax) {
        this.rpmMax = rpmMax;
    }

    public Integer getPressure() {
        return pressure;
    }

    public void setPressure(Integer pressure) {
        this.pressure = pressure;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }
    public String getNom_puit() {
        return nom_puit;
    }

    public void setNom_puit(String nom_puit) {
        this.nom_puit = nom_puit;
    }
}