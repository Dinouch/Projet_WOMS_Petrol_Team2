package com.example.entities;

import com.google.gson.annotations.Expose;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;
import java.time.Duration;
import java.time.LocalTime;

@Entity
@Table(name = "DELAI_OPR")
@SequenceGenerator(name = "delai_opr_seq", sequenceName = "DELAI_OPR_SEQ", allocationSize = 1)
public class DelaiOpr {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "delai_opr_seq")
    @Column(name = "id_delai_opr")
    private Long idDelaiOpr;

    @Column(name = "desp_opr")
    private String despOpr;

    @Column(name = "start_time")
    private Time startTime;

    @Column(name = "end_time")
    private Time endTime;

    @Column(name = "duree_pr") // New column for planned duration
    private String dureePr;

    @Column(name = "statut_delai")
    private String statutDelai;

    @Column(name = "date_creation")
    @Temporal(TemporalType.DATE)
    private Date dateCreation;

    @Column(name = "profondeur")
    private String profondeur;

    @Column(name = "progress")
    private String progress;

    @Column(name = "phase")
    private String phase;

    @Column(name = "daily_npt")
    private String dailyNpt;

    @Column(name = "cumulative_npt")
    private String cumulativeNpt;

    private String nom_puit;

    // Constructors
    public DelaiOpr() {
    }

    // Business method to calculate and set status based on duration comparison
    @PrePersist
    @PreUpdate
    public void calculateStatus() {
        if (startTime != null && endTime != null && dureePr != null) {
            // Calculate actual duration
            LocalTime start = startTime.toLocalTime();
            LocalTime end = endTime.toLocalTime();
            Duration actualDuration = Duration.between(start, end);


            Duration plannedDuration = parseDuration(dureePr);


            long diffMinutes = actualDuration.minus(plannedDuration).toMinutes();

            if (diffMinutes == 0) {
                statutDelai = "Sous Contrôle";
            }
            else if (diffMinutes > 0) { // Si durée réelle > durée prévue
                if (diffMinutes <= 30) { // Différence petite
                    statutDelai = "A Surveiller";
                } else { // Différence grande
                    statutDelai = "Dépassement";
                }
            }
            else { // Si durée réelle < durée prévue (diffMinutes négatif)
                if (diffMinutes >= -30) { // Différence petite
                    statutDelai = "A Surveiller";
                } else { // Différence grande
                    statutDelai = "Avance";
                }
            }
        }
    }


    public Duration parseDuration(String durationStr) {
        String[] parts = durationStr.split("h");
        int hours = Integer.parseInt(parts[0]);
        int minutes = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return Duration.ofHours(hours).plusMinutes(minutes);
    }

    // Getters and Setters
    public Long getIdDelaiOpr() {
        return idDelaiOpr;
    }

    public void setIdDelaiOpr(Long idDelaiOpr) {
        this.idDelaiOpr = idDelaiOpr;
    }

    public String getDespOpr() {
        return despOpr;
    }

    public void setDespOpr(String despOpr) {
        this.despOpr = despOpr;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public String getDureePr() {
        return dureePr;
    }

    public void setDureePr(String dureePr) {
        this.dureePr = dureePr;
    }

    public String getStatutDelai() {
        return statutDelai;
    }

    public void setStatutDelai(String statutDelai) {
        this.statutDelai = statutDelai;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
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

    public String getNom_puit() {
        return nom_puit;
    }

    public void setNom_puit(String nom_puit) {
        this.nom_puit = nom_puit;
    }
<<<<<<< HEAD

    public void setDureepr(String dureepr) {
        this.dureepr = dureepr;
    }
    public String getDureepr() { return dureepr;     }

}
=======
}
>>>>>>> origin/cerinebackfinal
