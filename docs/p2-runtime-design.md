# P2 Runtime Architecture Design

> Status: P2.0 design baseline; P2.1 ResourceScope/lifecycle implementation merged
> Updated: 2026-11-27
> Scope: PluginTemplate only. `OrzMC/RealTime` and `OrzMC/OrzMCPlugin` remain read-only references.

## 1. Goals and non-goals

P2 adds reusable runtime infrastructure without turning the template into a large framework.

Goals:

- Keep Core free of Bukkit/Paper/Folia classes.
- Make every runtime resource owned by a module and deterministically closed.
- Load typed configuration transactionally, with validation and schema migration.
- Keep command registration in the Paper adapter while delegating behavior to application code.
- Express scheduler thread/region semantics explicitly; never expose a misleading generic `runSync`.
- Test Core behavior with JUnit, Paper integration with MockBukkit, and runtime behavior in an isolated real server.

Non-goals:

- No database, message bus, DI container, generic event bus, or plugin SDK.
- No automatic migration of either reference project.
- No Folia support claim until the dedicated implementation and real Folia evidence exist.
- No generalized command DSL or scheduler API beyond demonstrated template needs.

## 2. Module ownership and dependency direction

```text
plugin-paper
  ├── JavaPlugin composition root
  ├── Paper config / event / command / scheduler adapters
  └── Paper logging and diagnostics
          │
          ▼
plugin-core
  ├── PluginModule + ModuleManager
  ├── ResourceScope / ResourceHandle
  ├── typed settings + validation + migration contracts
  └── platform-neutral scheduler ports and value types
```

Rules:

1. `plugin-core` must not import `org.bukkit.*`, `io.papermc.paper.*`, or MockBukkit.
2. `plugin-paper` owns all platform object lifetimes and converts them to Core value types.
3. The `JavaPlugin` entry point remains a thin composition root.
4. No additional Gradle module is introduced in P2 unless a concrete cross-project reuse case exists.

## 3. Resource ownership and module lifecycle

### Proposed contract

- Each module receives a `ModuleContext` containing its logger, required services and a module-owned `ResourceScope`.
- A module registers every acquired resource with its scope: listener registration, scheduled task, executor handle, file watcher or other closeable.
- `ResourceScope.close()` closes resources in reverse registration order, attempts every close, and aggregates failures.
- `ModuleManager` starts modules in declaration order and stops them in reverse order.
- If `start()` fails, the manager closes the failing module's scope and then rolls back already-started modules.
- `stop()` remains idempotent. A module's explicit stop logic and its scope cleanup must both be attempted even if one fails.

### Platform adapter example

`PaperEventRegistrar` registers a Bukkit listener and returns a Core-compatible `ResourceHandle`. Closing that handle unregisters that exact listener. The listener is not globally unregistered by unrelated modules.

Do not make Core aware of Bukkit's `Listener` or `HandlerList`; only the Paper adapter uses those types.

### Tests

- Close order is reverse registration order.
- A failing close does not prevent later resources from closing.
- Repeated close is safe.
- A module that fails halfway through startup still releases its partially acquired resources.
- Manager rollback continues if a module's cleanup throws.

## 4. Typed configuration

### Proposed pipeline

```text
config.yml
   │
   ▼
PaperYamlConfigStore (plugin-paper)
   │ immutable Map<String, Object>
   ▼
ConfigMigrator (plugin-core)
   │ current schema map
   ▼
PluginSettingsDecoder + Validator (plugin-core)
   │ validated immutable PluginSettings
   ▼
AtomicReference<PluginSettings>
```

Responsibilities:

- `PaperYamlConfigStore`: read/write YAML and copy the bundled default resource. It is the only layer using Paper/Bukkit configuration classes.
- `ConfigMigrator`: pure transformations between explicit schema versions; no file I/O.
- `PluginSettingsDecoder`: map primitive values into Java records and report path-specific type/range/enum errors.
- `ConfigService`: orchestrate load, migration, validation, persistence and atomic publication.

### Required behavior

1. Add an explicit `schema-version` field.
2. Missing configuration is initialized from the bundled default.
3. Migrations run in memory first; the migrated result is validated before replacing the on-disk file.
4. Before persisting a migration, preserve the original file as a versioned backup.
5. Reload parses and validates a complete candidate before swapping the active immutable settings reference.
6. If parsing, migration or validation fails, retain the previous active configuration unchanged.
7. Unknown keys are warnings in the first iteration, not silently discarded. Strict mode can be considered later.
8. Error messages include file, key path, invalid value category and correction guidance; do not dump secrets.

### Deliberately deferred

- Live file watching.
- Arbitrary third-party config formats.
- YAML-to-SQLite conversion.
- Per-module configuration directories before a real example needs them.

### Tests

Default generation, missing keys/defaults, wrong types, bounds, enum parsing, unknown keys, each migration edge, backup creation, invalid reload preserving the old active settings.

## 5. Commands and permissions

Commands remain a Paper-layer concern:

