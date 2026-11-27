# OrzMC PluginTemplate

Reusable Agent-friendly Paper plugin template.

- Java 25, Gradle Kotlin DSL
- `plugin-core`: platform-independent Java
- `plugin-paper`: Paper adapter and plugin entry point
- Traditional `plugin.yml` descriptor
- Paper Lifecycle API + Brigadier command
- Lifecycle-managed modules with startup rollback and reverse shutdown
- JUnit 5 and GitHub Actions CI
- Folia support is not declared in this baseline

Paper API target is centralized in `gradle.properties` (26.3).

## Build

Requires JDK 25+:

```bash
./gradlew --no-daemon clean check build
bash scripts/inspect-jar.sh
```

Output JAR: `plugin-paper/build/libs/OrzMCPluginTemplate-0.1.0-SNAPSHOT.jar`.

## Runtime acceptance

The local-agent handoff and isolated Paper 26.3 runtime acceptance procedure are documented in [docs/real-server-acceptance.md](docs/real-server-acceptance.md). A local coding agent should execute that procedure after cloning the repository and record observed results there. Never use or modify the production OrzMC server for this test.

See [docs/architecture.md](docs/architecture.md) and [docs/roadmap.md](docs/roadmap.md). No implementation code has been copied from RealTime or OrzMCPlugin; both remain read-only references. The template repository LICENSE decision is pending maintainer confirmation.
