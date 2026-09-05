# tdaah-agent — Spring Boot

Conversion de mesures dimensionnées, et refus explicite de la division par zéro.

Voir `docs/ROADMAP-AGENT-SPRING-BOOT.md`.

Pas de secrets, pas de fichiers audio, pas de logique NFT ici.

## Construire et tester

Java 21.

```
mvn test
mvn spring-boot:run
```

Si Maven répond « No compiler is provided in this environment », c'est que
`JAVA_HOME` pointe sur un JRE (ici `/opt/java/jre1.8.0_491`) et non sur un JDK.
Un JDK 21 est installé à côté :

```
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test
```

## Outils exposés

Le manifeste `/v1/agent/tools` n'annonce que ce qui répond réellement. Il
annonçait auparavant cinq outils dont aucun n'était servi : un agent qui en
appelait un recevait un 404.

| Outil | Appel |
| --- | --- |
| `convert_measurement` | `POST /v1/agent/convert` — `{"value":1,"from":"km","to":"m"}` |
| `list_units` | `GET /v1/agent/units` |
| `explain_dimension` | `GET /v1/agent/dimension/{unit}` |
| `refuse_zero_division` | `GET /v1/agent/ratio?value=10&unit=m&per=0&perUnit=m` |

`evaluate_expression` — l'évaluateur d'expressions dimensionnées de
`web/js/convertisseur.js` — n'est pas encore porté. Il est donc absent du
manifeste plutôt que promis.

## Parité avec le JavaScript

`src/main/java/fr/kvnbbg/tdaah/domain/Units.java` est un portage de
`web/js/convertisseur.js` : mêmes unités, mêmes facteurs, mêmes décalages, même
nettoyage numérique. Deux implémentations d'un même calcul qui divergent, c'est
un bug qui n'apparaît qu'en production, du côté que personne ne regarde.

Vérifié sur les mêmes entrées, au dernier chiffre près :

```
1 km -> m       = 1000
0 C -> K        = 273.15
0 C -> F        = 32
1 kWh -> J      = 3600000
1 psi -> Pa     = 6894.757293168361
100 km/h -> m/s = 27.77777777777778
36 unités, « m » en tête
```

## Le refus

L'invariant tient en une phrase : **aucune valeur non finie ne sort de `Units`.**
Ni `Infinity`, ni `NaN`, ni un zéro « par commodité ».

Trois portes, et non une seule — c'est l'élargissement de ce correctif :

| Cas | Refus | Code |
| --- | --- | --- |
| Dénominateur nul **en SI** | `ZeroDivisionMeasurementException` | `422` |
| Résultat qui déborde (`ratio`, `convert`) | `ZeroDivisionMeasurementException` | `422` |
| Valeur d'entrée `NaN` / `Infinity` | `UnitException` | `400` |

Nuance qui compte : `0 °C` n'est pas une mesure nulle — c'est 273,15 K — et le
rapport existe donc. Le refus porte sur la valeur **en SI**, pas sur celle qui a
été saisie.

Pourquoi `422` pour un débordement : la demande est recevable, le résultat
n'existe pas. Un `500` laisserait croire à une panne du service. Et pourquoi
`400` pour une entrée non finie : c'est la saisie qui est fautive, pas la
question — `AgentController.asDouble()` s'appuie sur `Double.parseDouble()`, qui
accepte volontiers les chaînes `"NaN"` et `"Infinity"`.

Le piège que ce correctif ferme : `clean()` rend **délibérément** les valeurs non
finies telles quelles, et ne peut donc pas servir de dernière ligne de défense.
Tant que le refus était écrit à la main dans `ratio()` seul, `convert()` rendait
`Infinity` sans que rien ne l'arrête — et Jackson le sérialisait en la *chaîne*
JSON `"Infinity"`, si bien qu'un agent appelant recevait du texte là où son
schéma annonçait un nombre. Le refus est désormais concentré dans
`requireFinite()` et `requireRepresentable()` : une opération nouvelle qui
oublierait de les appeler est un oubli visible, pas un trou silencieux.

## Tests

Les deux implémentations doivent refuser dans les mêmes cas, donc les deux
suites se lancent :

```
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test   # 16 tests, depuis agent-spring-boot/
npm test                                                # 12 tests, depuis la racine
```
