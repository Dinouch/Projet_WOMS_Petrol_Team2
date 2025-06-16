package com.example.dao;

import com.example.entities.CoutOpr;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Stateless
public class CoutOprDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    // Méthodes CRUD de base
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void create(CoutOpr coutOpr) {
        em.persist(coutOpr);
        em.flush();
    }

    // Méthodes pour les alertes
    public List<CoutOpr> findOverruns() {
        return em.createQuery(
                        "SELECT c FROM CoutOpr c WHERE c.coutReel > c.coutPrevu",
                        CoutOpr.class)
                .getResultList();
    }

    public List<CoutOpr> findToMonitor() {
        return em.createQuery(
                        "SELECT c FROM CoutOpr c WHERE c.coutReel <= c.coutPrevu AND c.coutReel IS NOT NULL",
                        CoutOpr.class)
                .getResultList();
    }

    // Initialisation des coûts prévus
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void remplirCoutsPrevusDepuisMap() {
        Map<String, BigDecimal> coutsPrevus = new HashMap<>();
        coutsPrevus.put("DRILLING RIG - ENTP", new BigDecimal("2700000"));
        coutsPrevus.put("ADDITIONAL SERVICES", new BigDecimal("2700000"));
        coutsPrevus.put("DOWNHOLE RENTAL TOOLS - WEATHERFORD", new BigDecimal("2700000"));
        coutsPrevus.put("SOLID CONTROL - NOS", new BigDecimal("15000"));
        coutsPrevus.put("COMMUNICATION AND DATA", new BigDecimal("2700000"));
        coutsPrevus.put("RIG SUPERVISION", new BigDecimal("2700000"));
        coutsPrevus.put("SECURITY", new BigDecimal("2700000"));
        coutsPrevus.put("MUD LOGGING - WEATHERFORD", new BigDecimal("150000"));
        coutsPrevus.put("CEMENTING CASING AND TUBING TESTING & COMPLETION - SCHLUMBERGER", new BigDecimal("225000"));
        coutsPrevus.put("WATER SUPPLIED WITH OLD DISTANCE & OLD COST SINCE SEPT/02 TO SEPT 14 /2013 - MARAR", new BigDecimal("700000"));
        coutsPrevus.put("WATER SUPPLIED WITH NEW DISTANCE AND NEW COST - MARAR", new BigDecimal("700000"));
        coutsPrevus.put("WATER SERVICES - NAZAR", new BigDecimal("700000"));
        coutsPrevus.put("WELL HEAD MSP DRILEX", new BigDecimal("56000"));
        coutsPrevus.put("CASING", new BigDecimal("86000"));
        coutsPrevus.put("CASING ACCESSORIES - WEATHERFORD", new BigDecimal("200000"));
        coutsPrevus.put("RUNNING CASING AND TUBING - WEATHERFORD", new BigDecimal("200000"));
        coutsPrevus.put("DRILLING BITS - NOV", new BigDecimal("280000"));
        coutsPrevus.put("CORING - ENTP", new BigDecimal("50000"));
        coutsPrevus.put("DRILLING MUD - NOS", new BigDecimal("350000"));
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

    // Méthodes d'analyse temporelle
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

    public List<Object[]> getSommeParMois(String nomPuit) {
        String sql = "SELECT EXTRACT(YEAR FROM date_creation) AS year, " +
                "EXTRACT(MONTH FROM date_creation) AS month, " +
                "SUM(cout_reel), SUM(cout_prevu) " +
                "FROM cout_opr WHERE nom_puit = ? " +
                "GROUP BY EXTRACT(YEAR FROM date_creation), EXTRACT(MONTH FROM date_creation) " +
                "ORDER BY EXTRACT(YEAR FROM date_creation), EXTRACT(MONTH FROM date_creation)";

        return em.createNativeQuery(sql)
                .setParameter(1, nomPuit)
                .getResultList();
    }

    public List<Object[]> getSommeCoutsParJourPourPuit(String nomPuit) {
        return em.createQuery(
                        "SELECT c.date_creation, SUM(COALESCE(c.coutReel, 0)), " +
                                "SUM(COALESCE(c.coutPrevu, 0)), c.phase " +
                                "FROM CoutOpr c WHERE c.nom_puit = :nomPuit " +
                                "GROUP BY c.date_creation, c.phase ORDER BY c.date_creation",
                        Object[].class)
                .setParameter("nomPuit", nomPuit)
                .getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void mettreAJourStatutCout() {
        List<CoutOpr> listeCouts = em.createQuery("SELECT c FROM CoutOpr c", CoutOpr.class).getResultList();

        for (CoutOpr cout : listeCouts) {
            if (cout.getCoutReel() != null && cout.getCoutPrevu() != null) {
                String statut = cout.getCoutReel().compareTo(cout.getCoutPrevu()) > 0 ?
                        "Dépassement" :
                        cout.getCoutReel().equals(cout.getCoutPrevu()) ? "À surveiller" : "Sous contrôle";
                cout.setStatutCout(statut);
                em.merge(cout);
            }
        }
        em.flush();
    }

    // Méthodes de recherche
    public List<CoutOpr> getOperationsByDate(Date date) {
        return em.createQuery(
                        "SELECT c FROM CoutOpr c WHERE c.date_creation = :date",
                        CoutOpr.class)
                .setParameter("date", date)
                .getResultList();
    }

    public List<CoutOpr> findByPuitAndDate(String nomPuit, Date date) {
        return em.createQuery(
                        "SELECT c FROM CoutOpr c WHERE c.nom_puit = :nomPuit AND c.date_creation = :date",
                        CoutOpr.class)
                .setParameter("nomPuit", nomPuit)
                .setParameter("date", date)
                .getResultList();
    }

    public List<CoutOpr> getOperationsPourPuitEtDate(String nomPuit, Date date) {
        try {
            return em.createQuery(
                            "SELECT c FROM CoutOpr c WHERE c.nom_puit = :nomPuit AND c.date_creation = :date ORDER BY c.idCoutOpr",
                            CoutOpr.class)
                    .setParameter("nomPuit", nomPuit)
                    .setParameter("date", date)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des opérations pour le puits " + nomPuit
                    + " et la date " + date, e);
        }
    }

    // Méthodes statistiques
    public List<Object[]> getSommeParPhase(String nomPuit) {
        return em.createQuery(
                        "SELECT c.phase, SUM(c.coutReel), SUM(c.coutPrevu) " +
                                "FROM CoutOpr c WHERE c.nom_puit = :nomPuit " +
                                "GROUP BY c.phase",
                        Object[].class)
                .setParameter("nomPuit", nomPuit)
                .getResultList();
    }

    public Map<String, Object> getStatistiquesGlobalesProjet(String nomPuit) {
        Map<String, Object> result = new HashMap<>();
        BigDecimal budgetPrevisionnel = new BigDecimal("22112000");

        try {
            BigDecimal totalReel = convertToBigDecimal(
                    em.createNativeQuery("SELECT SUM(COALESCE(cout_reel, 0)) FROM cout_opr WHERE nom_puit = ?")
                            .setParameter(1, nomPuit)
                            .getSingleResult());

            String statutGlobal = totalReel.compareTo(budgetPrevisionnel) > 0 ? "Rouge" : "Vert";
            BigDecimal totalPrevuReste = budgetPrevisionnel.subtract(totalReel).max(BigDecimal.ZERO);
            BigDecimal totalNonPrevu = totalReel.subtract(budgetPrevisionnel).max(BigDecimal.ZERO);

            result.put("statutGlobal", statutGlobal);
            result.put("totalReel", totalReel.setScale(2, RoundingMode.HALF_UP));
            result.put("totalPrevuReste", totalPrevuReste.setScale(2, RoundingMode.HALF_UP));
            result.put("totalNonPrevu", totalNonPrevu.setScale(2, RoundingMode.HALF_UP));
            result.put("budgetPrevisionnel", budgetPrevisionnel.setScale(2, RoundingMode.HALF_UP));

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul des statistiques globales", e);
        }
        return result;
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        return value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString());
    }
}