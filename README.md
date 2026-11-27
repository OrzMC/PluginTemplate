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
Requires JDK 25+; run `./gradlew clean check build`. Output JAR: `plugin-paper/build/libs/`.

## Verify
Copy the JAR to a Paper 26.3 server's `plugins/` directory and run `/template`.

See [docs/architecture.md](docs/architecture.md) and [docs/roadmap.md](docs/roadmap.md). No implementation code has been copied from RealTime or OrzMCPlugin; both remain read-only references. The template repository LICENSE decision is pending maintainer confirmation.
