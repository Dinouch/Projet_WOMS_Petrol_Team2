
package com.example.dao;

import com.example.entities.DelaiOpr;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import jakarta.persistence.TemporalType;

import java.sql.Time;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Stateless
public class DelaiOprDAO {

    @PersistenceContext(unitName = "myPU")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void create(DelaiOpr delaiOpr) {
        em.persist(delaiOpr);
    }

    // Method to generate and persist random test data
    public void generateTestData(int numberOfRecords) {
        Random random = new Random();
        String[] operations = {
                "MUD LOGGING", "DOWNHOLE RENTAL", "DRILLING MUD",
                "SOLID CONTROL", "WELL HEAD", "CASING", "CEMENTING"
        };
        String[] phases = {"26''", "16''", "12 1/4", "8 1/2"};

        for (int i = 0; i < numberOfRecords; i++) {
            DelaiOpr record = new DelaiOpr();

            // Set random operation and phase
            record.setDespOpr(operations[random.nextInt(operations.length)]);
            record.setPhase(phases[random.nextInt(phases.length)]);

            // Set random planned duration (1h00 to 5h30)
            int hours = 1 + random.nextInt(5);
            int minutes = random.nextInt(60);
            record.setDureePr(String.format("%dh%02d", hours, minutes));

            // Set random start time (between 8:00 and 16:00)
            long startMillis = TimeUnit.HOURS.toMillis(8) +
                    random.nextInt((int)TimeUnit.HOURS.toMillis(8));
            record.setStartTime(new Time(startMillis));

            // Calculate end time based on planned duration with some variance
            Duration plannedDuration = record.parseDuration(record.getDureePr());
            long variance = random.nextInt(90) - 30; // -30 to +60 minutes
            long endMillis = startMillis + plannedDuration.toMillis() +
                    TimeUnit.MINUTES.toMillis(variance);
            record.setEndTime(new Time(endMillis));

            // Set other fields
            record.setDateCreation(new Date());
            record.setProfondeur(String.format("%d m", 100 + random.nextInt(4000)));
            record.setProgress(String.format("%d%%", random.nextInt(100)));
           // record.setNomPuit("Puit-" + (1 + random.nextInt(10)));

            // Status will be automatically calculated by @PrePersist
            em.persist(record);
        }
    }

    public List<DelaiOpr> findAll() {
        return em.createQuery("SELECT d FROM DelaiOpr d", DelaiOpr.class).getResultList();
    }
// fct 2 : Afficher le résumé par date (Journal delai)
    public List<Object[]> getSummaryByDate() {
        return em.createQuery(
                        "SELECT d.dateCreation, d.phase, d.profondeur, d.progress, " +
                                "d.dailyNpt, d.cumulativeNpt, " +
                                "MIN(CASE WHEN d.statutDelai = 'Dépassement' THEN 2 " +
                                "WHEN d.statutDelai = 'A Surveiller' THEN 1 " +
                                "ELSE 0 END) as statusCode " +
                                "FROM DelaiOpr d " +
                                "GROUP BY d.dateCreation, d.phase, d.profondeur, d.progress, d.dailyNpt, d.cumulativeNpt " +
                                "ORDER BY d.dateCreation DESC", Object[].class)
                .getResultList();
    }

//fct 3 : Pour une date precise: afficher les operations et leur statut
    public List<DelaiOpr> findByDate(Date date) {
        return em.createQuery(
                        "SELECT d FROM DelaiOpr d WHERE d.dateCreation = :date ORDER BY d.startTime",
                        DelaiOpr.class)
                .setParameter("date", date, TemporalType.DATE)
                .getResultList();
    }

    public List<Object[]> getPuitsWithDates() {
        return em.createQuery(
                        "SELECT d.nom_puit, MIN(d.dateCreation) as dateDebut, MAX(d.dateCreation) as dateActuelle " +
                                "FROM DelaiOpr d " +
                                "WHERE d.nom_puit IS NOT NULL " +
                                "GROUP BY d.nom_puit " +
                                "ORDER BY d.nom_puit", Object[].class)
                .getResultList();
    }
// Cercle delai et KPI
public Map<String, Object> getStatistiquesGlobalesDelai(String nomPuit) {
        Map<String, Object> result = new HashMap<>();
        final int DELAI_PREVU_TOTAL = 120; // 120 jours
        final int SEUIL_ORANGE = 110; // Seuil pour passer en orange

        try {
            // 1. Calcul du nombre de jours distincts (nbrJourX)
            String sqlNbJours = "SELECT COUNT(DISTINCT TRUNC(date_creation)) FROM delai_opr WHERE nom_puit = ?";
            Integer nbrJourX = ((Number) em.createNativeQuery(sqlNbJours)
                    .setParameter(1, nomPuit)
                    .getSingleResult()).intValue();

            // 2. Calcul du statut global selon la nouvelle logique
            String statutGlobalDelai;

            if (nbrJourX > DELAI_PREVU_TOTAL) {
                statutGlobalDelai = "Rouge"; // Dépasse le délai prévu
            } else if (nbrJourX >= SEUIL_ORANGE) {
                statutGlobalDelai = "Orange"; // Approche du délai prévu
            } else {
                statutGlobalDelai = "Vert"; // Dans les temps
            }

            // 3. Calcul des indicateurs
            int totalPrevuReste = DELAI_PREVU_TOTAL - nbrJourX;
            int totalNonPrevu = Math.max(nbrJourX - DELAI_PREVU_TOTAL, 0);

            // 4. Stockage des résultats
            result.put("statutGlobalDelai", statutGlobalDelai);
            result.put("nbrJourX", nbrJourX);
            result.put("totalJour", nbrJourX);
            result.put("totalPrevuReste", totalPrevuReste);
            result.put("totalNonPrevu", totalNonPrevu);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul des statistiques de délai", e);
        }

        return result;
    }

    // Nouvelle méthode pour récupérer les délais par statut
    public List<DelaiOpr> findByStatus(String status) {
        TypedQuery<DelaiOpr> query = em.createQuery(
                "SELECT d FROM DelaiOpr d WHERE d.statutDelai = :status",
                DelaiOpr.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    // Méthode pour récupérer les délais à surveiller et en dépassement
    public List<DelaiOpr> findAlerts() {
        TypedQuery<DelaiOpr> query = em.createQuery(
                "SELECT d FROM DelaiOpr d WHERE d.statutDelai = 'Dépassement' OR d.statutDelai = 'À surveiller'",
                DelaiOpr.class);
        return query.getResultList();
    }

    // Méthode pour récupérer les délais en dépassement
    public List<DelaiOpr> findOverruns() {
        return findByStatus("Dépassement");
    }

    // Méthode pour récupérer les délais à surveiller
    public List<DelaiOpr> findToMonitor() {
        return findByStatus("À surveiller");
    }
}