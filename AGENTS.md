# Agent instructions
- Keep this repository generic; do not add OrzMC gameplay, RealTime behavior, production endpoints, secrets, or organization-specific IDs.
- `OrzMC/RealTime` and `OrzMC/OrzMCPlugin` are read-only references. Never modify either repository, branch, issue, release, workflow, or setting without explicit maintainer approval.
- If a reference project appears to require a change, stop and report evidence, exact files, rationale, impact, alternatives, and consequences of not changing it. Wait for approval.
- `plugin-core` must not depend on Bukkit/Paper. `plugin-paper` owns platform integration.
- Prefer constructor injection and explicit composition; no DI framework without a demonstrated need.
- Every acquired resource needs an owner and shutdown path. Do not claim Folia support before dedicated scheduling/thread-safety work and real verification.
- Read architecture and roadmap docs before changing boundaries. Add tests and run `./gradlew clean check build`. Never weaken tests just to get green CI.
