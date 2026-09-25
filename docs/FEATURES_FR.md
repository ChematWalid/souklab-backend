# Souklab Backend — Référence Technique des Fonctionnalités

<!-- AUTO-GENERATED: Ne pas éditer manuellement les tableaux de routes API ou les valeurs d'énumération dans ce fichier.
     Source de vérité : mappings des contrôleurs, énumérations des modèles, .env.example, docker-compose.yml -->

> **Version :** Spring Boot 4.0.8 · Java 21 · Généré depuis l'arbre source live le 2026-09-22.

Ce document est la référence technique officielle des fonctionnalités du backend Souklab, dérivée du code source.  
Chaque fonctionnalité listée ici a été vérifiée lors d'une campagne de tests exhaustive en conditions réelles (566 scénarios, taux de réussite de 100 %) et confirmée par la suite de tests automatisés (1 228 tests, 0 échecs).

---

## Table des matières

1. [Vue d'ensemble de la plateforme](#1-vue-densemble-de-la-plateforme)
2. [Stack technologique](#2-stack-technologique)
3. [Système de compte et d'identité](#3-système-de-compte-et-didentité)
4. [Authentification et sécurité JWT](#4-authentification-et-sécurité-jwt)
5. [Modèle de permissions et d'autorisation](#5-modèle-de-permissions-et-dautorisation)
6. [Profils artisan et galerie](#6-profils-artisan-et-galerie)
7. [Profils client et favoris](#7-profils-client-et-favoris)
8. [Catalogue et annuaire taxonomique](#8-catalogue-et-annuaire-taxonomique)
9. [Recherche dans l'annuaire des artisans](#9-recherche-dans-lannuaire-des-artisans)
10. [Formations et masterclasses](#10-formations-et-masterclasses)
11. [Accréditation Formateur](#11-accréditation-formateur)
12. [Fil d'actualités social et posts](#12-fil-dactualités-social-et-posts)
13. [Avis](#13-avis)
14. [Signalement de contenu et modération](#14-signalement-de-contenu-et-modération)
15. [Messagerie en temps réel (REST + WebSocket STOMP)](#15-messagerie-en-temps-réel-rest--websocket-stomp)
16. [Notifications](#16-notifications)
17. [Abonnements et paiements](#17-abonnements-et-paiements)
18. [Panneau d'administration et modération](#18-panneau-dadministration-et-modération)
19. [Avatars et gestion de fichiers](#19-avatars-et-gestion-de-fichiers)
20. [Stockage de fichiers (MinIO / S3)](#20-stockage-de-fichiers-minio--s3)
21. [Analytique et reporting](#21-analytique-et-reporting)
22. [Limitation du débit (Rate Limiting)](#22-limitation-du-débit-rate-limiting)
23. [Notifications par e-mail](#23-notifications-par-e-mail)
24. [Infrastructure et déploiement](#24-infrastructure-et-déploiement)
25. [Conventions API](#25-conventions-api)
26. [Référence des variables d'environnement](#26-référence-des-variables-denvironnement)
27. [Migrations de base de données](#27-migrations-de-base-de-données)
28. [Registre de vérification](#28-registre-de-vérification)

---

## 1. Vue d'ensemble de la plateforme

Souklab est une **marketplace bilatérale** mettant en relation des artisans algériens avec des clients.

| Acteur | Description |
|---|---|
| **Artisan** | Artisan qui publie un profil, liste ses produits, anime des masterclasses (formations), publie sur le fil social et échange des messages avec les clients. |
| **Client** | Consommateur qui parcourt l'annuaire, s'inscrit aux formations, suit les artisans et les contacte par messagerie. |
| **Admin** | Personnel de la plateforme avec pleins pouvoirs de modération sur les utilisateurs, le contenu, l'accréditation des artisans, les abonnements et l'analytique. |

Propositions de valeur principales :
- Annuaire d'artisans enrichi avec recherche plein texte Elasticsearch, filtrage facetté par catégorie d'artisanat, wilaya, matières, techniques et époque.
- Formations (masterclasses) avec un cycle de vie structuré : brouillon → révision admin → publication → inscription.
- Messagerie STOMP/WebSocket en temps réel entre clients et artisans, avec livraison garantie et persistance.
- Modèle d'abonnement à plusieurs niveaux (FREE / PRO / PREMIUM) conditionnant les droits des artisans.
- Stockage de fichiers via MinIO (compatible S3) avec analyse antivirus et validation des octets magiques via ClamAV.

---

## 2. Stack technologique

<!-- AUTO-GENERATED depuis pom.xml et docker-compose.yml -->

### Environnement d'exécution

| Composant | Version / Image |
|---|---|
| Java | 21 (Eclipse Temurin) |
| Spring Boot | 4.0.8 |
| Spring Framework | 7.x (résolu automatiquement via Boot) |
| Tomcat | 11.0.26 |

### Bibliothèques principales

| Bibliothèque | Version | Rôle |
|---|---|---|
| `spring-boot-starter-webmvc` | 4.0.8 | Couche REST HTTP |
| `spring-boot-starter-security` + `spring-boot-starter-oauth2-client` | 4.0.8 | Auth JWT + Google OAuth |
| `jjwt-api` / `jjwt-impl` / `jjwt-jackson` | 0.11.5 | Signature et analyse JWT |
| `spring-boot-starter-data-jpa` | 4.0.8 | ORM JPA/Hibernate |
| `mariadb-java-client` | — | Pilote JDBC MariaDB 11.4 |
| `spring-boot-starter-flyway` + `flyway-mysql` | — | Migrations de schéma DB |
| `spring-boot-starter-websocket` + `spring-boot-starter-amqp` | 4.0.8 | WebSocket STOMP + relais RabbitMQ |
| `hibernate-search-mapper-orm` + `hibernate-search-backend-elasticsearch` | 8.2.2.Final | Recherche plein texte via Elasticsearch |
| `bucket4j-core` + `bucket4j-redis` | 8.10.1 | Limitation du débit token-bucket (local ou Redis) |
| `lettuce-core` | — | Client Redis (backend de rate limiting) |
| `caffeine` | — | Cache applicatif en mémoire |
| `aws-sdk-s3` | 2.55.1 | Stockage fichiers MinIO / AWS S3 |
| `tika-core` | 4.0.0 | Détection de type MIME (inspection des octets magiques) |
| `thumbnailator` | 0.4.21 | Génération de miniatures |
| `pdfbox` | 3.0.8 | Traitement / validation PDF |
| `springdoc-openapi-starter-webmvc-ui` | 3.1.1 | OpenAPI 3 / Swagger UI |
| `spring-boot-starter-mail` | — | Envoi d'e-mails SMTP / MailerSend |
| `micrometer-registry-prometheus` | — | Export de métriques Prometheus |
| `lombok` | — | Réduction du code boilerplate |

### Infrastructure (Docker Compose)

| Service | Image | Port(s) | Rôle |
|---|---|---|---|
| `mariadb` | `mariadb:11.4` | 3306 (configurable) | Base de données relationnelle principale |
| `mariadb-flyway` | `mariadb:11.4` | 3308 (configurable) | Schéma isolé pour la vérification Flyway |
| `rabbitmq` | `rabbitmq:4.0-management` | 5672, 61613, 15672 | Broker AMQP + relais STOMP + UI de gestion |
| `minio` | `cgr.dev/chainguard/minio` | 9000, 9001 | Stockage objet compatible S3 + console |
| `clamav` | `clamav/clamav:1.4` | 3310 | Analyse antivirus |
| `redis` | `redis:7.4.1-alpine` | 6379 | Stockage rate limit + cache distribué optionnel |
| `elasticsearch` | `elasticsearch:8.15.3` | 9200 | Index de recherche plein texte |
| `app` | `Dockerfile` (build) | 8080 | Conteneur application Spring Boot (utilisateur non-root `souklab`, utilise `.env.docker`) |

---

## 3. Système de compte et d'identité

### Rôles de compte (`AccountRole`)

<!-- AUTO-GENERATED depuis src/main/java/com/project/souklab/model/AccountRole.java -->

| Valeur | Description |
|---|---|
| `ARTISAN` | Type de compte artisan. Accorde les permissions spécifiques aux artisans lors de l'activation. |
| `CLIENT` | Type de compte consommateur. Accorde les permissions de base de profil et de messagerie lors de l'activation. |
| `ADMIN` | Administrateur de la plateforme. Accordé via la console d'administration, non disponible à l'auto-inscription. |

L'inscription utilise le champ `accountType` (et non `role`). Valeurs acceptées : `ARTISAN`, `CLIENT`.

### Statuts de compte (`AccountStatus`)

<!-- AUTO-GENERATED depuis src/main/java/com/project/souklab/model/AccountStatus.java -->

| Valeur | Transitions vers | Description |
|---|---|---|
| `PENDING` | `ACTIVE`, `REJECTED` | Nouvellement inscrit — en attente de vérification e-mail et d'approbation admin. |
| `ACTIVE` | `SUSPENDED` | Compte pleinement opérationnel. |
| `SUSPENDED` | `ACTIVE` (auto si timeout expiré), débannissement manuel | Compte banni ou en suspension temporaire. L'horodatage `bannedUntil` détermine si la suspension est permanente ou temporaire. |
| `REJECTED` | — | Inscription explicitement rejetée par un administrateur. |

### Cycle de vie du compte

```
Inscription → PENDING (e-mail non vérifié)
→ Code PIN e-mail vérifié → PENDING (en attente d'approbation admin)
→ Approbation admin → ACTIVE
→ Bannissement admin → SUSPENDED (permanent)
→ Suspension temporaire admin → SUSPENDED (expire à bannedUntil)
→ Expiration du timeout → effectiveStatus = ACTIVE (automatique)
→ Débannissement admin → ACTIVE
```

### DTO d'inscription (`UserRegistrationDTO`)

| Champ | Type | Obligatoire | Validation |
|---|---|---|---|
| `email` | `String` | ✅ Oui | `@NotBlank`, `@Email` |
| `password` | `String` | ✅ Oui | `@NotBlank`, `@Size(min=8, max=128)` — règles de robustesse supplémentaires appliquées dans le service |
| `name` | `String` | Non | — |
| `firstName` | `String` | Non | — |
| `lastName` | `String` | Non | — |
| `accountType` | `AccountRole` | ✅ Oui | `@NotNull`, enum : `ARTISAN` \| `CLIENT` |

---

## 4. Authentification et sécurité JWT

### Flux d'authentification

| Étape | Endpoint | Méthode | Auth requise |
|---|---|---|---|
| 1. Inscription | `POST /api/v1/auth/register` | POST | Aucune |
| 2. Vérification e-mail | `POST /api/v1/auth/verify-email` | POST | Aucune |
| 3. Renvoi de vérification | `POST /api/v1/auth/resend-verification` | POST | Aucune |
| 4. Connexion | `POST /api/v1/auth/login` | POST | Aucune |
| 5. Rafraîchissement du token | `POST /api/v1/auth/refresh` | POST | Aucune (Bearer refresh token) |
| 6. Déconnexion | `POST /api/v1/auth/logout` | POST | Bearer access token |
| 7. Changement de mot de passe | `POST /api/v1/auth/change-password` | POST | Bearer access token |
| 8. Mot de passe oublié | `POST /api/v1/auth/forgot-password` | POST | Aucune |
| 9. Réinitialisation de mot de passe | `POST /api/v1/auth/reset-password` | POST | Aucune |
| 10. Récupérer son profil | `GET /api/v1/auth/me` | GET | Bearer access token |
| 11. Mettre à jour son profil | `PATCH /api/v1/auth/me` | PATCH | Bearer access token |
| 12. Compléter son profil | `POST /api/v1/auth/complete-profile` | POST | Bearer access token |
| 13. Google OAuth (artisan) | `GET /api/v1/auth/oauth/google/artisan` | GET | Redirection navigateur |
| 14. Google OAuth (client) | `GET /api/v1/auth/oauth/google/client` | GET | Redirection navigateur |

### Spécifications des tokens

| Propriété | Valeur |
|---|---|
| Type de token d'accès | JWT (HS256) |
| Expiration du token d'accès | `APP_JWT_ACCESS_EXP` ms (défaut 3 600 000 = 1 heure) |
| Expiration du token de rafraîchissement | `APP_JWT_REFRESH_EXP` ms (défaut 86 400 000 = 24 heures) |
| Rotation du token de rafraîchissement | Oui — chaque rafraîchissement émet une nouvelle paire et invalide l'ancienne |
| Détection de réutilisation | Oui — rejouer un token de rafraîchissement tourné déclenche la révocation de session |

### Politiques de sécurité

| Politique | Valeur configurée |
|---|---|
| Tentatives de connexion max avant verrouillage | `AUTH_LOCKOUT_MAX_ATTEMPTS=5` |
| Durée de verrouillage | `AUTH_LOCKOUT_DURATION_MINUTES=15` |
| Tentatives de vérification e-mail max | `AUTH_VERIFICATION_MAX_ATTEMPTS=5` |
| Expiration du code de vérification | `AUTH_VERIFICATION_EXPIRATION_MINUTES=15` |
| Longueur du code de vérification | `AUTH_VERIFICATION_CODE_LENGTH=6` (PIN numérique) |

### `PATCH /api/v1/auth/me` — Sémantique JSON Merge Patch

- Champs omis : **inchangés**.
- Valeurs `null` explicites : **effacement du champ** (champs nullables uniquement).
- Les champs critiques du compte (`email`, `accountStatus`, `password`, `permissions`) sont **ignorés** même s'ils sont inclus dans le corps — ils ne peuvent pas être modifiés via cet endpoint.
- `isTeacher` ne peut pas être auto-accordé ici ; il est défini uniquement via le chemin d'octroi formateur admin.

---

## 5. Modèle de permissions et d'autorisation

Toute l'autorisation est **basée sur les permissions**, non sur les rôles. Les rôles (`ARTISAN`, `CLIENT`, `ADMIN`) ne servent qu'à initialiser les permissions à l'inscription/approbation. Le contrôle d'accès est ensuite entièrement piloté par les valeurs d'énumération `Permission` détenues par l'utilisateur.

### Catalogue des permissions

<!-- AUTO-GENERATED depuis src/main/java/com/project/souklab/security/Permission.java -->

| Clé de permission | Autorité Spring | Description du détenteur |
|---|---|---|
| `permission:admin:users` | `Admin.USERS` | Gérer les utilisateurs : bannir/débannir/suspendre/approuver |
| `permission:admin:formations` | `Admin.FORMATIONS` | Réviser et publier des formations |
| `permission:admin:feed` | `Admin.FEED` | Modérer les posts et commentaires du fil |
| `permission:admin:reports` | `Admin.REPORTS` | Trier et résoudre les signalements de contenu |
| `permission:financial:admin` | `Financial.ADMIN` | Accorder/révoquer des abonnements, traiter des remboursements |
| `permission:artisan:formations` | `Artisan.FORMATIONS` | Créer et gérer ses propres formations (requiert `isTeacher=true`) |
| `permission:artisan:content` | `Artisan.CONTENT` | Publier des posts, gérer la galerie et les certifications |
| `permission:artisan:reviews` | `Artisan.REVIEWS` | Lire et répondre à ses propres avis |
| `permission:profile:read` | `Profile.READ` | Lire son propre profil et les profils publics |
| `permission:profile:write` | `Profile.WRITE` | Mettre à jour son propre profil |
| `permission:report:create` | `Report.CREATE` | Soumettre des signalements d'abus de contenu |
| `permission:file:read` | `File.READ` | Télécharger les fichiers protégés d'une formation |
| `permission:message:send` | `Message.SEND` | Envoyer et recevoir des messages |
| `permission:analytics:admin` | `Analytics.ADMIN` | Accéder au tableau de bord analytique et exporter des données |
| `permission:admin:catalog` | `Admin.CATALOG` | Gérer les taxonomies du catalogue : techniques, époques, régions, catégories, matériaux |
| `permission:client:favorites` | `Client.FAVORITES` | Gérer les artisans favoris des clients (ajout, liste, statut, retrait) |

### Prédicats de `AccessControlService`

| Méthode | Vérifie la permission |
|---|---|
| `isAdmin()` | `Admin.USERS` |
| `canManageUsers()` | `Admin.USERS` |
| `canManageFormations()` | `Admin.FORMATIONS` |
| `canModerateFeed()` | `Admin.FEED` |
| `canModerateReports()` | `Admin.REPORTS` |
| `canManageFinancialOperations()` | `Financial.ADMIN` |
| `canViewAnalytics()` | `Analytics.ADMIN` |
| `canManageCatalog()` | `Admin.CATALOG` |
| `canManageFavorites()` | `Client.FAVORITES` |
| `canManageArtisanFormations()` | `Artisan.FORMATIONS` |
| `canManageArtisanContent()` / `isArtisan()` | `Artisan.CONTENT` |
| `canManageArtisanReviews()` | `Artisan.REVIEWS` |
| `canReadProfile()` | `Profile.READ` |
| `canWriteProfile()` | `Profile.WRITE` |

### Endpoints d'attribution des permissions

| Endpoint | Méthode | Appelant |
|---|---|---|
| `GET /api/v1/admin/users/{userId}/permissions` | GET | Admin (`Admin.USERS`) |
| `POST /api/v1/admin/users/{userId}/permissions` | POST | Admin (`Admin.USERS`) |
| `DELETE /api/v1/admin/users/{userId}/permissions` | DELETE | Admin (`Admin.USERS`) |

---

## 6. Profils artisan et galerie

### Endpoints du profil artisan

<!-- AUTO-GENERATED depuis ArtisanController, ArtisanGalleryController, ArtisanCertificationController -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `GET /api/v1/artisan/{id}` | GET | Optionnelle | Récupérer le profil public d'un artisan par ID |
| `POST /api/v1/artisan/gallery` | POST (multipart) | `Artisan.CONTENT` | Télécharger une nouvelle image de galerie |
| `GET /api/v1/artisan/gallery` | GET | `Artisan.CONTENT` | Lister ses propres images de galerie |
| `PUT /api/v1/artisan/gallery/order` | PUT | `Artisan.CONTENT` | Réordonner les images de galerie |
| `DELETE /api/v1/artisan/gallery/{id}` | DELETE | `Artisan.CONTENT` | Supprimer une image de galerie (propriété vérifiée) |
| `POST /api/v1/artisan/certifications` | POST (multipart) | `Artisan.CONTENT` | Télécharger un document de certification (PDF/image) |
| `GET /api/v1/artisan/certifications` | GET | `Artisan.CONTENT` | Lister ses propres certifications |
| `DELETE /api/v1/artisan/certifications/{id}` | DELETE | `Artisan.CONTENT` | Supprimer une certification (propriété vérifiée) |

### Masquage des coordonnées

Les endpoints de profil appliquent une visibilité graduée des coordonnées selon la relation du consultant :

| Consultant | Visibilité téléphone / e-mail |
|---|---|
| Non authentifié / client non premium | Masqué (`null` ou censuré) |
| Client premium | Téléphone et e-mail complets révélés |
| Artisan (auto-consultation) | Complet |
| Admin | Complet |

### Limites galerie et certifications

| Paramètre | Défaut |
|---|---|
| Nombre max d'images en galerie | `ARTISAN_GALLERY_MAX_IMAGES=20` |
| Taille max d'un fichier de galerie | `ARTISAN_GALLERY_MAX_FILE_SIZE=10MB` |
| Types MIME autorisés galerie | `image/jpeg`, `image/png`, `image/webp` |
| Nombre max de certifications | `ARTISAN_CERTIFICATION_MAX_COUNT=10` |
| Taille max d'un fichier de certification | `ARTISAN_CERTIFICATION_MAX_FILE_SIZE=15MB` |
| Types MIME autorisés certification | `application/pdf`, `image/jpeg`, `image/png` |

---

## 7. Profils client et favoris

### Endpoints

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `GET /api/v1/client/profile` | GET | `Profile.READ` | Récupérer son propre profil client |
| `PATCH /api/v1/client/profile` | PATCH | `Profile.WRITE` | Mettre à jour son propre profil client |
| `POST /api/v1/client/favorites/artisans/{artisanId}` | POST | `Client.FAVORITES` | Ajouter un artisan aux favoris (201 Created ; 409 Conflict si doublon ou plafond atteint) |
| `GET /api/v1/client/favorites/artisans` | GET | `Client.FAVORITES` | Lister les artisans favoris (paginé, max 100, parité de masquage contact) |
| `GET /api/v1/client/favorites/artisans/{artisanId}/status` | GET | `Client.FAVORITES` | Vérifier si un artisan est dans les favoris (`{ "favorited": boolean }`) |
| `DELETE /api/v1/client/favorites/artisans/{artisanId}` | DELETE | `Client.FAVORITES` | Retirer un artisan des favoris (200 OK, `data: null` ; 404 si non favorisé) |

### Règles métier et garanties

- **Parité de confidentialité** : Les favoris sont strictement privés. Les artisans ne reçoivent aucune notification et ne voient pas qui les a ajoutés en favori. Les objets `ArtisanDirectoryCardDTO` retournés appliquent la confidentialité des contacts : les clients non-premium reçoivent un nom anonymisé (`"Artisan #XXXXX"`), identique à l'annuaire public, tandis que les clients premium reçoivent le nom complet démasqué.
- **Exigence d'un profil client** : En plus de détenir `permission:client:favorites`, l'appelant doit posséder un profil `Client` actif en base. Les appelants non-clients (ex. administrateurs) reçoivent `403 Forbidden` (`"Only registered clients can manage favorites."`).
- **Plafond configurable** : Le nombre maximal de favoris par client est contrôlé par `app.favorites.max-per-client` (variable `FAVORITES_MAX_PER_CLIENT`, défaut : `500`). Si la limite est atteinte, l'ajout échoue avec `409 Conflict` (`"Client favorite limit reached."`).
- **Verrouillage pessimiste de concurrence** : La vérification de capacité et l'insertion sont sérialisées avec un verrou pessimiste en écriture sur la ligne `Client`, évitant toute situation de concurrence. La contrainte d'unicité `(client_id, artisan_id)` intercepte les doublons et les traduit en `409 Conflict`.
- **Filtrage de visibilité** : La liste et le statut excluent automatiquement les comptes suspendus ou non visibles (`deletedAt IS NULL`, `accountStatus = ACTIVE`).
- **Architecture extensible** : Le modèle repose sur la classe abstraite `@MappedSuperclass` `ClientFavorite` avec le discriminateur `FavoriteType.ARTISAN`, permettant d'ajouter de futurs types de favoris sans impacter le code existant.

---

## 8. Catalogue et annuaire taxonomique

Tous les endpoints taxonomiques sont **publics** — aucune authentification requise.

<!-- AUTO-GENERATED depuis CatalogController -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `GET /api/v1/catalog/regions` | GET | Aucune | Arbre hiérarchique des régions (Algérie : racine DZ → 58 Wilayas → Communes) |
| `GET /api/v1/catalog/categories` | GET | Aucune | Hiérarchie de catégories d'artisanat avec sous-catégories imbriquées |
| `GET /api/v1/catalog/materials` | GET | Aucune | Liste des matières premières utilisées dans l'artisanat |
| `GET /api/v1/catalog/epoques` | GET | Aucune | Périodes historiques / époques de style |
| `GET /api/v1/catalog/techniques` | GET | Aucune | Techniques artisanales |

### Inventaire des taxonomies de référence

La plateforme initialise une taxonomie de référence algérienne et méditerranéenne complète :

- **Hiérarchie administrative géographique (`regions`)** :
  - Nœud pays racine : Algérie (`DZ`).
  - 58 Wilayas algériennes (d'Adrar à El Meniaa) avec codes administratifs et ordres d'affichage.
  - Communes imbriquées sous leur Wilaya parente respective.
- **Métiers artisanaux du bâtiment (`job_categories` & `job_sub_categories`)** :
  - 8 Catégories et 37 Sous-catégories (taxonomie française) :
    - *Gros œuvre & structure* (6 sous-catégories)
    - *Toiture & enveloppe du bâtiment* (5 sous-catégories)
    - *Électricité & énergie* (4 sous-catégories)
    - *Plomberie & systèmes techniques* (4 sous-catégories)
    - *Finitions & second œuvre* (5 sous-catégories)
    - *Menuiserie & agencement* (4 sous-catégories)
    - *Métal & serrurerie* (4 sous-catégories)
    - *Métiers du patrimoine* (5 sous-catégories)
- **Matériaux du bâtiment méditerranéen (`material_families` & `materials`)** :
  - 6 Familles et 25 Matériaux :
    - *Matériaux naturels traditionnels* (5 matériaux)
    - *Matériaux de maçonnerie* (4 matériaux)
    - *Matériaux de toiture* (4 matériaux)
    - *Métal & structure* (3 matériaux)
    - *Isolation & techniques modernes* (5 matériaux)
    - *Revêtements & finitions* (4 matériaux)
- **Époques historiques (`epoques`)** :
  - 14 périodes chronologiques (Antiquité, ères islamiques, régence ottomane, artisanat traditionnel et mouvements contemporains).
- **Techniques artisanales (`techniques`)** :
  - 20 méthodes et techniques artisanales traditionnelles et patrimoniales.

---

## 9. Recherche dans l'annuaire des artisans

<!-- AUTO-GENERATED depuis DirectoryController -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `GET /api/v1/public/directory` | GET | Aucune | Recherche plein texte + filtre facetté des profils artisans publiés |

### Paramètres de recherche

| Paramètre | Type | Description |
|---|---|---|
| `q` | `String` | Requête textuelle plein texte (correspondance floue via Elasticsearch) |
| `categorySlug` | `String` | Filtre par slug de catégorie d'artisanat |
| `subCategorySlug` | `String` | Filtre par slug de sous-catégorie |
| `wilayaCode` | `String` | Filtre par code de wilaya algérienne |
| `materialIds` | `List<UUID>` | Filtre par identifiants de matières |
| `techniqueIds` | `List<UUID>` | Filtre par identifiants de techniques |
| `epoqueIds` | `List<UUID>` | Filtre par identifiants d'époques |
| `verifiedOnly` | `Boolean` | Retourner uniquement les artisans vérifiés |
| `premiumOnly` | `Boolean` | Retourner uniquement les artisans de niveau premium |
| `teacherOnly` | `Boolean` | Retourner uniquement les formateurs accrédités |
| `sortBy` | `String` | `relevance` \| `name` \| `createdAt` \| `rating` |
| `page` | `int` | Numéro de page (indexé à 0, défaut 0) |
| `size` | `int` | Taille de page (défaut `DIRECTORY_DEFAULT_PAGE_SIZE=20`, max `DIRECTORY_MAX_PAGE_SIZE=100`) |

### Politique de pagination

| Paramètre | Défaut |
|---|---|
| Index de page par défaut | `DIRECTORY_DEFAULT_PAGE_INDEX=0` |
| Taille de page par défaut | `DIRECTORY_DEFAULT_PAGE_SIZE=20` |
| Taille de page minimale | `DIRECTORY_MIN_PAGE_SIZE=1` |
| Taille de page maximale | `DIRECTORY_MAX_PAGE_SIZE=100` — les requêtes au-delà sont plafonnées, jamais rejetées |

---

## 10. Formations et masterclasses

Les formations sont des masterclasses en présentiel ou hybrides, créées exclusivement par des artisans avec l'accréditation **formateur**.

### États du cycle de vie d'une formation

<!-- AUTO-GENERATED depuis src/main/java/com/project/souklab/model/FormationStatus.java -->

| État | Description |
|---|---|
| `DRAFT` | Nouvellement créée ; l'auteur peut encore modifier tous les champs |
| `PENDING_REVIEW` | Soumise pour révision admin ; aucune modification autorisée |
| `APPROVED` | Approuvée par l'admin ; prête à être publiée |
| `REJECTED` | Rejetée par l'admin avec retour ; l'auteur peut réviser et resoumettre |
| `PUBLISHED` | Visible publiquement ; inscriptions ouvertes |

### Statuts d'inscription

| Statut | Description |
|---|---|
| `CONFIRMED` | Inscription acceptée, session à venir |
| `ATTENDED` | Participant marqué comme ayant assisté — requis pour laisser un avis |
| `CANCELLED` | Inscription annulée (par le participant ou le système) |

### Endpoints de formation — Artisan formateur

<!-- AUTO-GENERATED depuis ArtisanFormationController, ArtisanFormationEnrollmentController -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `POST /api/v1/artisan/formations` | POST | `Artisan.FORMATIONS` + `isTeacher=true` | Créer une formation (démarre en `DRAFT`) |
| `PUT /api/v1/artisan/formations/{id}` | PUT | `Artisan.FORMATIONS` + propriétaire | Mettre à jour la formation (état DRAFT uniquement) |
| `POST /api/v1/artisan/formations/{id}/thumbnail` | POST (multipart) | `Artisan.FORMATIONS` + propriétaire | Télécharger ou remplacer la miniature de la formation |
| `POST /api/v1/artisan/formations/{id}/files` | POST (multipart) | `Artisan.FORMATIONS` + propriétaire | Télécharger un fichier de cours protégé (jusqu'à 10 fichiers) |
| `DELETE /api/v1/artisan/formations/{id}/files/{fileId}` | DELETE | `Artisan.FORMATIONS` + propriétaire | Supprimer un fichier de cours |
| `POST /api/v1/artisan/formations/{id}/submit` | POST | `Artisan.FORMATIONS` + propriétaire | Soumettre pour révision admin |
| `DELETE /api/v1/artisan/formations/{id}` | DELETE | `Artisan.FORMATIONS` + propriétaire | Suppression logique (états DRAFT/REJECTED uniquement) |
| `GET /api/v1/artisan/formations/me` | GET | `Artisan.FORMATIONS` | Lister ses propres formations avec filtres de statut |
| `GET /api/v1/artisan/formations/{id}` | GET | `Artisan.FORMATIONS` | Détail de sa propre formation |

### Endpoints de formation — Inscriptions (tout artisan)

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `GET /api/v1/artisan/formations/catalog` | GET | `Artisan.FORMATIONS` | Parcourir le catalogue des formations publiées |
| `GET /api/v1/artisan/formations/catalog/{id}` | GET | `Artisan.FORMATIONS` | Voir le détail d'une formation publiée |
| `POST /api/v1/artisan/formations/{id}/enroll` | POST | `Artisan.FORMATIONS` | S'inscrire à une formation |
| `POST /api/v1/artisan/formations/{id}/cancel` | POST | `Artisan.FORMATIONS` | Annuler sa propre inscription |
| `GET /api/v1/artisan/formations/my-enrollments` | GET | `Artisan.FORMATIONS` | Lister ses propres inscriptions |
| `GET /api/v1/artisan/formations/{id}/files/{fileId}/download` | GET | `File.READ` + inscrit ou propriétaire | Télécharger un fichier de cours protégé |

### Endpoints de formation — Admin

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `GET /api/v1/admin/formations/pending` | GET | `Admin.FORMATIONS` | Lister les formations en attente de révision |
| `POST /api/v1/admin/formations/{id}/review` | POST | `Admin.FORMATIONS` | Soumettre une décision de révision (APPROVED / REJECTED) |
| `POST /api/v1/admin/formations/{id}/publish` | POST | `Admin.FORMATIONS` | Publier une formation approuvée |

### Limites des formations

| Paramètre | Défaut |
|---|---|
| Taille max miniature | `FORMATION_THUMBNAIL_MAX_FILE_SIZE=10MB` |
| Types MIME autorisés miniature | `image/jpeg`, `image/png`, `image/webp` |
| Nombre max de fichiers de cours | `FORMATION_FILE_MAX_COUNT=10` |
| Taille max d'un fichier de cours | `FORMATION_FILE_MAX_FILE_SIZE=25MB` |
| Types MIME autorisés fichiers de cours | `application/pdf`, `image/jpeg`, `image/png` |
| Devise par défaut | `FORMATION_DEFAULT_CURRENCY=DZD` |
| Délai d'annulation | `FORMATION_CANCELLATION_DEADLINE_HOURS=24` (avant le début de la formation) |

### Règles métier

- Seuls les artisans avec `isTeacher=true` (formateurs accrédités) peuvent créer des formations.
- L'**auteur d'une formation ne peut pas s'y inscrire lui-même** — retourne `400 Bad Request`.
- L'**inscription en double** à la même formation retourne `409 Conflict`.
- La **capacité est strictement appliquée** — une inscription au-delà de `maxParticipants` retourne `409 Conflict`.
- Un avis ne peut être soumis que par un artisan avec une inscription `ATTENDED` sur cette formation.
- Les artisans non inscrits tentant de télécharger des fichiers protégés reçoivent `403 Forbidden` (et non `404`).
- Les auteurs de formation ont toujours accès aux fichiers protégés de leur propre formation.

---

## 11. Accréditation Formateur

Contrôle quels artisans peuvent créer des formations (masterclasses).

<!-- AUTO-GENERATED depuis ArtisanFormateurController, AdminFormateurController -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `POST /api/v1/artisan/formateur-request` | POST | `Artisan.CONTENT` | Soumettre une demande d'accréditation |
| `GET /api/v1/admin/formateur-requests` | GET | `Admin.FORMATIONS` | Lister les demandes d'accréditation en attente |
| `POST /api/v1/admin/formateur-requests/{id}/approve` | POST | `Admin.FORMATIONS` | Approuver la demande → définit `isTeacher=true` |
| `POST /api/v1/admin/formateur-requests/{id}/reject` | POST | `Admin.FORMATIONS` | Rejeter avec délai d'attente optionnel |
| `POST /api/v1/admin/artisans/{artisanId}/formateur-grant` | POST | `Admin.FORMATIONS` | Accorder directement le statut de formateur |
| `POST /api/v1/admin/artisans/{artisanId}/formateur-revoke` | POST | `Admin.FORMATIONS` | Révoquer le statut de formateur |
| `POST /api/v1/admin/formateur-requests/{artisanId}/lift-cooldown` | POST | `Admin.FORMATIONS` | Lever le délai d'attente après rejet |

### Machine à états de l'accréditation

```
Artisan soumet → PENDING
Admin approuve → APPROVED (isTeacher=true)
Admin rejette  → REJECTED (avec délai d'attente optionnel)
  Pendant le délai : nouvelle soumission → 403 Forbidden
  Admin lève le délai : nouvelle soumission réussie
Admin direct-grant : APPROVED (contourne le flux de demande)
Admin direct-revoke : isTeacher=false (les formations restent)
Admin blocage permanent (canReapply=false) : futures demandes → 403 Forbidden jusqu'à levée
```

### Délai d'attente de re-soumission

| Paramètre | Défaut |
|---|---|
| Délai d'attente de re-soumission formateur | `ARTISAN_FORMATEUR_REAPPLY_COOLDOWN_DAYS=14` |

---

## 12. Fil d'actualités social et posts

<!-- AUTO-GENERATED depuis FeedPostController, AdminFeedController -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `GET /api/v1/feed` | GET | `Profile.READ` | Fil paginé de posts publiés |
| `GET /api/v1/feed?authorId=&tag=&q=&sort=` | GET | Public | Filtre par auteur/tag, recherche texte et tri `latest`/`popular` |
| `GET /api/v1/feed/{id}` | GET | `Profile.READ` | Récupérer le détail d'un post |
| `GET /api/v1/feed/me` | GET | Authentifiée | Lister ses brouillons, posts en attente, rejetés et publiés |
| `GET /api/v1/feed/following` | GET | Client authentifié | Fil des artisans ajoutés aux favoris |
| `POST /api/v1/feed` | POST | `Artisan.CONTENT` | Créer un brouillon ou soumettre un nouveau post |
| `POST /api/v1/feed/{id}/submit` | POST | Propriétaire | Soumettre un brouillon ou un post rejeté |
| `PUT /api/v1/feed/{id}` | PUT | `Artisan.CONTENT` + propriétaire | Mettre à jour le texte/les tags d'un post |
| `DELETE /api/v1/feed/{id}` | DELETE | `Artisan.CONTENT` + propriétaire | Suppression logique de son propre post |
| `POST/DELETE /api/v1/feed/{id}/likes` | POST/DELETE | Authentifiée | Aimer/retirer son like, une seule fois par personne |
| `POST/DELETE /api/v1/feed/{id}/bookmarks` | POST/DELETE | Authentifiée | Ajouter/retirer un post des favoris |
| `GET /api/v1/feed/saved` | GET | Authentifiée | Lister ses posts enregistrés |
| `GET/POST /api/v1/feed/{id}/comments` | GET/POST | Public/authentifiée | Lire des commentaires racine publiquement ou en ajouter avec authentification |
| `GET/POST /api/v1/feed/comments/{commentId}/replies` | GET/POST | Public/authentifiée | Lire ou ajouter une réponse de niveau 1 |
| `POST/DELETE /api/v1/feed/comments/{commentId}/likes` | POST/DELETE | Authentifiée | Aimer/retirer son like d'un commentaire |
| `GET /api/v1/feed/comments/{commentId}/likes` | GET | Public | Lire l'état du like du commentaire pour l'appelant |
| `DELETE /api/v1/feed/comments/{commentId}` | DELETE | Authentifiée/modérateur | Supprimer logiquement un commentaire |
| `POST /api/v1/feed/{id}/share` | POST | Public | Incrémenter le compteur et retourner un chemin relatif |
| `POST /api/v1/feed/{id}/media` | POST (multipart) | `Artisan.CONTENT` + propriétaire | Attacher un média à un post |
| `DELETE /api/v1/feed/{id}/media/{mediaId}` | DELETE | `Artisan.CONTENT` + propriétaire | Supprimer un média d'un post |
| `POST /api/v1/admin/feed/{id}/publish` | POST | `Admin.FEED` | Admin : publier un post masqué |
| `POST /api/v1/admin/feed/{id}/reject` | POST | `Admin.FEED` | Admin : rejeter avec une note de modération |
| `POST /api/v1/admin/feed/{id}/hide` | POST | `Admin.FEED` | Admin : masquer un post |
| `POST /api/v1/admin/feed/{id}/remove` | POST | `Admin.FEED` | Admin : supprimer définitivement un post |
| `DELETE /api/v1/admin/feed/{id}` | DELETE | `Admin.FEED` | Suppression administrative canonique |

Les statuts sont `DRAFT`, `PENDING`, `PUBLISHED`, `REJECTED`, `HIDDEN` et `REMOVED`. Les tags sont normalisés et les likes de posts/commentaires sont uniques par utilisateur grâce aux contraintes de base de données. Les soumissions en attente notifient les administrateurs disposant de `Admin.FEED`.

### Permissions des posts du fil

| Action | Requise |
|---|---|
| Créer un post | `Artisan.CONTENT` + statut de compte `ACTIVE` + artisan vérifié |
| Modifier / supprimer son post | `Artisan.CONTENT` + vérification de propriété |
| Aimer / commenter / enregistrer / répondre | Utilisateur authentifié |
| Modération admin | `Admin.FEED` |

### Configuration des médias

| Paramètre | Défaut |
|---|---|
| Nombre max de médias par post | `FEED_MAX_MEDIA_PER_POST=10` |
| Types MIME d'images autorisés | `FEED_ALLOWED_IMAGE_MIME_TYPES=image/jpeg,image/png,image/webp` |

---

## 13. Avis

### Création d'avis — Chemin unique

> **Il existe exactement un chemin de création d'avis : `POST /api/v1/artisan/formations/{formationId}/reviews`**  
> Aucun mécanisme d'avis basé sur les conversations ou la messagerie n'existe.

<!-- AUTO-GENERATED depuis ArtisanReviewController -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `GET /api/v1/artisans/{artisanId}/reviews` | GET | Optionnelle | Lister les avis publics d'un artisan (paginé) |
| `POST /api/v1/artisan/formations/{formationId}/reviews` | POST | `Artisan.REVIEWS` | Créer un avis (requiert une inscription `ATTENDED` sur cette formation) |
| `PUT /api/v1/artisan/reviews/{reviewId}` | PUT | `Artisan.REVIEWS` + propriétaire | Mettre à jour son propre avis |
| `DELETE /api/v1/artisan/reviews/{reviewId}` | DELETE | `Artisan.REVIEWS` + propriétaire | Supprimer son propre avis |

### Règles métier des avis

- Le rédacteur doit avoir une inscription `ATTENDED` sur la formation spécifique dont il fait l'avis.
- Un avis en double sur la même inscription retourne `409 Conflict` (contrainte unique sur `enrollment_id`).
- Les avis sont liés à la note agrégée de l'artisan, qui se met à jour à la création/modification/suppression.
- Statut de l'avis : `PUBLISHED` (par défaut à la création).

---

## 14. Signalement de contenu et modération

<!-- AUTO-GENERATED depuis ContentReportController -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `POST /api/v1/reports` | POST | `Report.CREATE` | Soumettre un signalement d'abus |
| `GET /api/v1/admin/reports` | GET | `Admin.REPORTS` | Lister les signalements ouverts (admin) |
| `POST /api/v1/admin/reports/{id}/resolve` | POST | `Admin.REPORTS` | Résoudre un signalement avec décision et action |

### Cibles des signalements

Les signalements peuvent être soumis contre :
- Des posts du fil
- Des commentaires
- Des comptes utilisateurs

### Codes de motif des signalements

Les codes de motif standard incluent : `SPAM`, `HARASSMENT`, `INAPPROPRIATE_CONTENT`, `MISINFORMATION` et `OTHER`.

### Règles métier

- Un signalement en double contre la même entité par le même utilisateur retourne `409 Conflict`.
- La résolution admin peut inclure : rejeter (aucune action), supprimer le contenu signalé, suspendre/bannir l'utilisateur signalé.

---

## 15. Messagerie en temps réel (REST + WebSocket STOMP)

La messagerie fonctionne sur deux couches complémentaires : REST pour la récupération d'état et STOMP sur WebSocket pour la livraison en temps réel.

### Endpoints de conversation REST

<!-- AUTO-GENERATED depuis ConversationController -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `POST /api/v1/conversations` | POST | `Message.SEND` | Créer ou récupérer une conversation 1-à-1 |
| `GET /api/v1/conversations` | GET | `Message.SEND` | Lister ses propres conversations (paginé) |
| `PATCH /api/v1/conversations/{id}/archive` | PATCH | `Message.SEND` + participant | Archiver une conversation |
| `GET /api/v1/conversations/{id}/messages` | GET | `Message.SEND` + participant | Récupérer l'historique paginé des messages |
| `POST /api/v1/conversations/{id}/messages` | POST | `Message.SEND` + participant | Envoyer un message (fallback REST) |
| `PATCH /api/v1/conversations/{conversationId}/messages/{messageId}` | PATCH | `Message.SEND` + expéditeur | Modifier son propre message |
| `DELETE /api/v1/conversations/{conversationId}/messages/{messageId}` | DELETE | `Message.SEND` + expéditeur | Suppression logique de son propre message |
| `POST /api/v1/conversations/{id}/read` | POST | `Message.SEND` + participant | Marquer la conversation comme lue |
| `POST /api/v1/conversations/{id}/attachments` | POST (multipart) | `Message.SEND` + participant | Télécharger une pièce jointe de message |

### Protocole STOMP WebSocket

**Endpoint de connexion :** `ws://{host}/ws/websocket`  
**Authentification :** Inclure `Authorization: Bearer {access_token}` comme en-tête STOMP sur la trame CONNECT.

#### Destinations applicatives (Client → Serveur)

| Destination | Payload | Description |
|---|---|---|
| `/app/v1/conversations/{id}/messages.send` | `SendMessageRequest` | Envoyer un message |
| `/app/v1/conversations/{id}/messages.edit` | `EditMessageCommand` | Modifier un message existant |
| `/app/v1/conversations/{id}/messages.delete` | `MessageCommand` | Supprimer un message |
| `/app/v1/conversations/{id}/read` | `MessageCommand` | Envoyer un accusé de lecture |
| `/app/v1/conversations/{id}/typing.start` | `TypingCommand` | Diffuser début de frappe |
| `/app/v1/conversations/{id}/typing.stop` | `TypingCommand` | Diffuser fin de frappe |

#### Destinations utilisateur (Serveur → Client)

| Destination | Types d'événements | Description |
|---|---|---|
| `/user/queue/chat` | `ACKNOWLEDGED`, `READ_UP_TO`, payloads de messages | Accusés de réception et livraison de messages |
| `/user/queue/chat-events` | `EDIT`, `DELETE`, `TYPING_START`, `TYPING_STOP` | Mutations de messages et événements de présence |
| `/user/queue/notifications` | Payloads de notifications | Notifications système et applicatives |

> **Important :** S'abonner aux **trois** destinations pour recevoir la surface complète d'événements. Chacune gère une classe d'événements distincte.

### Configuration du chat

| Paramètre | Défaut |
|---|---|
| Longueur max d'un message | `CHAT_MESSAGE_MAX_LENGTH=4000` |
| Nombre max de pièces jointes par message | `CHAT_ATTACHMENT_MAX_COUNT=5` |
| Taille de page par défaut | `CHAT_DEFAULT_PAGE_SIZE=50` |
| Taille de page maximale | `CHAT_MAX_PAGE_SIZE=100` |
| Durée de vie du curseur | `CHAT_CURSOR_LIFETIME=24h` |
| Intervalle d'événements de frappe | `CHAT_TYPING_EVENT_INTERVAL=500ms` |

### Règles métier

- La messagerie à soi-même (conversation avec soi-même) retourne `400 Bad Request`.
- Un non-participant envoyant dans une conversation retourne `403 Forbidden`.
- Les messages supprimés logiquement sont exclus des réponses `GET /messages` ultérieures.
- La reconnexion après déconnexion restaure l'historique complet des messages sans livraison en double.
- Isolation des trames STOMP : l'utilisateur A ne reçoit jamais les trames de file privée de l'utilisateur B.

---

## 16. Notifications

<!-- AUTO-GENERATED depuis NotificationController, NotificationType -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `GET /api/v1/notifications` | GET | Bearer | Liste paginée de notifications (les plus récentes en premier) |
| `GET /api/v1/notifications/unread-count` | GET | Bearer | Nombre entier brut de notifications non lues |
| `PUT /api/v1/notifications/{id}/read` | PUT | Bearer + propriétaire | Marquer une notification individuelle comme lue |
| `PUT /api/v1/notifications/read-all` | PUT | Bearer | Marquer toutes ses notifications comme lues |
| `DELETE /api/v1/notifications/{id}` | DELETE | Bearer + propriétaire | Supprimer sa propre notification |

> **Important :** `GET /api/v1/notifications/unread-count` retourne le compte comme un **entier brut** dans le champ `data` (type `int64`) :  
> `{ "code": 200, "data": 5, "success": true }`  
> Il n'est **pas** encapsulé comme `{ "unreadCount": 5 }`.

### Types de notifications

<!-- AUTO-GENERATED depuis src/main/java/com/project/souklab/model/NotificationType.java -->

| Catégorie | Clé de type | Déclencheur |
|---|---|---|
| Compte | `ACCOUNT_VALIDATED` | L'admin approuve un compte |
| Compte | `ACCOUNT_REJECTED` | L'admin rejette un compte |
| Compte | `ACCOUNT_SUSPENDED` | L'admin suspend un compte |
| Compte | `ACCOUNT_REINSTATED` | L'admin réintègre un compte |
| Formation | `FORMATION_APPROVED` | L'admin approuve une formation soumise |
| Formation | `FORMATION_REJECTED` | L'admin rejette une formation soumise |
| Formation | `NEW_FORMATION` | Nouvelle formation correspondant aux intérêts d'un artisan |
| Message | `NEW_MESSAGE` | Nouveau message de chat reçu |
| Abonnement | `SUBSCRIPTION_RENEWED` | Abonnement renouvelé automatiquement |
| Abonnement | `SUBSCRIPTION_EXPIRED` | Abonnement expiré |
| Abonnement | `SUBSCRIPTION_REVOKED` | L'admin révoque l'abonnement |
| Abonnement | `SUBSCRIPTION_RENEWAL_REMINDER` | Rappel avant expiration |
| Abonnement | `SUBSCRIPTION_GRANT_MANUAL` | L'admin accorde manuellement un abonnement |
| Paiement | `PAYMENT_SUCCESS` | Paiement traité avec succès |
| Paiement | `PAYMENT_FAILED` | Échec du traitement du paiement |
| Checkout | `CHECKOUT_CREATED` | Session de checkout créée |
| Checkout | `CHECKOUT_CANCELED` | Session de checkout annulée |
| Remboursement | `REFUND_REQUEST_UNAVAILABLE` | Remboursement non applicable |
| Signalement | `REPORT_NEW` | Nouveau signalement de contenu reçu (admin) |
| Avis | `REVIEW_NEW` | Nouvel avis sur sa propre formation |
| Formateur | `FORMATEUR_REQUEST_SUBMITTED` | Demande d'accréditation soumise |
| Formateur | `FORMATEUR_APPROVED` | Accréditation approuvée |
| Formateur | `FORMATEUR_GRANTED` | Octroi direct de formateur par l'admin |
| Formateur | `FORMATEUR_REJECTED` | Accréditation rejetée |
| Formateur | `FORMATEUR_REVOKED` | Statut de formateur révoqué |

### Convention de propriété

L'accès à la notification d'un autre utilisateur retourne **`404 Not Found`** (isolation par portée de requête, non `403`). C'est le comportement documenté ; il diffère de l'accès aux fichiers de formation qui retourne `403` — les deux sont corrects pour leurs types de ressources respectifs.

---

## 17. Abonnements et paiements

### Niveaux d'abonnement

| Niveau | Description |
|---|---|
| `FREE` | Niveau par défaut pour tous les artisans |
| `PRO` | Niveau intermédiaire avec limites étendues de produits et formations |
| `PREMIUM` | Niveau supérieur avec droits maximaux, positionnement prioritaire dans l'annuaire et coordonnées client déverrouillées |

### Endpoint public des plans

`GET /api/v1/subscriptions/plans` — **Aucune authentification requise**

### Endpoints d'abonnement artisan

<!-- AUTO-GENERATED depuis SubscriptionAccountController, SubscriptionCheckoutController -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `GET /api/v1/subscriptions/plans` | GET | Aucune | Lister les plans d'abonnement disponibles |
| `GET /api/v1/subscriptions/current` | GET | Bearer | Récupérer son abonnement actuel |
| `GET /api/v1/subscriptions` | GET | Bearer | Lister l'historique de ses abonnements |
| `POST /api/v1/subscriptions/checkout` | POST | Bearer | Initier un checkout Chargily pour un plan |
| `POST /api/v1/subscriptions/{id}/renew` | POST | Bearer | Renouveler un abonnement existant |
| `POST /api/v1/subscriptions/{id}/cancel` | POST | Bearer | Annuler un abonnement |
| `GET /api/v1/payments` | GET | Bearer | Lister ses propres paiements |
| `GET /api/v1/payments/{id}` | GET | Bearer | Récupérer un paiement individuel |

### Endpoints d'abonnement admin

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `GET /api/v1/admin/subscriptions` | GET | `Financial.ADMIN` | Lister tous les abonnements |
| `GET /api/v1/admin/subscriptions/payments` | GET | `Financial.ADMIN` | Lister tous les paiements |
| `GET /api/v1/admin/subscriptions/webhooks` | GET | `Financial.ADMIN` | Lister les événements webhook |
| `POST /api/v1/admin/subscriptions/grant` | POST | `Financial.ADMIN` | Accorder manuellement un abonnement |
| `POST /api/v1/admin/subscriptions/{id}/revoke` | POST | `Financial.ADMIN` | Révoquer un abonnement |
| `POST /api/v1/admin/subscriptions/{id}/cancel` | POST | `Financial.ADMIN` | Admin : annuler un abonnement |
| `POST /api/v1/admin/subscriptions/{id}/correct-state` | POST | `Financial.ADMIN` | Corriger l'état d'un abonnement |
| `POST /api/v1/admin/subscriptions/payments/{id}/correct-state` | POST | `Financial.ADMIN` | Corriger l'état d'un paiement |
| `POST /api/v1/admin/payments/{id}/refund` | POST | `Financial.ADMIN` | Traiter un remboursement |
| `GET /api/v1/admin/subscription-plans` | GET | `Financial.ADMIN` | Lister les plans (vue admin) |
| `POST /api/v1/admin/subscription-plans` | POST | `Financial.ADMIN` | Créer un plan d'abonnement |
| `PUT /api/v1/admin/subscription-plans/{id}` | PUT | `Financial.ADMIN` | Mettre à jour un plan d'abonnement |
| `DELETE /api/v1/admin/subscription-plans/{id}` | DELETE | `Financial.ADMIN` | Supprimer un plan d'abonnement |

### Webhook Chargily

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `POST /api/v1/integrations/chargily/webhook` | POST | Vérification de signature | Recevoir les événements de paiement Chargily |

Les requêtes webhook avec une signature invalide sont rejetées avec `400 Bad Request`. Le traitement des `event_id` en double est idempotent.

### Configuration des abonnements

| Paramètre | Défaut |
|---|---|
| Devise | `SUBSCRIPTION_CURRENCY=DZD` |
| Décalages de rappel de renouvellement | `SUBSCRIPTION_REMINDER_OFFSETS=7,1` (jours avant expiration) |
| Intervalle du job de cycle de vie | `SUBSCRIPTION_LIFECYCLE_INTERVAL=1h` |
| Rétention idempotence checkout | `SUBSCRIPTION_CHECKOUT_IDEMPOTENCY_RETENTION=24h` |
| Rétention événements webhook | `SUBSCRIPTION_WEBHOOK_RETENTION=90d` |

---

## 18. Panneau d'administration et modération

### Gestion des utilisateurs

<!-- AUTO-GENERATED depuis UserManagementController -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `GET /api/v1/admin/users` | GET | `Admin.USERS` | Lister tous les utilisateurs avec filtres (rôle, statut) |
| `GET /api/v1/admin/users/pending` | GET | `Admin.USERS` | Lister les utilisateurs en attente (non vérifiés/approuvés) |
| `GET /api/v1/admin/users/audit-logs` | GET | `Admin.USERS` | Journal d'audit paginé des actions admin |
| `POST /api/v1/admin/users/{id}/approve` | POST | `Admin.USERS` | Approuver un compte en attente |
| `POST /api/v1/admin/users/approve-bulk` | POST | `Admin.USERS` | Approbation en masse des comptes en attente |
| `POST /api/v1/admin/users/{id}/ban` | POST | `Admin.USERS` | Bannir définitivement un utilisateur |
| `POST /api/v1/admin/users/{id}/timeout` | POST | `Admin.USERS` | Suspendre temporairement un utilisateur (avec expiration) |
| `POST /api/v1/admin/users/{id}/unban` | POST | `Admin.USERS` | Débannir ou lever une suspension |
| `GET /api/v1/admin/users/{userId}/permissions` | GET | `Admin.USERS` | Lister les permissions d'un utilisateur |
| `POST /api/v1/admin/users/{userId}/permissions` | POST | `Admin.USERS` | Attribuer des permissions à un utilisateur |
| `DELETE /api/v1/admin/users/{userId}/permissions` | DELETE | `Admin.USERS` | Supprimer des permissions d'un utilisateur |

### Règles métier

- Débannir un utilisateur déjà `ACTIVE` retourne `409 Conflict`.
- L'expiration du timeout est automatiquement reflétée dans `effectiveStatus` sans job cron — calculé à la requête.
- Des entrées de journal d'audit sont créées pour chaque action admin avec l'ID de l'acteur, le type d'action et l'horodatage.

### Gestion du catalogue et des taxonomies

<!-- AUTO-GENERATED depuis AdminCatalogController -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `POST /api/v1/admin/catalog/techniques` | POST | `Admin.CATALOG` | Créer une technique artisanale |
| `PUT /api/v1/admin/catalog/techniques/{id}` | PUT | `Admin.CATALOG` | Remplacer une technique |
| `PATCH /api/v1/admin/catalog/techniques/{id}` | PATCH | `Admin.CATALOG` | Mise à jour partielle d'une technique (statut/ordre) |
| `DELETE /api/v1/admin/catalog/techniques/{id}` | DELETE | `Admin.CATALOG` | Supprimer une technique (bloqué si liée à des artisans) |
| `POST /api/v1/admin/catalog/epoques` | POST | `Admin.CATALOG` | Créer une époque/période historique |
| `PUT /api/v1/admin/catalog/epoques/{id}` | PUT | `Admin.CATALOG` | Remplacer une époque |
| `PATCH /api/v1/admin/catalog/epoques/{id}` | PATCH | `Admin.CATALOG` | Mise à jour partielle d'une époque |
| `DELETE /api/v1/admin/catalog/epoques/{id}` | DELETE | `Admin.CATALOG` | Supprimer une époque (bloqué si liée à des artisans) |
| `POST /api/v1/admin/catalog/regions` | POST | `Admin.CATALOG` | Créer une région (wilaya ou commune) |
| `PUT /api/v1/admin/catalog/regions/{id}` | PUT | `Admin.CATALOG` | Remplacer une région (garde contre les cycles circulaires) |
| `PATCH /api/v1/admin/catalog/regions/{id}` | PATCH | `Admin.CATALOG` | Mise à jour partielle d'une région |
| `DELETE /api/v1/admin/catalog/regions/{id}` | DELETE | `Admin.CATALOG` | Supprimer une région (bloqué si elle a des sous-régions) |
| `POST /api/v1/admin/catalog/categories` | POST | `Admin.CATALOG` | Créer une catégorie de métier |
| `PUT /api/v1/admin/catalog/categories/{id}` | PUT | `Admin.CATALOG` | Remplacer une catégorie |
| `PATCH /api/v1/admin/catalog/categories/{id}` | PATCH | `Admin.CATALOG` | Mise à jour partielle d'une catégorie |
| `DELETE /api/v1/admin/catalog/categories/{id}` | DELETE | `Admin.CATALOG` | Supprimer une catégorie (bloqué si elle a des sous-catégories) |
| `POST /api/v1/admin/catalog/subcategories` | POST | `Admin.CATALOG` | Créer une sous-catégorie sous une catégorie parente |
| `PUT /api/v1/admin/catalog/subcategories/{id}` | PUT | `Admin.CATALOG` | Remplacer une sous-catégorie |
| `PATCH /api/v1/admin/catalog/subcategories/{id}` | PATCH | `Admin.CATALOG` | Mise à jour partielle d'une sous-catégorie |
| `DELETE /api/v1/admin/catalog/subcategories/{id}` | DELETE | `Admin.CATALOG` | Supprimer une sous-catégorie (bloqué si liée à des artisans) |
| `POST /api/v1/admin/catalog/material-families` | POST | `Admin.CATALOG` | Créer une famille de matériaux |
| `PUT /api/v1/admin/catalog/material-families/{id}` | PUT | `Admin.CATALOG` | Remplacer une famille de matériaux |
| `PATCH /api/v1/admin/catalog/material-families/{id}` | PATCH | `Admin.CATALOG` | Mise à jour partielle d'une famille de matériaux |
| `DELETE /api/v1/admin/catalog/material-families/{id}` | DELETE | `Admin.CATALOG` | Supprimer une famille de matériaux (bloqué si elle a des matériaux) |
| `POST /api/v1/admin/catalog/materials` | POST | `Admin.CATALOG` | Créer un matériau sous une famille parente |
| `PUT /api/v1/admin/catalog/materials/{id}` | PUT | `Admin.CATALOG` | Remplacer un matériau |
| `PATCH /api/v1/admin/catalog/materials/{id}` | PATCH | `Admin.CATALOG` | Mise à jour partielle d'un matériau |
| `DELETE /api/v1/admin/catalog/materials/{id}` | DELETE | `Admin.CATALOG` | Supprimer un matériau (bloqué si lié à des artisans) |

### Règles de gestion du catalogue

- Dérivation automatique du slug unique via `SlugUtils.toSlug(nom)` lorsque le champ `slug` est nul ou omis.
- Un slug en doublon retourne `409 Conflict`.
- Les relations hiérarchiques à deux niveaux (`JobCategory` / `JobSubCategory`, `MaterialFamily` / `Material`) empêchent la suppression d'entités parentes ayant des éléments enfants (`409 Conflict`).
- Les hiérarchies auto-référencées (`Region`) détectent et rejettent les cycles circulaires via CTE récursive (`422 Unprocessable Entity`).
- Toute modification invalide les caches Caffeine publics (`@CacheEvict`) et génère une entrée d'audit avec `AuditLogAction.CATALOG_ITEM_CREATED`, `UPDATED` et `DELETED`.

---

## 19. Avatars et gestion de fichiers

<!-- AUTO-GENERATED depuis AvatarController -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `POST /api/v1/users/me/avatars` | POST (multipart) | Bearer | Télécharger un nouvel avatar |
| `GET /api/v1/users/me/avatars` | GET | Bearer | Lister ses propres avatars |
| `DELETE /api/v1/users/me/avatars/{id}` | DELETE | Bearer | Supprimer un avatar |
| `PUT /api/v1/users/me/avatars/{id}/activate` | PUT | Bearer | Définir un avatar comme actif |

### Règles des avatars

| Paramètre | Défaut |
|---|---|
| Nombre max d'avatars par utilisateur | `AVATAR_MAX_PER_USER=10` — le 11e upload retourne `409 Conflict` |
| Types MIME autorisés | `AVATAR_ALLOWED_MIME_TYPES=image/jpeg,image/png,image/webp` |
| Limite de débit par utilisateur | `AVATAR_RATE_LIMIT_CAPACITY=5` requêtes par `AVATAR_RATE_LIMIT_REFILL_DURATION=1m` |

### Règles supplémentaires

- Supprimer l'avatar actuellement actif définit l'avatar actif à `null` sans en promouvoir automatiquement un autre.
- Supprimer un avatar non possédé ou inexistant retourne `404 Not Found`.
- Réactiver l'avatar déjà actif est idempotent (retourne `200 OK`).

---

## 20. Stockage de fichiers (MinIO / S3)

### Téléchargement de fichiers

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `GET /api/v1/files/{key}` | GET | `File.READ` | Télécharger un fichier stocké par clé de stockage |

### Pipeline de validation

Tous les fichiers téléchargés passent par :
1. **Vérification de taille** — appliquée au niveau de la servlet (`STORAGE_MAX_FILE_SIZE=15MB`).
2. **Liste blanche de types MIME** — vérifiée contre `STORAGE_ALLOWED_MIME_TYPES`.
3. **Inspection des octets magiques** — Apache Tika détecte le type de contenu réel indépendamment de l'en-tête `Content-Type` ou de l'extension du fichier. Un fichier renommé avec une mauvaise extension est rejeté.
4. **Analyse antivirus ClamAV** — les fichiers positifs EICAR sont rejetés. `STORAGE_VIRUS_SCAN_FAIL_OPEN` configurable contrôle le comportement quand ClamAV est inaccessible.

### Configuration du stockage de fichiers

| Paramètre | Défaut |
|---|---|
| Fournisseur de stockage | `STORAGE_PROVIDER=s3` (MinIO localement) |
| Endpoint S3 | `STORAGE_S3_ENDPOINT=http://localhost:9000` |
| Bucket S3 | `STORAGE_S3_BUCKET=souklab-files` |
| Taille max de fichier | `STORAGE_MAX_FILE_SIZE=15MB` |
| Types MIME autorisés (général) | `image/jpeg,image/png,application/pdf` |
| Analyse antivirus activée | `STORAGE_VIRUS_SCAN_ENABLED=false` (activer en production) |
| Hôte ClamAV | `STORAGE_VIRUS_SCAN_HOST=localhost` |
| Port ClamAV | `STORAGE_VIRUS_SCAN_PORT=3310` |
| Capacité de limite de débit | `STORAGE_RATE_LIMIT_CAPACITY=120` / `STORAGE_RATE_LIMIT_REFILL_DURATION=1m` |

---

## 21. Analytique et reporting

Le module analytique fournit un système basé sur des jobs pour calculer les KPIs de la plateforme avec des intervalles de temps configurables et des politiques de rétention.

<!-- AUTO-GENERATED depuis AnalyticsJobController -->

| Endpoint | Méthode | Auth | Description |
|---|---|---|---|
| `POST /api/v1/admin/analytics/rollups/jobs/rebuild` | POST | `Analytics.ADMIN` | Déclencher un job de reconstruction des rollups |
| `POST /api/v1/admin/analytics/rollups/jobs/backfill` | POST | `Analytics.ADMIN` | Déclencher un job de backfill historique |
| `GET /api/v1/admin/analytics/rollups/jobs/{id}` | GET | `Analytics.ADMIN` | Vérifier le statut d'un job de rollup |
| `POST /api/v1/admin/analytics/rollups/rebuild` | POST | `Analytics.ADMIN` | Reconstruction synchrone des rollups |
| `POST /api/v1/admin/analytics/rollups/backfill` | POST | `Analytics.ADMIN` | Backfill historique synchrone |
| `POST /api/v1/admin/analytics/jobs` | POST | `Analytics.ADMIN` | Soumettre un job de requête analytique |
| `GET /api/v1/admin/analytics/jobs/{id}` | GET | `Analytics.ADMIN` | Récupérer le statut d'un job analytique |
| `GET /api/v1/admin/analytics/jobs/{id}/result` | GET | `Analytics.ADMIN` | Récupérer le résultat d'un job |
| `GET /api/v1/admin/analytics/jobs/{id}/download` | GET | `Analytics.ADMIN` | Télécharger le résultat d'un job en CSV |
| `DELETE /api/v1/admin/analytics/jobs/{id}` | DELETE | `Analytics.ADMIN` | Supprimer un job et ses artefacts |

> Également accessible via `/api/v1/admin/stats/**` (mapping alias).

### Configuration analytique

| Paramètre | Défaut |
|---|---|
| Intervalles de temps supportés | `DAY,WEEK,MONTH,QUARTER` |
| Nombre max de lignes de résultats | `ANALYTICS_MAX_RESULT_ROWS=500` |
| Taille max des résultats | `ANALYTICS_MAX_RESULT_BYTES=10485760` (10 Mo) |
| Plage de requête maximale | `ANALYTICS_MAXIMUM_RANGE_DAYS=366` |
| Nombre max de buckets | `ANALYTICS_MAXIMUM_BUCKET_COUNT=500` |
| Rétention des événements bruts | `ANALYTICS_RAW_EVENT_RETENTION=90d` |
| Rétention des rollups | `ANALYTICS_ROLLUP_RETENTION=730d` |
| Rétention des jobs | `ANALYTICS_JOB_RETENTION=24h` |
| Rétention des exports | `ANALYTICS_EXPORT_RETENTION=24h` |
| Fuseau horaire métier | `ANALYTICS_BUSINESS_TIME_ZONE=Africa/Algiers` |
| Concurrence des jobs | `ANALYTICS_JOB_CONCURRENCY=2` |
| Préfixe de stockage des exports | `ANALYTICS_EXPORT_STORAGE_PREFIX=analytics/exports` |

---

## 22. Limitation du débit (Rate Limiting)

La limitation du débit est implémentée via **Bucket4j** (algorithme token bucket). Le stockage sous-jacent est configurable : `local` (en mémoire, par instance) ou `redis` (distribué, partagé entre instances).

### Niveaux de limitation du débit

<!-- AUTO-GENERATED depuis .env.example -->

| Couche | S'applique à | Capacité | Recharge |
|---|---|---|---|
| IP-based (public sensible) | `/api/v1/auth/login`, `/api/v1/auth/verify-email`, `/api/v1/auth/forgot-password` | 5 req | par 1 minute |
| Upload d'avatar (par utilisateur) | `POST /api/v1/users/me/avatars` | `AVATAR_RATE_LIMIT_CAPACITY=5` | `AVATAR_RATE_LIMIT_REFILL_DURATION=1m` |
| Téléchargement de fichiers (par IP) | `GET /api/v1/files/**` | `STORAGE_RATE_LIMIT_CAPACITY=120` | `STORAGE_RATE_LIMIT_REFILL_DURATION=1m` |
| Utilisateurs authentifiés (général) | Tous les endpoints authentifiés | `APP_RATE_LIMIT_USER_CAPACITY=120` | `APP_RATE_LIMIT_USER_REFILL_DURATION=PT1M` |
| Endpoints admin (par utilisateur) | `/api/v1/admin/**` | `RATE_LIMIT_ADMIN_USER_CAPACITY=60` | par 1 minute |
| Endpoints analytique (par utilisateur) | `/api/v1/admin/analytics/**` | `RATE_LIMIT_ANALYTICS_USER_CAPACITY=20` | par 1 minute |
| Téléchargement résultats analytique | Téléchargement résultats | `RATE_LIMIT_ANALYTICS_RESULTS_USER_CAPACITY=60` | par 1 minute |
| Export CSV (par utilisateur) | Téléchargement CSV | `RATE_LIMIT_CSV_USER_CAPACITY=10` | par 1 minute |
| Webhook (par utilisateur) | Endpoints webhook | `RATE_LIMIT_WEBHOOK_USER_CAPACITY=120` | par 1 minute |

### Réponse 429

Les requêtes dépassant les limites reçoivent :
- HTTP `429 Too Many Requests`
- En-tête `Retry-After: {secondes}` dérivé de l'estimation de recharge du bucket
- Enveloppe d'erreur JSON standard avec `errorCode`

---

## 23. Notifications par e-mail

La plateforme envoie des e-mails transactionnels via deux backends configurables :

| Paramètre | Rôle |
|---|---|
| `APP_EMAIL_USE_SMTP=true` | Utiliser SMTP local (défaut, adapté au développement) |
| `MAILERSEND_API_KEY` défini | Utiliser l'API REST MailerSend pour la production |

### Événements déclenchant l'envoi d'e-mails

- Code PIN de vérification d'adresse e-mail
- Code PIN de réinitialisation de mot de passe
- Approbation / rejet de compte
- Notification d'approbation / rejet de formation
- Rappels d'expiration d'abonnement (7 jours et 1 jour avant expiration)

### Configuration SMTP

| Paramètre | Défaut |
|---|---|
| Hôte SMTP | `SMTP_HOST=localhost` |
| Port SMTP | `SMTP_PORT=1025` |
| Authentification | `SMTP_AUTH=false` |
| STARTTLS | `SMTP_STARTTLS=false` |
| E-mail expéditeur | `MAILERSEND_SENDER_EMAIL=noreply@souklab.dz` |
| Nom expéditeur | `MAILERSEND_SENDER_NAME=Souklab` |

---

## 24. Infrastructure et déploiement

### Build Docker multi-étapes

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
# Exécute sous l'utilisateur non-root 'souklab'
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### Actuator et observabilité

| Endpoint | Rôle |
|---|---|
| `/actuator/health` | Vérification de santé (liveness / readiness) |
| `/actuator/prometheus` | Endpoint de scrape des métriques Prometheus |
| `APP_HEALTH_DEPENDENCY_TIMEOUT=2s` | Timeout pour les sondes de santé des dépendances |

### Pools de threads asynchrones

| Pool | Core | Max | File d'attente | Préfixe |
|---|---|---|---|---|
| Pool applicatif | 4 | 16 | 200 | `souklab-async-` |
| Pool workflow | 8 | 32 | 500 | `souklab-workflow-` |

### Cache applicatif

| Paramètre | Défaut |
|---|---|
| Expiration après écriture | `APP_CACHE_EXPIRE_AFTER_WRITE=60m` |
| Taille maximale du cache | `APP_CACHE_MAXIMUM_SIZE=1000` |

### CORS

| Paramètre | Défaut |
|---|---|
| Origines autorisées | `APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000` |

### OpenAPI / Swagger UI

| Paramètre | Défaut |
|---|---|
| Activé | `OPENAPI_ENABLED=true` |
| Chemin de la doc API | `OPENAPI_PATH=/v3/api-docs` |
| Chemin Swagger UI | `OPENAPI_SWAGGER_PATH=/swagger-ui.html` |

---

## 25. Conventions API

### Enveloppe de réponse

Toutes les réponses REST sont encapsulées dans une enveloppe standard `ApiResponse<T>` :

```json
{
  "code": 200,
  "success": true,
  "message": "Description de l'opération",
  "data": { ... },
  "errors": null
}
```

| Champ | Présent quand | Type |
|---|---|---|
| `code` | Toujours | Entier (miroir du statut HTTP) |
| `success` | Toujours | Booléen |
| `message` | Toujours | Chaîne |
| `data` | Réponses de succès | Quelconque (objet, tableau, primitif ou `null`) |
| `errors` | `422 Unprocessable Content` uniquement | `Map<String, String>` — nom du champ → message de validation |
| `errorCode` | Erreurs dérivées de `AppException` uniquement | Chaîne (code d'erreur domaine-spécifique) |

### Utilisation des codes HTTP

| Code HTTP | Signification |
|---|---|
| `200 OK` | GET, PUT, PATCH ou POST de changement d'état réussi |
| `201 Created` | Création de ressource réussie |
| `204 No Content` | Suppression réussie |
| `400 Bad Request` | État de requête invalide (ex. auto-conversation, annulation après délai) |
| `401 Unauthorized` | JWT manquant ou invalide / expiré |
| `403 Forbidden` | Authentifié mais permissions insuffisantes ou violation de propriété (fichiers de formation) |
| `404 Not Found` | Ressource introuvable ou accès délimité par propriété à la ressource d'un autre utilisateur (notifications) |
| `409 Conflict` | Entité en double ou état conflictuel |
| `413 Payload Too Large` | Fichier dépassant la limite de taille |
| `422 Unprocessable Content` | Échec de validation Bean — map `errors` incluse |
| `429 Too Many Requests` | Limite de débit dépassée — en-tête `Retry-After` inclus |
| `500 Internal Server Error` | Erreur serveur inattendue |

### Pagination

Les endpoints paginés retournent :

```json
{
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalPages": 5,
    "totalElements": 98
  }
}
```

- `page` est indexé à 0.
- Les valeurs `page` négatives sont plafonnées à `0`.
- `size` au-dessus du maximum configuré est plafonné (ne retourne jamais `422`).
- `page` au-delà de la dernière page retourne un `content: []` vide (jamais `404`).
- Taille de page par défaut : `PAGEABLE_DEFAULT_SIZE=20`, max : `PAGEABLE_MAX_SIZE=100`.

---

## 26. Référence des variables d'environnement

<!-- AUTO-GENERATED depuis .env.example -->

### Serveur principal

| Variable | Obligatoire | Défaut | Description |
|---|---|---|---|
| `PORT` | Non | `8080` | Port du serveur HTTP |
| `COMPOSE_PROJECT_NAME` | Non | `souklab` | Nom du projet Docker Compose |

### Base de données (MariaDB)

| Variable | Obligatoire | Défaut | Description |
|---|---|---|---|
| `DB_NAME` | Oui | `souklab_db` | Nom de la base de données |
| `DB_URL` | Oui | `jdbc:mariadb://localhost:3306/...` | URL de connexion JDBC |
| `DB_USERNAME` | Oui | — | Nom d'utilisateur de la base de données |
| `DB_PASSWORD` | Oui | — | Mot de passe de la base de données |
| `DB_ROOT_PASSWORD` | Oui (Docker) | — | Mot de passe root MariaDB (Docker uniquement) |
| `JPA_HIBERNATE_DDL_AUTO` | Non | `update` | Utiliser `validate` en production avec Flyway |
| `DB_POOL_MAX_SIZE` | Non | `20` | Taille maximale du pool HikariCP |
| `FLYWAY_ENABLED` | Non | `false` | Activer les migrations Flyway |

### JWT et sécurité

| Variable | Obligatoire | Défaut | Description |
|---|---|---|---|
| `APP_JWT_SECRET` | **Oui** | — | Secret de signature HS256 — minimum 32 caractères |
| `APP_JWT_ACCESS_EXP` | Non | `3600000` | Expiration du token d'accès en millisecondes (1 heure) |
| `APP_JWT_REFRESH_EXP` | Non | `86400000` | Expiration du token de rafraîchissement en millisecondes (24 heures) |
| `AUTH_LOCKOUT_MAX_ATTEMPTS` | Non | `5` | Tentatives de connexion échouées avant verrouillage |
| `AUTH_LOCKOUT_DURATION_MINUTES` | Non | `15` | Durée du verrouillage |
| `AUTH_VERIFICATION_MAX_ATTEMPTS` | Non | `5` | Tentatives max de vérification PIN e-mail |
| `AUTH_VERIFICATION_EXPIRATION_MINUTES` | Non | `15` | Expiration du PIN e-mail |

### Stockage de fichiers (MinIO / S3)

| Variable | Obligatoire | Défaut | Description |
|---|---|---|---|
| `STORAGE_PROVIDER` | Non | `s3` | Backend de stockage (`s3` uniquement supporté) |
| `STORAGE_S3_ENDPOINT` | Oui (S3) | `http://localhost:9000` | Endpoint MinIO / S3 |
| `STORAGE_S3_BUCKET` | Oui (S3) | `souklab-files` | Nom du bucket S3 |
| `STORAGE_S3_ACCESS_KEY` | Oui (S3) | — | Clé d'accès S3 |
| `STORAGE_S3_SECRET_KEY` | Oui (S3) | — | Clé secrète S3 |
| `STORAGE_MAX_FILE_SIZE` | Non | `15MB` | Taille maximale d'upload |
| `STORAGE_VIRUS_SCAN_ENABLED` | Non | `false` | Activer l'analyse ClamAV |
| `STORAGE_VIRUS_SCAN_FAIL_OPEN` | Non | `false` | Autoriser les uploads si ClamAV inaccessible |

### E-mail

| Variable | Obligatoire | Défaut | Description |
|---|---|---|---|
| `APP_EMAIL_USE_SMTP` | Non | `true` | Utiliser SMTP au lieu de MailerSend |
| `SMTP_HOST` | Oui (SMTP) | `localhost` | Hôte du serveur SMTP |
| `SMTP_PORT` | Non | `1025` | Port du serveur SMTP |
| `MAILERSEND_API_KEY` | Oui (MailerSend) | — | Clé API MailerSend |
| `MAILERSEND_SENDER_EMAIL` | Non | `noreply@souklab.dz` | Adresse e-mail de l'expéditeur |

### Paiements (Chargily)

| Variable | Obligatoire | Défaut | Description |
|---|---|---|---|
| `CHARGILY_API_KEY` | Oui (Paiements) | — | Clé API Chargily |
| `CHARGILY_SECRET_KEY` | Oui (Paiements) | — | Secret webhook Chargily |
| `CHARGILY_ENABLED` | Non | `false` | Activer l'intégration Chargily |
| `SUBSCRIPTION_ENABLED` | Non | `false` | Activer le système d'abonnements |
| `SUBSCRIPTION_CURRENCY` | Non | `DZD` | Devise de facturation des abonnements |

### Favoris clients

| Variable | Obligatoire | Défaut | Description |
|---|---|---|---|
| `FAVORITES_MAX_PER_CLIENT` | Non | `500` | Nombre maximum d'artisans favoris par client (`app.favorites.max-per-client`) |

### STOMP / RabbitMQ

| Variable | Obligatoire | Défaut | Description |
|---|---|---|---|
| `RELAY_HOST` | Oui (WS) | `localhost` | Hôte du relais STOMP RabbitMQ |
| `RELAY_PORT` | Non | `61613` | Port STOMP RabbitMQ |
| `RELAY_CLIENT_LOGIN` | Oui | — | Login client STOMP |
| `RELAY_CLIENT_PASSCODE` | Oui | — | Code d'accès client STOMP |

### Elasticsearch / Hibernate Search

| Variable | Obligatoire | Défaut | Description |
|---|---|---|---|
| `HIBERNATE_SEARCH_ENABLED` | Non | `true` | Activer la recherche plein texte |
| `ELASTICSEARCH_URIS` | Oui (Recherche) | `http://localhost:9200` | Endpoint Elasticsearch |
| `SEARCH_SYNC_ON_STARTUP` | Non | `true` | Resynchroniser l'index de recherche au démarrage |

---

## 27. Migrations de base de données

Flyway gère tous les changements de schéma. Les migrations sont **immuables** — les migrations appliquées ne doivent jamais être modifiées.

<!-- AUTO-GENERATED depuis src/main/resources/db/migration/ -->

| Version | Fichier | Description |
|---|---|---|
| `V0` | `V0__baseline_schema.sql` | Schéma de base complet — utilisateurs, artisans, formations, inscriptions, catalogue, etc. |
| `V1` | `V1__phase7_social.sql` | Tables du fil social : posts, likes, commentaires, médias |
| `V2` | `V2__authorization_permissions.sql` | Système de permissions : table `authorization_permission` |
| `V3` | `V3__phase8_messaging.sql` | Messagerie en temps réel : conversations, messages, pièces jointes |
| `V4` | `V4__production_query_indexes.sql` | Index de performance des requêtes en production |
| `V5` | `V5__phase9_subscriptions_payments.sql` | Abonnements et paiements : plans, abonnements, paiements, webhooks, remboursements |
| `V6` | `V6__phase10_analytics.sql` | Analytique : événements bruts, rollups, file de jobs, résultats |
| `V7` | `V7__phase10_analytics_outbox.sql` | Pattern outbox des événements analytiques |
| `V8` | `V8__phase10_analytics_audit_action.sql` | Actions du journal d'audit pour l'analytique |
| `V9` | `V9__phase10_analytics_artifacts.sql` | Stockage des artefacts d'export analytique |
| `V10` | `V10__phase10_payment_origin.sql` | Traçage de l'origine des paiements |
| `V11` | `V11__phase10_outbox_retry_schedule.sql` | Colonnes de planification des tentatives outbox |
| `V12` | `V12__phase10_analytics_maintenance_jobs.sql` | Suivi des jobs de maintenance analytique |
| `V13` | `V13__phase10_report_resolution_time.sql` | Suivi du temps de résolution des signalements |
| `V14` | `V14__phase10_financial_audit_actions.sql` | Actions du journal d'audit financier |
| `V15` | `V15__admin_catalog_permission.sql` | Permission de gestion du catalogue taxonomique (`permission:admin:catalog`) et types d'actions d'audit associées |
| `V16` | `V16__client_favorites.sql` | Table des artisans favoris des clients (`client_favorite_artisans`), contrainte d'unicité, cascades FK, index et permission (`permission:client:favorites`) |
| `V17` | `V17__feed_social_enhancements.sql` | Statuts de feed, tags normalisés, likes, favoris, commentaires/réponses, compteurs et signalements de commentaires |

---

## 28. Registre de vérification

Cette section enregistre les campagnes de vérification live menées contre l'instance Souklab en fonctionnement.

### Campagne de vérification des favoris clients — 2026-09-24

| Composant | Cible / Suite | Total | Réussis | Échoués | Statut |
|---|---|:---:|:---:|:---:|:---:|
| Tests unitaires et de slice | `GlobalExceptionHandlerTest`, `ArtisanControllerTest` | 9 | 9 | 0 | **100 % Réussi** |
| Intégration et concurrence | `ClientFavoriteArtisanIntegrationTest` | 11 | 11 | 0 | **100 % Réussi** |
| Scénarios d'intégration live | `test_favorites.py` (cURL/REST) | 47 | 47 | 0 | **100 % Réussi** |
| Comptage des requêtes SQL | Assertions de statement count (5, 20, 60 éléments) | 4 | 4 | 0 | **Aucun N+1 (Batch size 50)** |

### Campagne de vérification live — 2026-09-22

| Domaine | Script de test | Total | Réussis | Échoués | Taux de réussite |
|---|---|:---:|:---:|:---:|:---:|
| Auth et identité | `scripts/verify-domain1-auth.py` | 64 | 64 | 0 | **100 %** |
| Profils artisan et client | `scripts/verify-domain2-profiles.py` | 45 | 45 | 0 | **100 %** |
| Catalogue et annuaire | `scripts/verify-domain3-catalog.py` | 60 | 60 | 0 | **100 %** |
| Formations et masterclasses | `scripts/verify-domain4-formations.py` | 57 | 57 | 0 | **100 %** |
| Fil social, avis et signalements | `scripts/verify-domain5-feed-reports.py` | 80 | 80 | 0 | **100 %** |
| Messagerie et WebSocket | `scripts/verify-domain6-messaging.py` | 70 | 70 | 0 | **100 %** |
| Notifications | `scripts/verify-domain7-notifications.py` | 36 | 36 | 0 | **100 %** |
| Abonnements et plans | `scripts/verify-domain8-subscriptions.py` | 37 | 37 | 0 | **100 %** |
| Administration et modération | `scripts/verify-domain9-admin.py` | 36 | 36 | 0 | **100 %** |
| Avatars et stockage de fichiers | `scripts/verify-domain10-storage.py` | 41 | 41 | 0 | **100 %** |
| Limitation du débit | `scripts/verify-domain11-ratelimit.py` | 40 | 40 | 0 | **100 %** |
| **TOTAL** | | **566** | **566** | **0** | **100,0 %** |

### Suite de tests Maven — 2026-09-22

```
Tests run: 1228, Failures: 0, Errors: 0, Skipped: 10
BUILD SUCCESS
Total time: 02:27 min
```

### Corrections de code appliquées lors de la vérification

| Fichier | Description de la correction |
|---|---|
| `GlobalExceptionHandler.java` | Ajout de `@ExceptionHandler({InvalidDataAccessApiUsageException.class, PropertyReferenceException.class})` pour mapper les paramètres de tri/requête invalides vers 400 Bad Request `INVALID_PARAMETER` |
| `FormationIntegrationTest.java` | Mise à jour des assertions de tableau de contenu de l'index fixe `content[0].id` vers Hamcrest `hasItem(formationId)` pour gérer les données de test préexistantes |
| `AvatarService.java` | Ajout de `@Transactional` sur les méthodes publiques pour éviter `TransactionRequiredException` lors de l'auto-invocation via proxy Spring |
| `RateLimitFilter.java` | Ajout de l'en-tête `Retry-After` sur les réponses 429, dérivé de l'estimation de recharge Bucket4j |
| `AvatarUploadRateLimitFilter.java` | Même correction `Retry-After` |
| `FileRateLimitFilter.java` | Même correction `Retry-After` |
| `UserRateLimitFilter.java` | Même correction `Retry-After` |
| `AuthService.java` | Ajout de `@Transactional` sur la méthode `logout` |
| `ConversationService.java` | Protection contre les objets de pièces jointes nuls/vides lors de la création de messages |
| `FeedPostService.java` | Garantie d'attribution d'ID des médias avant la persistance des médias de post |

---

*Généré par Antigravity à partir de l'arborescence live du code source.*

---

*Généré par Antigravity depuis l'arbre source live. Source de vérité : annotations `@RequestMapping` des contrôleurs, énumérations des modèles, `.env.example`, `docker-compose.yml` et `pom.xml`.*
