package com.example.entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "JOURNAL_DELAI")
@SequenceGenerator(name = "journal_delai_seq", sequenceName = "JOURNAL_DELAI_SEQ", allocationSize = 1)
public class JournalDelai {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "journal_delai_seq")
    @Column(name = "id_journal")
    private Long idJournal;

    @Column(name = "profondeur")
    private String profondeur;

    @Column(name = "progress")
    private String progress;

    @Column(name = "date_rapport")
    @Temporal(TemporalType.DATE)
    private Date dateRapport;

    @Column(name = "phase")
    private String phase;

    @Column(name = "daily_npt")
    private String dailyNpt;

    @Column(name = "cumulative_npt")
    private String cumulativeNpt;



    // Constructeurs
    public JournalDelai() {
    }

    // Getters et Setters
    public Long getIdJournal() {
        return idJournal;
    }

    public void setIdJournal(Long idJournal) {
        this.idJournal = idJournal;
    }

    public String getProfondeur() {
        return profondeur;
    }

    public void setProfondeur(String profondeur) {
        this.profondeur = profondeur;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public Date getDateRapport() {
        return dateRapport;
    }

    public void setDateRapport(Date dateRapport) {
        this.dateRapport = dateRapport;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getDailyNpt() {
        return dailyNpt;
    }

    public void setDailyNpt(String dailyNpt) {
        this.dailyNpt = dailyNpt;
    }

    public String getCumulativeNpt() {
        return cumulativeNpt;
    }

    public void setCumulativeNpt(String cumulativeNpt) {
        this.cumulativeNpt = cumulativeNpt;
    }


}