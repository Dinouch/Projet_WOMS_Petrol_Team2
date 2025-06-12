# WOMS Petrol - Backend

<div align="center">



**Système de Gestion et de Planification Préventive pour l'Industrie Pétrolière**

_Solution développée par l'équipe **PRISM** pour Sonatrach dans le cadre du projet 2CS - ESI_

</div>

---

## Table des Matières

- [À Propos du Projet](#à-propos-du-projet)
- [Contexte Client](#contexte-client)
- [Fonctionnalités](#fonctionnalités)
- [Technologies Utilisées](#technologies-utilisées)
- [Prérequis Système](#prérequis-système)
- [Installation](#installation)
- [Configuration](#configuration)
- [Utilisation](#utilisation)
- [Architecture](#architecture)
- [API Documentation](#api-documentation)
- [Perspectives d'Évolution](#perspectives-dévolution)
- [Équipe de Développement](#équipe-de-développement)

---

## À Propos du Projet

**WOMS Petrol** est une solution de gestion et de planification préventive développée spécifiquement pour répondre aux besoins opérationnels de Sonatrach dans le domaine pétrolier. Cette plateforme transforme les données complexes des forages en informations exploitables, facilitant la prise de décision stratégique et opérationnelle.

### Objectifs du Système

Le système vise à faciliter l'interprétation des informations issues des forages pétroliers pour mieux anticiper, planifier et optimiser les opérations tout en minimisant les risques, les temps d'arrêt et les coûts opérationnels.

---

## Contexte Client

Ce projet est développé dans le cadre d'un partenariat avec **Sonatrach**, entreprise leader dans le secteur des hydrocarbures en Algérie. La solution répond aux défis spécifiques de gestion des opérations de forage et de maintenance préventive des installations pétrolières.

**Client** : Sonatrach  
**Cadre** : Projet de spécialité 2CS - École Supérieure d'Informatique  
**Durée** : Année académique 2024-2025

---

## Fonctionnalités

### Cartographie Interactive des Puits

- Visualisation géospatiale en temps réel de l'ensemble des puits
- Système de codes couleur pour l'état de fonctionnement
- Navigation intuitive entre sites géographiquement dispersés
- Accès rapide aux informations critiques

### Tableau de Bord Opérationnel

- Métriques essentielles configurables selon les besoins utilisateur
- Interface adaptée aux processus métier de Sonatrach
- Représentation visuelle synthétique de l'état des puits
- Vue d'ensemble consolidée des opérations

### Système d'Alertes Avancé

- **Alertes IADC Dull Grading** : Surveillance de l'usure des outils de forage
- **Monitoring des Paramètres Critiques** : Détection des écarts de pression (psi) et débit (gpm)
- **Gestion des Retards** : Notifications automatiques des délais imprévus
- **Optimisation ROP** : Alerte en cas de chute de la vitesse de pénétration
- **Contrôle Qualité Boue** : Surveillance des risques liés aux fluides de forage
- **Suivi Budgétaire** : Notifications de dépassement des enveloppes financières

### Planification des Opérations

- Définition de la périodicité optimale selon l'état des installations
- Respect des contraintes temporelles et budgétaires
- Approche préventive de gestion des risques opérationnels
- Génération de rapports détaillés et analyses de performance

---

## Technologies Utilisées

| Technologie         | Version | Rôle dans l'Architecture              |
| ------------------- | ------- | ------------------------------------- |
| **Java**            | 11+     | Langage de programmation principal    |
| **Jakarta EE**      | 10.0.0  | Framework d'entreprise                |
| **Apache Tomcat**   | 10.x    | Serveur d'applications                |
| **Maven**           | 3.x     | Gestionnaire de dépendances et build  |
| **Oracle Database** | 19c+    | Système de gestion de base de données |
| **Hibernate**       | 6.2.0   | ORM (Object-Relational Mapping)       |
| **Apache POI**      | 5.2.3   | Traitement des fichiers Excel         |
| **Gson**            | 2.10    | Sérialisation/désérialisation JSON    |
| **BCrypt**          | 0.4     | Sécurisation des mots de passe        |

---

## Prérequis Système

### Environnement de Développement

- **Java JDK 11** ou version supérieure
- **Oracle Database 19c** ou version supérieure
- **Apache Maven 3.6+**
- **Apache Tomcat 10.x**
- **Git** pour le contrôle de version

### Configuration Serveur de Production

- Serveur compatible Jakarta EE 10
- Base de données Oracle avec licences appropriées
- Connexion réseau sécurisée pour l'accès aux données terrain

---

## Installation

### Étape 1 : Récupération du Code Source

```bash
git clone https://github.com/Dinouch/Projet_WOMS_Petrol_Team2.git
cd test_j2ee
```

### Étape 2 : Configuration Base de Données

```sql
-- Création de la base de données Oracle
CREATE DATABASE woms_petrol;

-- Création d'un utilisateur dédié
CREATE USER woms_user IDENTIFIED BY your_secure_password;
GRANT ALL PRIVILEGES ON woms_petrol.* TO 'woms_user';
```

### Étape 3 : Build du Projet

```powershell
# Vous pourrez facilement déployer l'intégralité du projet en un clic via le script powershell deploy.ps1
./deploy.ps1
```

---

## Configuration

### Configuration Hibernate (persistence.xml)

```xml
<!-- src/main/resources/META-INF/persistence.xml -->
<persistence-unit name="woms-petrol-pu">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
    <properties>
        <property name="jakarta.persistence.jdbc.driver" value="oracle.jdbc.OracleDriver"/>
        <property name="jakarta.persistence.jdbc.url" value="jdbc:oracle:thin:@server:1521:PROD"/>
        <property name="jakarta.persistence.jdbc.user" value="woms_user"/>
        <property name="jakarta.persistence.jdbc.password" value="secure_password"/>
        <property name="hibernate.dialect" value="org.hibernate.dialect.OracleDialect"/>
        <property name="hibernate.hbm2ddl.auto" value="update"/>
    </properties>
</persistence-unit>
```

### Déploiement Docker (Environnement de Test)

```bash
# Lancement des services conteneurisés
docker-compose up -d
```

---

## Utilisation

### Démarrage de l'Application

1. Déployer le fichier WAR sur le serveur Tomcat
2. Accéder à l'interface via : `http://localhost:8090/petrol`

### Points d'Accès Principaux

- **Interface Principale** : `/`
- **API Gestion Puits** : `/api/wells`
- **Tableau de Bord** : `/api/dashboard`
- **Système d'Alertes** : `/api/alerts`
- **Génération Rapports** : `/api/reports`

---

## Architecture

### Structure du Projet

```
src/main/
├── java/com/sonatrach/woms/
│   ├── config/          # Configuration applicative
│   ├── dao/             # Couche d'accès aux données
│   ├── entities/        # Entités métier JPA
│   ├── servlets/        # Contrôleurs REST
│   ├── services/        # Logique métier
│   └── utils/           # Utilitaires transverses
├── resources/
│   └── META-INF/        # Configuration JPA/Hibernate
└── webapp/
    └── WEB-INF/         # Configuration web
        └── data/        # Données de référence
```

### Modèle Architectural

- **Couche Présentation** : Servlets Jakarta EE avec API REST
- **Couche Services** : Logique métier et règles de gestion
- **Couche Persistance** : DAO Pattern avec Hibernate/JPA
- **Couche Données** : Oracle Database avec optimisations sectorielles

---

## API Documentation

### Gestion des Puits Pétroliers

```http
GET    /api/wells              # Récupération de tous les puits
GET    /api/wells/{id}         # Détails d'un puits spécifique
POST   /api/wells              # Création d'un nouveau puits
PUT    /api/wells/{id}         # Modification des données d'un puits
DELETE /api/wells/{id}         # Suppression d'un puits
```

### Analytics et Reporting

```http
GET    /api/dashboard          # Données consolidées du tableau de bord
GET    /api/metrics            # Métriques opérationnelles temps réel
GET    /api/reports/{type}     # Génération de rapports par typologie
```

### Système d'Alertes

```http
GET    /api/alerts             # Consultation des alertes actives
POST   /api/alerts/config      # Configuration des seuils d'alerte
PUT    /api/alerts/{id}/ack    # Acquittement d'une alerte
```

---

## Perspectives d'Évolution

### Fonctionnalités Planifiées

- **Intelligence Artificielle** : Implémentation de modèles prédictifs pour l'optimisation des opérations
- **Analytics Avancés** : Tableaux de bord prédictifs avec scénarios d'optimisation
- **API Intelligence** : Services d'aide à la décision basés sur l'historique opérationnel

### Améliorations Techniques

- Intégration avec les systèmes d'information existants de Sonatrach
- Module de reporting avancé avec export vers formats métier
- Interface mobile pour les équipes terrain
- Système de notifications temps réel multi-canal

---

## Équipe de Développement

Ce projet a été réalisé intégralement par l'équipe PRISM avec :


### **MALLEK Dina** - Cheffe de Projet
Responsable de la coordination générale du projet, de la gestion des relations avec le client Sonatrach, et du suivi des livrables. Assure l'interface entre l'équipe technique et les parties prenantes.

### **TAIBI Ryad Brahim** - Développeur Backend
Spécialisé dans l'architecture serveur et le développement des services REST. Responsable de la conception de l'infrastructure backend, de l'intégration avec Oracle Database et de l'optimisation des performances.

### **CHEHBOUB Cérine Mona** - Développeur Full-Stack
En charge du développement de l'interface utilisateur et de l'intégration frontend-backend. Responsable de l'expérience utilisateur et de la cohérence de l'application.

### **GUEDDA Fatima Zahraa** - Développeur Data
Spécialisée dans la modélisation des données métier et l'optimisation des bases de données. Responsable de la structure des données pétrolières et des requêtes de performance.

**Encadrement Académique** : Équipe pédagogique ESI  
**Supervision Client** : Équipe technique Sonatrach

---



**Encadrement Académique** :
- Pr NADER Fahima
- Pr CHALAL Rachid
- Dr MAHFOUDI Amar

**Supervision Client** :
- Mr ABDELAZIZ Arezki (ancien cadre chez Sonatrach)

---

## Informations Projet

**Statut** : En cours de développement  
**Version** : 1.0-SNAPSHOT  
**Livraison prévue** : Juin 2025

---

## Support et Documentation

Pour toute question technique ou fonctionnelle concernant le projet :

- **Contact Équipe** : Via les canaux de communication ESI
- **Documentation Technique** : Bientôt disponible

---

## Licence

Ce projet est développé dans le cadre d'un partenariat académique ESI-Sonatrach. Tous droits de propriété intellectuelle selon accords contractuels.

---

<div align="center">

**Développé par l'équipe PRISM**  
_École Supérieure d'Informatique (ESI) - Promotion 2CS 2024-2025_  
_En partenariat avec Sonatrach_

</div>
