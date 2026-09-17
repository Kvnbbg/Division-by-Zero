# Enterprise control plane — Spring Boot + native C helpers

© 2026 Kevin Marville · [techandstream.com](https://techandstream.com) · [kvnbbg.fr](https://kvnbbg.fr) · [kvnbbg-creations.io](https://kvnbbg-creations.io)

**Scope:** authorized IT operations for enterprise and daily employee devices.  
**Out of scope:** unauthorized access, exploit chains, privilege escalation, or bypass of security controls.

## Goal

A **control plane** that employees and IT can use every day:

- Spring Boot (Java 21) for APIs, auth, tickets, audit, batch jobs
- Small **C** helpers for fast, local OS checks (package managers, device presence, bus inventory)
- Named failures only (TDAAH style): no silent success, no silent “Infinity”

## Layers

```
┌─────────────────────────────────────────────────────────┐
│  Clients (employee laptop, IT console, CI)              │
└───────────────────────────┬─────────────────────────────┘
                            │ HTTPS + MFA / API key
┌───────────────────────────▼─────────────────────────────┐
│  Spring Boot control plane (tdaah-agent)                │
│  · /v1/agent/tools                                      │
│  · /v1/pipeline/*                                       │
│  · /v1/device/inventory  (authorized)                   │
│  · /v1/policy/usb        (read policy, not bypass)      │
│  · Actuator health / metrics                            │
└───────────────────────────┬─────────────────────────────┘
                            │ process spawn / JNI / CLI
┌───────────────────────────▼─────────────────────────────┐
│  Native C helpers (tools/)                              │
│  · ai_bootstrap.c     — install curl via apt/brew/...   │
│  · bus_inventory.c    — list PCI/USB *presence* (read)  │
│  · No shell injection of untrusted input                │
└─────────────────────────────────────────────────────────┘
```

## What “BUS control” means here

| Term | Authorized meaning | Not allowed |
|---|---|---|
| **USB / PCI inventory** | Read device IDs present on a managed host for support | Force-open locked devices, steal sessions |
| **Policy** | Publish “allowed USB classes” for MDM | Disable antivirus / EDR |
| **Deep / quick** | Low-latency local C checks + short Spring batch jobs | Network penetration without ticket |

Enterprise “control” = **inventory + policy + audit**, under least privilege, with tickets.

## Security rules (non-negotiable)

1. **Authorization first** — every mutating endpoint requires a named account + role (`IT_OPERATOR`, not anonymous).
2. **No secret in Git** — API keys and MDM tokens only via env / vault.
3. **Allowlisted native commands** — C helpers run fixed binaries/paths; never `system(userString)`.
4. **Audit** — who ran what, when, on which host id (hashed if needed).
5. **Fail closed** — unknown device class → deny; zero-division style named refusal.
6. **Employee daily use** — self-service: “is my VPN agent healthy?”, “is curl present?”, not “open the server room door”.

## Spring Boot packages (target)

```
fr.kvnbbg.tdaah
  api/          DeviceController, PolicyController (read-mostly)
  pipeline/     existing batch readers/writers
  native/       NativeBridge → ProcessBuilder to tools/*.c binaries
  audit/        AuditEvent (who, action, result)
```

## C helpers (target)

| Binary | Role | Privilege |
|---|---|---|
| `ai_bootstrap` | Ensure `curl` via package manager | User or sudo only when package install is intentional |
| `bus_inventory` | Print USB/PCI *presence* summary | Unprivileged read of sysfs where OS allows |

Build example:

```bash
cc -O2 -o tools/bus_inventory tools/bus_inventory.c
cc -O2 -o tools/ai_bootstrap tools/ai_bootstrap.c
```

## Daily employee flows

1. Open internal portal → “Device health”.
2. Control plane calls local agent or returns last inventory snapshot.
3. If `curl` missing, show link to run `ai_bootstrap` **with user consent**.
4. USB unknown device → ticket to IT, not auto-disable of security tools.

## Relation to TDAAH pipeline

Batch jobs remain **short-lived**: start → transform → write → exit.  
Native checks feed the pipeline as **FILE/API sources**, never as hidden backdoors.

See also: [PIPELINE.md](../PIPELINE.md), [Copernicus instructions](../Instructions/Instruction_utilisation_Copernicus.pdf).
