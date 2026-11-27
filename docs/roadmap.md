# Roadmap

## P1 — Minimal baseline

- [x] Java 25, Gradle Kotlin DSL, multi-module structure
- [x] Core lifecycle contract and rollback-capable module manager
- [x] Traditional plugin.yml and minimal Brigadier command
- [x] JUnit lifecycle tests and CI workflow
- [x] Add Gradle Wrapper 9.2.1 and verify clean build in GitHub Actions
- [x] Verify plugin startup and command on a real Paper 26.3 server ([acceptance procedure](real-server-acceptance.md), [record](real-server-acceptance.md#final-verdict))
- [x] Confirm repository LICENSE with maintainer (MIT; merged to main)

## P2 — Runtime

P2.0 架构设计见 [docs/p2-runtime-design.md](p2-runtime-design.md)。建议顺序：资源所有权/生命周期 → 类型化配置 → Listener 与命令 → Paper Scheduler →（有独立证据后）Folia。

- [x] P2.1 Module-owned ResourceScope、逆序清理、启动回滚和关闭异常隔离（PR #4；CI Run #41）
- [x] P2.2 Typed config、校验和 schema v0→v1 migration（PR #6；CI Run #45）
- [x] P2.3 Listener ownership、permission nodes 和 /template reload（PR #8；CI Run #49）
- [x] P2.4 Paper main-thread/async one-shot scheduler、TaskHandle 状态机和 ResourceScope 取消（PR #9；CI Run #53）
- [ ] P2.5 Folia global/region/entity/async adapters and real smoke tests
- [ ] Only then consider folia-supported: true

## P3 — Quality

- [ ] MockBukkit integration tests
- [x] Real Paper smoke runner + EULA-gated GitHub Actions job; five real Paper smoke runs passed (CI Runs #61–#65)
- [x] Daily latest-STABLE Paper 26.3 workflow implemented; first scheduled execution pending ([nightly workflow](../.github/workflows/nightly-paper.yml), [operations](nightly-paper.md))
- [x] At least 5 successful real Paper smoke runs; startup timeout, command-response timeout, EULA denial and forced process cleanup covered by regression tests (PR #14/#15)
- [ ] Secret/dependency scanning

## P4 — Initialization and release

- [ ] Parameterized project/package/command identity and dry-run initializer
- [ ] GitHub Release; optional Hangar/Modrinth publish

## P5 — Safe evolution

- [ ] Template manifest, upgrade notes and migration checks
- [ ] Agent-assisted upgrade PRs only; never overwrite business code
- [ ] Validate upgrades in a generated sample plugin

No business code from RealTime or OrzMCPlugin is included. No SDK/Build Logic split or Folia support claim in P1–P2.
