package com.example.dao;

import com.example.entities.CoutOpr;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.config.JpaUtil.getEntityManager;

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

    public List<Object[]> getOperationsPourPuit(String nomPuit, Date date) {
        String jpql = "SELECT c.date, SUM(c.coutReel), SUM(c.coutPrevu), c.phase " +
                "FROM CoutOpr c " +
                "WHERE c.nom_puit = :nomPuit AND c.date = :date " +
                "GROUP BY c.date, c.phase";

        return em.createQuery(jpql, Object[].class)
                .setParameter("nomPuit", nomPuit)
                .setParameter("date", date)
                .getResultList();
    }


    public List<CoutOpr> getOperationsPourPuitEtDate(String nomPuit, Date date) {
        try {
            String jpql = "SELECT c FROM CoutOpr c WHERE c.nom_puit = :nomPuit AND c.date_creation = :date ORDER BY c.idCoutOpr";
            return em.createQuery(jpql, CoutOpr.class)
                    .setParameter("nomPuit", nomPuit)
                    .setParameter("date", date)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des opérations pour le puits " + nomPuit
                    + " et la date " + date, e);
        }
    }


// pop up cout jdida
public List<CoutOpr> findByPuitAndDate(String nomPuit, Date date) {
    return em.createQuery(
                    "SELECT c FROM CoutOpr c WHERE c.nom_puit = :nomPuit AND c.date_creation = :date",
                    CoutOpr.class)
            .setParameter("nomPuit", nomPuit)
            .setParameter("date", date)
            .getResultList();
}
// pour page cout global

    public Map<String, Object> getStatistiquesGlobalesProjet(String nomPuit) {
        Map<String, Object> result = new HashMap<>();
        BigDecimal budgetPrevisionnel = new BigDecimal("22112000");

        try {
            // 1. Calcul du total réel
            String sqlSommeReel = "SELECT SUM(COALESCE(cout_reel, 0)) FROM cout_opr WHERE nom_puit = ?";
            BigDecimal totalReel = convertToBigDecimal(
                    em.createNativeQuery(sqlSommeReel)
                            .setParameter(1, nomPuit)
                            .getSingleResult()
            );

            // 2. Détermination du statut global
            String statutGlobal = "Vert"; // Par défaut
            if (totalReel.compareTo(budgetPrevisionnel) > 0) {
                statutGlobal = "Rouge";
            }

            // 3. Calculs des indicateurs
            BigDecimal totalPrevuReste = budgetPrevisionnel.subtract(totalReel).max(BigDecimal.ZERO);
            BigDecimal totalNonPrevu = totalReel.subtract(budgetPrevisionnel).max(BigDecimal.ZERO);

            // 4. Arrondi des valeurs
            totalReel = totalReel.setScale(2, RoundingMode.HALF_UP);
            totalPrevuReste = totalPrevuReste.setScale(2, RoundingMode.HALF_UP);
            totalNonPrevu = totalNonPrevu.setScale(2, RoundingMode.HALF_UP);
            budgetPrevisionnel = budgetPrevisionnel.setScale(2, RoundingMode.HALF_UP);

            // 5. Stockage des résultats
            result.put("statutGlobal", statutGlobal);
            result.put("totalReel", totalReel);
            result.put("totalPrevuReste", totalPrevuReste);
            result.put("totalNonPrevu", totalNonPrevu);
            result.put("budgetPrevisionnel", budgetPrevisionnel);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul des statistiques globales", e);
        }

        return result;
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        return new BigDecimal(value.toString());
    }





}