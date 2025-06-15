package com.example.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.config.JpaUtil.getEntityManager;

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

    public void remplirCoutPrevuDepuisMap() {

        Map<String, BigDecimal> coutsPrevus = new HashMap<>();
        coutsPrevus.put("DRILLING RIG  - ENTP", new BigDecimal("2700000"));

        coutsPrevus.put("ADDITIONAL SERVICES", new BigDecimal("2700000"));
        coutsPrevus.put("DOWNHOLE RENTAL TOOLS  - WEATHERFORD", new BigDecimal("2700000"));
        coutsPrevus.put("SOLID CONTROL - NOS", new BigDecimal("15000"));
        coutsPrevus.put("COMMUNICATION AND DATA", new BigDecimal("2700000"));
        coutsPrevus.put("RIG SUPERVISION", new BigDecimal("2700000"));
        coutsPrevus.put("SECURITY", new BigDecimal("2700000"));

        coutsPrevus.put("MUD LOGGING -  WEATHERFORD", new BigDecimal("150000"));
        coutsPrevus.put("CEMENTING CASING AND TUBING TESTING & COMPLETION - SCHLUMBERGER", new BigDecimal("225000"));
        coutsPrevus.put("WATER SUPPLIED WITH OLD DISTANCE & OLD  COST SINCE SEPT/02  TO SEPT 14 /2013   -MARAR", new BigDecimal("700000"));
        coutsPrevus.put("WATER SUPPLIED   WITH NEW DISTANCE AND NEW COST    - MARAR", new BigDecimal("700000"));
        coutsPrevus.put("WATER SERVICES - NAZAR", new BigDecimal("700000"));

        coutsPrevus.put("WELL  HEAD  MSP DRILEX", new BigDecimal("56000"));

        coutsPrevus.put("CASING", new BigDecimal("86000"));
        coutsPrevus.put("CASING ACCESSORIES - WEATHERFORD", new BigDecimal("200000"));
        coutsPrevus.put("RUNNING CASING AND TUBING - WEATHERFORD", new BigDecimal("200000"));

        coutsPrevus.put("DRILLING BITS - NOV", new BigDecimal("280000"));
        coutsPrevus.put("CORING - ENTP", new BigDecimal("50000"));
        coutsPrevus.put("DRILLING MUD  - NOS", new BigDecimal("350000"));
        coutsPrevus.put("LOGGING - SCHLUMBERGER", new BigDecimal("400000"));
        coutsPrevus.put("TESTING - ENTP", new BigDecimal("1100000"));
        coutsPrevus.put("SECURITE", new BigDecimal("700000"));



        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            for (Map.Entry<String, BigDecimal> entry : coutsPrevus.entrySet()) {
                em.createQuery("UPDATE CoutOpr c SET c.coutPrevu = :cout WHERE c.nomOpr = :nom")
                        .setParameter("cout", entry.getValue())
                        .setParameter("nom", entry.getKey())
                        .executeUpdate();
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors du remplissage des coûts prévus : " + e.getMessage(), e);
        }
    }
}