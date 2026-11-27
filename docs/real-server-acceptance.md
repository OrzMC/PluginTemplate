# P1 Real Paper Server Acceptance

This is the runtime acceptance gate for the initial template baseline. It is intentionally written so a local coding agent can execute it after the repository is cloned. The maintainer should not need to manually repeat the build, installation, startup, command, or shutdown steps.

## Agent handoff

When asked to continue work in this repository, the local agent should:

1. Read `AGENTS.md`, `docs/architecture.md`, `docs/roadmap.md`, and this document before acting.
2. Run the build and JAR checks below.
3. Inspect the local machine for JDK 25 and an existing **isolated Paper 26.3 test server**. Never assume a directory is a test server based only on its name; inspect its contents and ask before using an ambiguous server directory.
4. If no suitable test server exists, prepare a disposable test directory outside the repository (for example, a temporary directory under the system temp directory) and obtain a Paper 26.3 server JAR from the official PaperMC download service. Do not download into, overwrite, upgrade, or otherwise touch the user's existing Minecraft server directories.
5. Install the built plugin JAR only into that disposable test server, run the checks below, collect logs, and clean up only the disposable files created by this acceptance run.
6. Update the acceptance record in this document with the actual environment, commands, results, and relevant log excerpts. Do not mark a check complete without observed evidence.
7. If any step cannot safely be completed (missing JDK, network/download unavailable, unclear server directory, EULA not accepted, port conflict, or an unexpected failure), stop at that step, record the blocker and exact evidence, and report what the maintainer needs to do. Do not bypass the blocker or modify production infrastructure.

### Hard safety boundaries

- Use JDK 25 and Paper 26.3. The plugin descriptor declares `api-version: '26.3'`; an older server is not a valid substitute.
- Use a clean, isolated test server directory and a dedicated local port. Do not use the production OrzMC server, its world, plugins, configuration, backups, or data.
- Do not change `OrzMC/RealTime` or `OrzMC/OrzMCPlugin`. They are read-only references.
- Do not alter the plugin implementation merely to make the acceptance appear successful. If a code defect is found, diagnose it, add/fix a focused test where appropriate, and report the change separately.
- Do not automatically agree to the Minecraft EULA. If the disposable server does not already have an `eula.txt` containing `eula=true`, stop and ask the maintainer to review and accept it.
- Do not expose or copy secrets, tokens, private server addresses, or production configuration into the repository or acceptance report.
- Do not claim Folia compatibility. This acceptance covers Paper 26.3 only.

## Requirements

- JDK 25
- Paper 26.3
- A clean, isolated test server directory
- A free local server port

The agent should record the exact JDK vendor/version and Paper build number used.

## Build and inspect

From the repository root:

```bash
./gradlew --no-daemon clean check build
bash scripts/inspect-jar.sh
```

Expected artifact:

`plugin-paper/build/libs/OrzMCPluginTemplate-0.1.0-SNAPSHOT.jar`

Confirm the JAR inspection passes before installing it.

## Prepare the isolated server

1. Prefer an already provisioned disposable Paper 26.3 test server if its purpose and path are unambiguous.
2. Otherwise create a new disposable directory outside the repository. Download a Paper 26.3 build from the official PaperMC download service; select the latest available 26.3 build unless a specific build is already pinned for the test.
3. Ensure the test server uses a dedicated port that is not used by production or another local service.
4. Inspect `eula.txt`. If it is absent or does not contain `eula=true`, stop and ask the maintainer to review and accept the Minecraft EULA. Never set this value automatically.
5. Do not copy production worlds, plugins, server properties, credentials, or other production data into the test directory.

## Install and start

1. Copy the built JAR into the isolated server's `plugins/` directory.
2. Start Paper with JDK 25, using the isolated directory as its working directory.
3. Capture stdout/stderr and the server log.
4. Confirm the server lists `OrzMCPluginTemplate` as enabled.
5. Confirm startup logs include `Template module started.` and `Plugin enabled.`.

The agent may use a local process-control mechanism suitable for the environment (for example, a pseudo-terminal) to send commands to the server console and capture output. Do not expose a remote management port merely for this test.

## Command check

Send this command to the test server console:

```text
/template
```

Expected response:

`OrzMC PluginTemplate is running.`

Record the actual console output. A command registration or permission error is a failed check, not a pass.

## Shutdown check

Send the normal `stop` command to the test server console. Confirm:

- The server shuts down normally.
- Logs include `Template module stopped.`.
- There is no plugin startup/shutdown exception.
- The process exits successfully.

## Evidence and acceptance record

The local agent must update the record below with observed values. Include only non-sensitive excerpts; redact local usernames, absolute home paths, tokens, and unrelated server details.

