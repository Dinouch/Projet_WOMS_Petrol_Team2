package com.example.entities;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity(name = "DRILLING_PARAMETERS")
@Table(name = "DRILLING_PARAMETERS")
public class DRILLING_PARAMETERS implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "drilling_params_seq")
    @SequenceGenerator(name = "drilling_params_seq", sequenceName = "drilling_params_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BIT_NUMBER")
    private Integer bitNumber;

    @Column(name = "BIT_SIZE")
    private String bitSize;

    @Column(name = "WOB_MIN")
    private Double wobMin; // Weight on Bit min (tonnes)

    @Column(name = "WOB_MAX")
    private Double wobMax; // Weight on Bit max (tonnes)

    @Column(name = "RPM_MIN")
    private Integer rpmMin;

    @Column(name = "RPM_MAX")
    private Integer rpmMax;

    @Column(name = "FLOW_RATE")
    private Integer flowRate; // GPM

    @Column(name = "PRESSURE")
    private Integer pressure; // PSI

    @Column(name = "HSI")
    private Double hsi; // Horsepower per square inch

    @Column(name = "DEPTH")
    private Double depth; // Profondeur en mètres

    // Constructeurs
    public DRILLING_PARAMETERS() {}

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getBitNumber() {
        return bitNumber;
    }

    public void setBitNumber(Integer bitNumber) {
        this.bitNumber = bitNumber;
    }

    public String getBitSize() {
        return bitSize;
    }

    public void setBitSize(String bitSize) {
        this.bitSize = bitSize;
    }

    public Double getWobMin() {
        return wobMin;
    }

    public void setWobMin(Double wobMin) {
        this.wobMin = wobMin;
    }

    public Double getWobMax() {
        return wobMax;
    }

    public void setWobMax(Double wobMax) {
        this.wobMax = wobMax;
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

    public Integer getFlowRate() {
        return flowRate;
    }

    public void setFlowRate(Integer flowRate) {
        this.flowRate = flowRate;
    }

    public Integer getPressure() {
        return pressure;
    }

    public void setPressure(Integer pressure) {
        this.pressure = pressure;
    }

    public Double getHsi() {
        return hsi;
    }

    public void setHsi(Double hsi) {
        this.hsi = hsi;
    }

    public Double getDepth() {
        return depth;
    }

    public void setDepth(Double depth) {
        this.depth = depth;
    }
}
