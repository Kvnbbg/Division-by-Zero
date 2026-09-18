# Division-by-Zero — Audit des axes d’amélioration

## Périmètre

Audit statique du dépôt public `Kvnbbg/Division-by-Zero` à partir du README, de l’arborescence, du frontend TDAAH, du backend Spring Boot, des tests et de la documentation existante.

Cet audit ne prétend pas remplacer une exécution CI ou un test de charge.

## Constats

### 1. Architecture déjà bien séparée

Le dépôt possède une séparation claire entre :

- web ;
- domaine ;
- API ;
- pipeline ;
- sécurité ;
- audit ;
- documentation.

**Action :** conserver cette structure.

### 2. Contrôle frontend plus strict que le pipeline

Le moteur JS rejette explicitement les valeurs non finies et les résultats non représentables. `SafeRatioTransform` Java doit adopter la même politique.

**Action :** créer un utilitaire numérique partagé côté Java.

### 3. Parité à formaliser

Les règles existent à plusieurs endroits. Elles doivent être testées avec une matrice commune.

**Action :** définir un corpus JSON de cas numériques et l’utiliser côté JS + Java.

### 4. Contrat API à renforcer

Le endpoint `/v1/exercise-attempts` est clair mais les erreurs gagneraient à utiliser Problem Details.

**Action :** standardiser les réponses 4xx/5xx.

### 5. Parser volontairement minimal

Le parser est compact et lisible, mais il ne faut pas lui attribuer les propriétés d’un moteur CAS complet.

**Action :** documenter explicitement le DSL accepté et refuser les constructions ambiguës.

### 6. Historique uniquement navigateur

L’historique visible est en mémoire dans `app.js`.

**Action :** décider explicitement si l’historique est une fonctionnalité de session ou une fonctionnalité persistante. Ne pas ajouter une base de données simplement pour stocker huit lignes locales.

### 7. Accessibilité

La surface HTML est minimaliste, mais les résultats dynamiques doivent être annoncés aux technologies d’assistance.

**Action :** `aria-live`, focus management et tests clavier.

### 8. Observabilité

Le backend possède déjà audit et Actuator dans la feuille de route.

**Action :** terminer le socle Micrometer + correlation ID avant d’ajouter des fonctions produit.

## Ordre recommandé

1. **Finite/representable parity**
2. **Corpus de tests partagé**
3. **Problem Details**
4. **OpenAPI**
5. **Micrometer + correlation**
6. **Accessibilité**
7. **Documentation utilisateur**
8. **Fonctions produit supplémentaires**

## Anti-régressions

Toute modification du moteur numérique doit tester simultanément :

- valeur valide ;
- zéro ;
- quasi-zéro ;
- NaN ;
- Infinity ;
- overflow ;
- dimensions incompatibles ;
- conversion avec offset.

Toute modification API doit tester :

- 200 ;
- 400 ;
- 401/403 si sécurité activée ;
- 422 ;
- 500 ;
- structure JSON d’erreur.

