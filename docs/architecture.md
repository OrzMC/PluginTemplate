# Architecture
- `plugin-core` is platform-independent and must not import Bukkit/Paper.
- `plugin-paper` owns the JavaPlugin entry point, metadata, command registration and platform adapters.
- The entry point is the composition root; dependencies are wired explicitly.
- Modules start in declaration order and stop in reverse order. Startup failure triggers rollback including the failing module (for partial initialization); shutdown attempts every started module. Stop is idempotent; restart is unsupported.
- A module must clean up resources acquired before its own start failed.
- Commands use traditional `plugin.yml` and the main plugin's Paper Lifecycle API + Brigadier registration.
- This baseline has no scheduler abstraction and makes no Folia compatibility claim.
- Typed config, event ownership, permission helpers and scheduler adapters are deferred until designed and tested.


## P2 runtime design

The proposed P2 contracts for resource ownership, typed configuration, command/permission boundaries and explicit scheduler semantics are documented in [p2-runtime-design.md](p2-runtime-design.md). Treat that document as a design proposal until the maintainer review is complete; do not implement Folia support or add a Folia metadata declaration based on this document alone.
