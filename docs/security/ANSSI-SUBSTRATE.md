# Socle administratif & ANSSI (références publiques)

© 2026 Kevin Marville · [techandstream.com](https://techandstream.com) · [kvnbbg.fr](https://kvnbbg.fr) · [kvnbbg-creations.io](https://kvnbbg-creations.io)

Document **public**. Ne contient aucun secret, aucun plan de salle, aucun compte.

## Position

TDAAH / Copernicus s’alignent sur des **pratiques d’hygiène** défendues par l’ANSSI et le référentiel administratif français, sans se substituer à un audit officiel ni recopier les guides ANSSI in extenso.

## Principes repris (synthèse)

| Principe | Application TDAAH |
|---|---|
| Moindre privilège | Rôles `EMPLOYEE`, `IT_OPERATOR`, `AUDITOR` — pas d’admin par défaut |
| Authentification forte | API key rotative **ou** OIDC (IdP entreprise) ; MFA côté IdP |
| Traçabilité | Journal d’audit immuable côté app : qui / quoi / quand / ticket |
| Cloisonnement | dev ≠ test ≠ préprod ≠ prod |
| Mises à jour | Fenêtre de maintenance + retour arrière |
| Secrets hors code | Vault / variables d’env, jamais dans Git |
| Mode ticket | Toute action sensible exige un `ticketId` ; pas de « thruster » silencieux |

## Références publiques (cliquables)

- ANSSI — [https://cyber.gouv.fr](https://cyber.gouv.fr)
- ANSSI — guides et bonnes pratiques : [https://cyber.gouv.fr/publications](https://cyber.gouv.fr/publications)
- ANSSI — recommandations mots de passe / authentification (voir publications à jour sur cyber.gouv.fr)
- CNIL — [https://www.cnil.fr](https://www.cnil.fr) (données personnelles, registres)
- NIST Cybersecurity Framework (repère international) — [https://www.nist.gov/cyberframework](https://www.nist.gov/cyberframework)
- OWASP ASVS / API Security — [https://owasp.org](https://owasp.org)

Toujours vérifier la **version en vigueur** sur le site de l’émetteur.

## Substrat administratif interne (checklist)

1. Politique d’accès nominative + révocation au départ.
2. Registre des traitements si données perso (CNIL).
3. Procédure d’incident (constat → confinement → analyse → retour).
4. Conservation des logs d’admin (durée définie par la politique locale).
5. Interdiction de contourner EDR / MDM « pour aller plus vite ».

## Mode ticket (no thruster)

```
Demande → Ticket ouvert → Autorisation de rôle → Exécution → AuditEvent → Ticket clôturé
```

Sans `ticketId` sur une opération mutante : **refus nommé** (HTTP 422 / 403), jamais un succès silencieux.
