package com.example.dao;

import com.example.entities.CoutOpr;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class CoutOprDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void create(CoutOpr coutOpr) {
        em.persist(coutOpr);
        em.flush();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void remplirCoutsPrevusDepuisMap() {
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

        for (Map.Entry<String, BigDecimal> entry : coutsPrevus.entrySet()) {
            em.createQuery("UPDATE CoutOpr c SET c.coutPrevu = :cout WHERE c.nomOpr = :nom")
                    .setParameter("cout", entry.getValue())
                    .setParameter("nom", entry.getKey())
                    .executeUpdate();
        }
    }

    public List<Object[]> getSommeCoutsParJourPourPuit(String nomPuit) {
        return em.createQuery(
                        "SELECT c.date_creation, " +
                                "SUM(COALESCE(c.coutReel, 0)), " +
                                "SUM(COALESCE(c.coutPrevu, 0)), " +
                                "c.phase " +
                                "FROM CoutOpr c " +
                                "WHERE c.nom_puit = :nomPuit " +
                                "GROUP BY c.date_creation, c.phase " +
                                "ORDER BY c.date_creation", Object[].class)
                .setParameter("nomPuit", nomPuit)
                .getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void mettreAJourStatutCout() {
        List<CoutOpr> listeCouts = em.createQuery("SELECT c FROM CoutOpr c", CoutOpr.class).getResultList();

        for (CoutOpr cout : listeCouts) {
            if (cout.getCoutReel() != null && cout.getCoutPrevu() != null) {
                int cmp = cout.getCoutReel().compareTo(cout.getCoutPrevu());
                String statut;
                if (cmp > 0) {
                    statut = "Dépassement";
                } else if (cmp == 0) {
                    statut = "À surveiller";
                } else {
                    statut = "Sous contrôle";
                }
                cout.setStatutCout(statut);
                em.merge(cout);  // update
            } else {
                // Optionnel : gérer le cas où un des couts est null
                cout.setStatutCout(null);
                em.merge(cout);
            }
        }
        em.flush();
    }

    public List<CoutOpr> getOperationsByDate(Date date) {
        return em.createQuery(
                        "SELECT c FROM CoutOpr c WHERE c.date_creation = :date", CoutOpr.class)
                .setParameter("date", date)
                .getResultList();
    }

    // 1. Somme des coûts par phase
    public List<Object[]> getSommeParPhase(String nomPuit) {
        String jpql = "SELECT c.phase, SUM(c.coutReel), SUM(c.coutPrevu) " +
                "FROM CoutOpr c WHERE c.nom_puit = :nomPuit " +
                "GROUP BY c.phase";
        return em.createQuery(jpql, Object[].class)
                .setParameter("nomPuit", nomPuit)
                .getResultList();
    }

    // Somme par mois (année + mois en nombres)
    public List<Object[]> getSommeParMois(String nomPuit) {
        String sql = "SELECT EXTRACT(YEAR FROM date_creation) AS year, EXTRACT(MONTH FROM date_creation) AS month, " +
                "SUM(cout_reel), SUM(cout_prevu) " +
                "FROM cout_opr WHERE nom_puit = ? " +
                "GROUP BY EXTRACT(YEAR FROM date_creation), EXTRACT(MONTH FROM date_creation) " +
                "ORDER BY EXTRACT(YEAR FROM date_creation), EXTRACT(MONTH FROM date_creation)";

        return em.createNativeQuery(sql)
                .setParameter(1, nomPuit)
                .getResultList();
    }

    public List<Object[]> getSommeCoutReelParSemaine(String nomPuit) {
        String sql = "SELECT " +
                "TRUNC((c.date_creation - minDates.min_date) / 7) AS semaine_relative, " +
                "SUM(c.cout_reel) " +
                "FROM cout_opr c " +
                "JOIN ( " +
                "   SELECT nom_puit, MIN(date_creation) AS min_date " +
                "   FROM cout_opr " +
                "   WHERE nom_puit = ? " +
                "   GROUP BY nom_puit " +
                ") minDates ON c.nom_puit = minDates.nom_puit " +
                "WHERE c.nom_puit = ? " +
                "GROUP BY TRUNC((c.date_creation - minDates.min_date) / 7) " +
                "ORDER BY semaine_relative";

        return em.createNativeQuery(sql)
                .setParameter(1, nomPuit)
                .setParameter(2, nomPuit)
                .getResultList();
    }










}