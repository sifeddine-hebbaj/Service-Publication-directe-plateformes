# Service Publication Directe Plateformes

## Présentation
Ce projet est une application Spring Boot permettant la publication directe d'offres d'emploi sur LinkedIn, avec stockage en base de données PostgreSQL. Il expose une API REST pour recevoir les offres et les publier sur LinkedIn via l'API officielle.

## Architecture

```
+-------------------+        +-------------------+        +-------------------+
|   Client (API)    | <----> | Spring Boot App   | <----> |   LinkedIn API    |
+-------------------+        +-------------------+        +-------------------+
                                   |
                                   v
                          +-------------------+
                          |  PostgreSQL DB    |
                          +-------------------+
```

- **Controller** : Point d'entrée REST (`JobPublicationController`)
- **Service** : Logique métier, publication LinkedIn, gestion des tokens (`JobPublicationService`)
- **Entity/DTO/Mapper** : Modélisation et conversion des offres d'emploi
- **Repository** : Accès base de données (Spring Data JPA)
- **Security** : Configuration de la sécurité (Spring Security)

## Technologies principales
- Java 17
- Spring Boot 3.5+
- Spring Web, Spring Data JPA, Spring Security, WebFlux
- PostgreSQL
- Lombok

## Configuration

Fichier `src/main/resources/application.properties` :
- Paramètres LinkedIn (client id/secret, redirect URI, scope, organization URN)
- Connexion PostgreSQL (`spring.datasource.*`)

## Flux principal : Publication d'une offre sur LinkedIn

1. **Requête POST** sur `/api/job-offers/linkedin/publish` avec un JSON :
   ```json
   {
     "title": "Titre du poste",
     "description": "Description du poste",
     "location": "Ville, Pays",
     "company": "Nom de l'entreprise"
   }
   ```
   et les headers :
   - `Authorization: Bearer <access_token LinkedIn>`
   - `X-Linkedin-User-Urn: urn:li:person:...`

2. **Contrôleur** :
   - Récupère le token et l'URN utilisateur
   - Appelle le service pour publier sur LinkedIn
   - Stocke l'offre (avec le texte LinkedIn) en base
   - Retourne un message de succès ou d'erreur

3. **Service** :
   - Construit le texte du post LinkedIn
   - Appelle l'API LinkedIn via WebClient
   - Gère les erreurs éventuelles
   - Sauvegarde l'offre en base

4. **Base de données** :
   - Table `job_offer` (id, title, description, location, company, published, linkedinPostText)

## Sécurité
- L'endpoint `/api/job-offers/linkedin/publish` est ouvert (pas d'authentification Spring Security, mais nécessite un token LinkedIn valide)
- Les autres endpoints sont protégés par défaut

## Lancer le projet
1. Installer PostgreSQL et créer la base `nexotek_rh`
2. Adapter les paramètres dans `application.properties` si besoin
3. Lancer l'application :
   ```bash
   ./mvnw spring-boot:run
   ```
4. Tester l'API avec Postman ou curl

## Exemple de requête curl
```bash
curl -X POST http://localhost:8080/api/job-offers/linkedin/publish \
  -H "Authorization: Bearer <access_token>" \
  -H "X-Linkedin-User-Urn: urn:li:person:xxxx" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Développeur Java",
    "description": "CDI, 3 ans d'expérience",
    "location": "Paris, France",
    "company": "Nexotek RH"
  }'
```

## Structure des dossiers
- `Controller/` : Contrôleur REST
- `Service/` : Logique métier
- `Entity/` : Entités JPA
- `DTOs/` : Objets de transfert
- `Mapper/` : Conversion DTO <-> Entité
- `Repository/` : Accès base
- `Security/` : Sécurité

## Remarques
- Le stockage du token LinkedIn est simplifié (à améliorer pour la production)
- L'API LinkedIn nécessite des droits spécifiques et un token utilisateur valide
- Le projet peut être étendu pour d'autres plateformes (Indeed, etc.) 