# TaskFlow Backend

API Backend pour l'application TaskFlow, développée avec Spring Boot.

## Fonctionnalités Implémentées

*   **Authentification** : Inscription et Connexion sécurisées des utilisateurs via JWT & Spring Security.
*   **Gestion des Tâches** : Opérations CRUD complètes pour les tâches.
*   **Accès aux Données Avancé** : Pagination, Tri et Filtrage côté serveur.
*   **Sécurité** :
    *   Chiffrement des mots de passe (BCrypt).
    *   Validation de la propriété des données (les utilisateurs ne gèrent que leurs propres tâches).
    *   Authentification sans état (Stateless).
*   **Logs** : Logging SLF4J implémenté dans les Contrôleurs et Services.
*   **Base de Données** : Base de données H2 In-Memory pour le développement.

## Comment Lancer l'Application

### Prérequis
*   Java 17+
*   Maven

### Profils

L'application utilise des profils Spring Boot pour gérer les configurations.

*   `dev` (défaut) : Utilise une clé secrète fixe pour faciliter le développement et active les logs en mode DEBUG.
*   `prod` : Nécessite une variable d'environnement pour la clé secrète et utilise les logs en mode INFO.

### Lancement

#### 1. Mode Développement (Par défaut)
Lancez simplement la commande suivante. Le profil `dev` sera utilisé automatiquement.

```bash
mvn spring-boot:run
```

OU explicitement :

```bash
mvn spring-boot:run -Dspring-profiles.active=dev
```

*   **Console Base de Données** : Accès à la console H2 sur `http://localhost:8080/h2-console` (URL JDBC : `jdbc:h2:mem:taskflowdb`, Utilisateur : `sa`, Mot de passe : `password`).
*   **API** : Accessible sur `http://localhost:8080`.

#### 2. Mode Production
Pour la production, vous **DEVEZ** fournir la variable d'environnement `JWT_SECRET`.

```bash
export JWT_SECRET=votre_cle_secrete_super_securisee_minimum_256_bits
mvn spring-boot:run -Dspring-profiles.active=prod
```

### Exécution des Tests

Pour exécuter les tests unitaires et d'intégration :

```bash
mvn clean test
```
