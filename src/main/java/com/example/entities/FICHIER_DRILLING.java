package com.example.entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "FICHIER_DRILLING") // Assurez-vous que le nom est en majuscules comme votre classe
public class FICHIER_DRILLING {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fichier_drilling_seq")
    @SequenceGenerator(name = "fichier_drilling_seq", sequenceName = "fichier_drilling_seq", allocationSize = 1)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_UPLOAD")
    private Date dateUpload;

    @Column(name = "NOM_FICHIER")
    private String nomFichier;

    @Lob
    @Column(name = "CONTENU_FICHIER")
    private byte[] contenuFichier;

    @Lob
    @Column(name = "JSON_DATA")
    private String jsonData;

    @Lob
    @Column(name = "JSON_COST_DATA")
    private String jsonCostData;

    // Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateUpload() {
        return dateUpload;
    }

    public void setDateUpload(Date dateUpload) {
        this.dateUpload = dateUpload;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public byte[] getContenuFichier() {
        return contenuFichier;
    }

    public void setContenuFichier(byte[] contenuFichier) {
        this.contenuFichier = contenuFichier;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public String getJsonCostData() {
        return jsonCostData;
    }

    public void setJsonCostData(String jsonData) {
        this.jsonCostData = jsonCostData;
    }
}