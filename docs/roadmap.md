# Roadmap
## P1 — Minimal baseline
- [x] Java 25, Gradle Kotlin DSL, multi-module structure
- [x] Core lifecycle contract and rollback-capable module manager
- [x] Traditional plugin.yml and minimal Brigadier command
- [x] JUnit lifecycle tests and CI workflow
- [x] Add Gradle Wrapper 9.2.1 and verify clean build in GitHub Actions
- [ ] Verify plugin startup and command on a real Paper 26.3 server ([acceptance procedure](real-server-acceptance.md))
- [ ] Confirm repository LICENSE with maintainer
## P2 — Runtime
- [ ] Typed config, validation and migration
- [ ] Listener ownership and cleanup
- [ ] Permission conventions and command helpers
- [ ] Scheduler port and Paper adapter
- [ ] Folia global/region/entity/async adapters and real smoke tests
- [ ] Only then consider folia-supported: true
## P3 — Quality
- [ ] MockBukkit integration tests
- [ ] Real Paper smoke automation, JAR inspection, secret/dependency scanning
- [ ] Nightly compatibility checks
## P4 — Initialization and release
- [ ] Parameterized project/package/command identity and dry-run initializer
- [ ] GitHub Release; optional Hangar/Modrinth publish
## P5 — Safe evolution
- [ ] Template manifest, upgrade notes and migration checks
- [ ] Agent-assisted upgrade PRs only; never overwrite business code
- [ ] Validate upgrades in a generated sample plugin
No business code from RealTime or OrzMCPlugin is included. No SDK/Build Logic split or Folia support claim in P1.
