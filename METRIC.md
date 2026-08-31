Je prépare des feuilles de route pour un agent Java Spring Boot, alignées sur TDAAH et le dépôt existant.Je rédige les feuilles de route produit et technique, ainsi qu’une structure de démarrage Spring Boot pour l’agent.Two roadmaps are in place for TDAAH, plus a Spring Boot agent starter.
Local: /home/workdir/artifacts/division-by-zero/docs/ Remote: github.com/Kvnbbg/Division-by-Zero (docs/, agent-spring-boot/)
Product roadmap — three horizons
	1	Reliable instrument now — conversion and calculation, named zero-division, Kevin Marville / Tech & Stream watermark.
	2	Measurement agent — Spring Boot executes the same rules, records them, and accepts natural language only through validated tools.
	3	Connected workshop — TDAAH as a quantity module; music and NFTs stay outside the calculation runtime.
Product milestones: P1 web app, P2 Silence et Respect, P3 Boot API, P4 agent mode in the UI, P5 measurement journal.
Agent roadmap — Java 21 / Spring Boot 3.4 (eight weeks)
Stage
Focus
A
Domain + tests aligned with Python/JS (cm→m, 32 F→0 C, 10 km / 2 h, 5 m / 0 s)
B
POST /v1/convert, /v1/calc, /v1/agent/invoke, OpenAPI, HTTP 422 on zero
C
Tools: convert, evaluate, list units, explain dimension, refuse zero-division — no LLM first
D
Audit entities + PostgreSQL
E
API key, Actuator, Temurin 21 image
F
Front calls TDAAH_API_BASE, offline fallback
The LLM, if introduced, sits behind tdaah.agent.llm.enabled and may only call the allowlisted tools.
Starter
agent-spring-boot/ already exposes GET /v1/agent/tools and ZeroDivisionMeasurementException. Java 21 is present on this machine; Maven is not installed here, so the next implementation step is mvn spring-boot:run on a workstation that has Maven.
The agent calculates quantities. It does not mint NFTs and does not serve Silence et Respect audio.