- Register with the existing traditional `plugin.yml` + Paper Lifecycle API + Brigadier approach.
- Keep command tree and Bukkit sender adaptation inside `plugin-paper`.
- Command handlers call Core use cases; Core never receives `CommandSender`, `Player`, `Location` or Brigadier objects.
- Define permission nodes centrally and document them alongside the root command.
- Provide small helpers for permission checks and player-only/console-only constraints only when they remove repeated adapter code.
- Keep user-facing messages and argument parsing explicit; do not introduce a command annotation framework or reflection scanner.
- Register command lifecycle at the plugin composition root; do not let feature modules independently register duplicate roots.

P2.3 should first add a minimal permission-aware example subcommand and tests, not a generic command framework.

## 6. Scheduler port and thread semantics

A single generic `runSync` is prohibited because it cannot express Folia's ownership rules.

### Proposed operations

Expose narrow ports with platform-neutral arguments:

- `executeGlobal(task)`: global coordination only; not arbitrary access to region-owned world state.
- `executeRegion(position, task)`: work associated with a specific world/block position.
- `executeEntity(entityId, task)`: work associated with one entity and its entity scheduler.
- `executeAsync(task)`: file, network and pure computation only; no Bukkit/Paper world/entity access.

Core value types should be small immutable Java values such as `WorldPosition(worldId, x, y, z)` and `EntityId(uuid)`. No Bukkit objects cross the port.

Each operation returns a `TaskHandle` with idempotent cancellation and observable completion/cancellation state. Tasks are registered in the owning module's `ResourceScope`; module shutdown cancels outstanding work.

The first implementation should cover one-shot execution and cancellation. Delayed/repeating APIs should be added only after an actual feature demonstrates required tick/time semantics; do not conflate Minecraft ticks with wall-clock `Duration`.

### Adapter rules

- Paper adapter maps the port to Paper's supported scheduler APIs and documents its execution context.
- Folia adapter maps global/region/entity/async operations to their matching Folia schedulers.
- Entity retirement or missing targets must complete/cancel the handle deterministically.
- Async callbacks must return to the correct global/region/entity scheduler before touching platform state.
- Exceptions are logged with task identity and owner; one task failure must not corrupt the registry.

### Folia release gate

Do not set `folia-supported: true` until all are true:

1. Global/region/entity/async adapters implemented.
2. Task cancellation, entity retirement, module shutdown and exception paths tested.
3. Platform-object access reviewed for thread ownership.
4. Real Folia server smoke passes on the declared target.
5. Paper and Folia both pass the same plugin lifecycle and command acceptance suite.

A Paper smoke pass is not Folia evidence.

## 7. Testing and CI contract

| Layer | Tool | Required evidence |
|---|---|---|
| Core | JUnit 5 | Resource scope, config decoder/migration, scheduler-port behavior |
| Paper adapter | MockBukkit | Plugin composition, command/permission, listener registration and cleanup |
| Real Paper | GitHub Actions + disposable Paper process | Enable, command response, disable, normal process exit |
| Real Folia | GitHub Actions + disposable Folia process | Added only after scheduler implementation; separate evidence |

The Paper smoke design is documented in the parent repository at [github-actions-smoke.md](https://github.com/OrzMC/OrzMC/blob/main/docs/plugin-template/github-actions-smoke.md).

The real-server job uses GitHub-hosted runners, no self-hosted runner, no production network, no RCON and no long-lived credentials. It must remain gated until a maintainer explicitly accepts the Minecraft EULA and sets the repository variable `MINECRAFT_EULA_ACCEPTED=true`.

PR build/unit tests remain available regardless of the EULA gate. Nightly latest-stable compatibility checks are separate from the pinned PR smoke.

## 8. Implementation sequence

| Step | Scope | Exit condition |
|---|---|---|
| P2.0 | This architecture and CI design | Maintainer review of config, ownership, command and scheduler contracts |
| P2.1 | ResourceScope + lifecycle hardening | **Completed**: PR #4 merged; CI Run #41 passed, including rollback and cleanup tests |
| P2.2 | Typed configuration and migration | Next: transactional load/reload and migration tests |
| P2.3 | Listener ownership + command/permission helpers | Registration cleanup and command integration tests pass |
| P2.4 | Scheduler ports + Paper adapter | Fake tests and real Paper smoke pass |
| P2.5 | Folia adapter (conditional) | Real Folia smoke and thread-safety review pass |

The order intentionally puts resource ownership before listeners and scheduler tasks: later runtime features must use the same lifecycle cleanup mechanism.

## 9. Open decisions for maintainer review

1. Should configuration use the proposed Paper-provided YAML parser in the adapter, with Core receiving only immutable maps, or introduce a dedicated YAML library? Recommendation: reuse the Paper-provided parser to avoid another runtime dependency.
2. Should reload be included in the first typed-config implementation? Recommendation: yes, but only as explicit command/API invocation; no file watcher.
3. Should scheduler ports initially expose one-shot operations only? Recommendation: yes; add delayed/repeating calls when a real template feature needs them.
4. Should the initial scheduler port live in Core or be injected only into Paper feature modules? Recommendation: Core port only if platform-independent use cases need to request scheduling; otherwise keep it in Paper until that need is demonstrated.

No implementation code is included in this design PR.
