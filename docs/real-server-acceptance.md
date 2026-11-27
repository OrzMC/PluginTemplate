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
| JDK 25 | Pending | |
| Paper 26.3 | Pending | |
| Gradle clean/check/build | Pending | |
| JAR inspection | Pending | |
| Plugin enabled | Pending | |
| Startup lifecycle log | Pending | |
| `/template` response | Pending | |
| Normal shutdown lifecycle log | Pending | |
| Process exit | Pending | |

### Run details

- Date/time:
- OS:
- Java vendor/version:
- Paper version/build:
- Test server directory (use a repository-relative or redacted label, not a private absolute path):
- Test port:
- Artifact SHA-256:
- Agent/tool used:

### Log excerpts

```text
[Paste only the relevant, redacted startup, command, and shutdown excerpts here.]
```

### Blockers / deviations

- None recorded.

### Final verdict

- Pending — do not mark P1 runtime acceptance complete until every required check above has observed passing evidence.

## Scope

This acceptance proves only the Paper 26.3 baseline. It does not prove Folia compatibility. Folia metadata remains absent until the dedicated scheduler and real Folia verification work is complete.
