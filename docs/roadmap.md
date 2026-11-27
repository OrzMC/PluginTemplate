# Roadmap
## P1 — Minimal baseline
- [x] Java 25, Gradle Kotlin DSL, multi-module structure
- [x] Core lifecycle contract and rollback-capable module manager
- [x] Traditional plugin.yml and minimal Brigadier command
- [x] JUnit lifecycle tests and CI workflow
- [x] Add Gradle Wrapper 9.2.1 and verify clean build in GitHub Actions
- [x] Verify plugin startup and command on a real Paper 26.3 server ([acceptance procedure](real-server-acceptance.md), [record](real-server-acceptance.md#final-verdict))
- [x] Confirm repository LICENSE with maintainer (MIT; merged to main)
## P2 — Runtime\n\nP2.0 架构设计草案见 [docs/p2-runtime-design.md](p2-runtime-design.md)。建议顺序：资源所有权/生命周期 → 类型化配置 → Listener 与命令 → Scheduler Paper 适配 →（有证据后）Folia。GitHub Actions Paper smoke 设计见 [父仓库文档](https://github.com/OrzMC/OrzMC/blob/main/docs/plugin-template/github-actions-smoke.md)。
- [x] Module-owned ResourceScope, reverse-order cleanup, startup rollback and shutdown failure isolation (PR #4; CI Run #41)\n- [x] Typed config, validation and schema v0→v1 migration (P2.2 implementation PR; CI pending)
- [ ] Listener ownership and cleanup (will use module ResourceScope)
- [ ] Permission conventions and command helpers
- [ ] Scheduler port and Paper adapter (task handles will be registered in module ResourceScope)
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
