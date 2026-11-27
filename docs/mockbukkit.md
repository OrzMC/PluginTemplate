# MockBukkit test scope

## Dependency alignment

The Paper adapter test source set uses the Paper 26.3-specific MockBukkit artifact:

- MockBukkit: `org.mockbukkit.mockbukkit:mockbukkit-v26.3:4.124.0`
- MockBukkit's matching Paper API build: `26.3.build.200-stable` (pinned as `mockBukkitPaperApiVersion` in `gradle.properties`).
- JUnit 6.1.3 is used by the Paper adapter test source set; the independent core module retains its existing test dependency baseline.

MockBukkit validates version-specific registry data at startup. Its test runtime must use the Paper API build matching the MockBukkit artifact's `Paper-Version` manifest; using the project's floating `26.3.build.+` API caused initialization failure. Production compilation remains on the centralized `paperApiVersion` target.

## Current integration coverage

`PaperListenerRegistrarTest` verifies:

1. a listener receives a PlayerJoinEvent while its owning module is active;
2. stopping the module closes its ResourceScope;
3. the listener is unregistered and receives no later PlayerJoinEvent.

This tests the adapter's listener ownership contract using MockBukkit. The MockBukkit dependency is test-only and is not bundled into the plugin JAR.

## Skips are not passes

MockBukkit can represent unsupported API operations as aborted/skipped tests. The Paper adapter test task logs passed/skipped/failed test events and fails if the suite contains zero tests or any skipped tests. This prevents an unsupported mock API call from appearing as a green test.

JUnit XML and HTML reports are uploaded as the `junit-test-reports` artifact on every CI build, including failures.

## Paper YAML adapter coverage

`PaperYamlConfigStoreTest` exercises the Paper YAML adapter with MockBukkit:

- save/load round-trip for scalar values, nested maps and lists;
- timestamped backup creation for an existing config;
- replacement of an existing config and cleanup of the temporary file.

The Core `ConfigServiceTest` remains responsible for schema migration, validation, future-schema rejection and transactional publication. The adapter tests complement those Core tests; they do not replace the real Paper runtime acceptance.

## What still requires real Paper

MockBukkit does not replace the real Paper smoke test. The following remain validated only by the isolated Paper runtime:

- Paper Lifecycle API and Brigadier command registration/dispatch (including `/template` and `/template reload`);
- actual plugin enable/disable sequence;
- Paper runtime scheduler behavior;
- server process startup, command handling, reload and clean shutdown.

No Folia compatibility is inferred from MockBukkit or Paper smoke results.
