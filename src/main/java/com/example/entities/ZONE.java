package com.example.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "ZONE")
@SequenceGenerator(name = "zone_seq", sequenceName = "ZONE_SEQ", allocationSize = 1)
public class ZONE {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zone_seq")
    @Column(name = "id_zone")
    private Long idZone;

    @Column(name = "wilaya")
    private String wilaya;

    @Column(name = "base")
    private String base;

    @Column(name = "X")
    private Double x;

    @Column(name = "Y")
    private Double y;

    @Column(name = "elevation")
    private Double elevation;

    @Column(name = "ellipsoide")
    private String ellipsoide;

    @Column(name = "projection")
    private String projection;

    // Constructeur vide
    public ZONE() {}

    // Getters et setters
    public Long getIdZone() { return idZone; }
    public void setIdZone(Long idZone) { this.idZone = idZone; }

    public String getWilaya() { return wilaya; }
    public void setWilaya(String wilaya) { this.wilaya = wilaya; }

    public String getBase() { return base; }
    public void setBase(String base) { this.base = base; }

    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }

    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }

    public Double getElevation() { return elevation; }
    public void setElevation(Double elevation) { this.elevation = elevation; }

    public String getEllipsoide() { return ellipsoide; }
    public void setEllipsoide(String ellipsoide) { this.ellipsoide = ellipsoide; }

    public String getProjection() { return projection; }
    public void setProjection(String projection) { this.projection = projection; }
}

