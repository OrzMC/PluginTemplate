# P1 Real Paper Server Acceptance

This is the manual runtime gate for the initial template baseline. Use an isolated test server, not a production OrzMC server.

## Requirements

- JDK 25
- Paper 26.3
- A clean test server directory

The plugin descriptor declares `api-version: '26.3'`; do not use an older server for this acceptance.

## Build

From a clean clone of the PR branch:

```bash
./gradlew --no-daemon clean check build
bash scripts/inspect-jar.sh
```

Expected artifact:

`plugin-paper/build/libs/OrzMCPluginTemplate-0.1.0-SNAPSHOT.jar`

## Install and start

1. Copy the JAR into the isolated server's `plugins/` directory.
2. Start Paper with Java 25.
3. Confirm the server lists `OrzMCPluginTemplate` as enabled.
4. Confirm startup logs include `Template module started.` and `Plugin enabled.`.

## Command check

Run:

```text
/template
```

Expected response:

`OrzMC PluginTemplate is running.`

## Shutdown check

Stop the server normally. Confirm logs include `Template module stopped.` and there is no plugin startup/shutdown exception.

## Evidence to record

- Paper build/version and Java version
- Plugin load result
- `/template` command result
- Startup and shutdown log excerpts
- Whether any warnings/errors appeared

## Scope

This acceptance proves only the Paper 26.3 baseline. It does not prove Folia compatibility. Folia metadata remains absent until the dedicated scheduler and real Folia verification work is complete.