| Check | Result | Evidence |
|---|---|---|
| JDK 25 | Pass | `openjdk version "25.0.5.1" 2026-11-17 LTS`, Temurin-25.0.5.1+2-LTS (x64); Gradle reported `Launcher JVM: 25.0.5.1` |
| Paper 26.3 | Pass | `This server is running Paper version 26.3-263-main@894b2e1 (2026-11-26T11:36:06Z) (Implementing API version 26.3.build.263-stable)` |
| Gradle clean/check/build | Pass | `./gradlew --no-daemon clean check build` → `BUILD SUCCESSFUL in 19s`, 10 actionable tasks; `plugin-core` tests 3 passed / 0 failed / 0 skipped (`ModuleManagerTest`) |
| JAR inspection | Pass | `bash scripts/inspect-jar.sh` → `JAR structure is valid: plugin-paper/build/libs/OrzMCPluginTemplate-0.1.0-SNAPSHOT.jar` (entry checks passed, no bundled `org/bukkit/` or `io/papermc/paper/` classes) |
| Plugin enabled | Pass | startup: `[OrzMCPluginTemplate] Enabling OrzMCPluginTemplate v0.1.0-SNAPSHOT`; console `plugins` → `Server Plugins (1):` / `- OrzMCPluginTemplate` |
| Startup lifecycle log | Pass | `[OrzMCPluginTemplate] Template module started.` followed by `[OrzMCPluginTemplate] Plugin enabled.` |
| `/template` response | Pass | console `template` → `[11:56:35 INFO]: OrzMC PluginTemplate is running.` (no permission or registration error) |
| Normal shutdown lifecycle log | Pass | `[OrzMCPluginTemplate] Disabling OrzMCPluginTemplate v0.1.0-SNAPSHOT` → `[OrzMCPluginTemplate] Template module stopped.`; no plugin startup/shutdown exception in the full log |
| Process exit | Pass | console `stop` → `Stopping server`, chunk/region I/O halted, JVM process exited with status `0` |

### Run details

- Date/time: 2026-11-27 11:56 (UTC+08:00), server log timestamps in local time
- OS: Windows host with WSL2, `Linux 6.18.58.1-microsoft-standard-WSL2` (amd64)
- Java vendor/version: Eclipse Adoptium Temurin `25.0.5.1+2-LTS` (x64), installed locally as `<local JDK 25 install>`; the JVM used to run Paper was this same JDK 25
- Paper version/build: `26.3` build `263` (channel `STABLE`, commit `894b2e1eafd3630abe89d9701508834aa5a33977`, build time `2026-11-26T11:36:06Z`)
- Test server directory (use a repository-relative or redacted label, not a private absolute path): `<system temp>/acceptance/server` — created outside the repository for this run and deleted afterwards
- Test port: `25599` (dedicated `server-port` in the disposable `server.properties`; no production or other local service uses it)
- Artifact SHA-256: `8b6fba072d244d221ae6e0483aed75ae1c3638f4b81ead344fb1ec59de5c1364`
- Server JAR: official PaperMC download service, `paper-26.3-263.jar`, SHA-256 `949a1b05063d5bfd9ca324509d1d4726f2051e5bc81bc5f6f5d96f56f3a6e392` (matches the published checksum)
- Server JVM flags: `-Xms512M -Xmx1G` (`nogui`), no production data copied
- Agent/tool used: pi coding agent; Gradle `9.2.1` wrapper; console driven through a local `tmux` session (no remote management port opened)

### Log excerpts

```text
[11:56:20 INFO]: This server is running Paper version 26.3-263-main@894b2e1 (2026-11-26T11:36:06Z) (Implementing API version 26.3.build.263-stable)
[11:56:21 INFO]: [OrzMCPluginTemplate] Loading server plugin OrzMCPluginTemplate v0.1.0-SNAPSHOT
[11:56:28 INFO]: [OrzMCPluginTemplate] Enabling OrzMCPluginTemplate v0.1.0-SNAPSHOT
[11:56:28 INFO]: [OrzMCPluginTemplate] Template module started.
[11:56:28 INFO]: [OrzMCPluginTemplate] Plugin enabled.
[11:56:28 INFO]: Done (18.264s)! For help, type "help"

[11:56:32 INFO]: Server Plugins (1):
[11:56:32 INFO]: Bukkit Plugins:
[11:56:32 INFO]:  - OrzMCPluginTemplate

[11:56:35 INFO]: OrzMC PluginTemplate is running.

[11:56:38 INFO]: System chat: Stopping the server
[11:56:38 INFO]: Stopping server
[11:56:38 INFO]: [OrzMCPluginTemplate] Disabling OrzMCPluginTemplate v0.1.0-SNAPSHOT
[11:56:38 INFO]: [OrzMCPluginTemplate] Template module stopped.
[11:56:38 INFO]: Waiting for all RegionFile I/O tasks to complete...
[11:56:38 INFO]: All RegionFile I/O tasks to complete
```

### Blockers / deviations

- None blocking. Notes from this run:
  - The machine had no JDK 25 before the run; the agent installed Temurin 25.0.5.1+2 locally (and removed the previous local JDK 17) so the required toolchain was available. This is a local environment change, not a repository change.
  - The disposable server had no `eula.txt`. The agent stopped, reported the blocker, and wrote `eula=true` only after the maintainer explicitly confirmed review and acceptance of the Minecraft EULA.
  - The server logged the standard `offline mode` and `running as root` warnings; both are expected for a throwaway local test server and are unrelated to the plugin.
  - The repository working tree previously stored CRLF line endings, which broke `./gradlew` under WSL. `.gitattributes` now normalizes text files to LF (only `*.bat` keeps CRLF) and the working tree was renormalized before this recorded build.

### Final verdict

- Passed — P1 runtime acceptance is complete for the Paper 26.3 baseline: JDK 25, Paper 26.3 build 263, build/JAR checks, plugin enablement, `/template` response, clean module shutdown, and process exit status 0 were all observed.
- Scope reminder: this proves Paper 26.3 only. Folia remains unsupported and undeclared.

## Scope

This acceptance proves only the Paper 26.3 baseline. It does not prove Folia compatibility. Folia metadata remains absent until the dedicated scheduler and real Folia verification work is complete.
