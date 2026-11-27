# Nightly Paper compatibility checks

## Schedule and scope

- Workflow: [`.github/workflows/nightly-paper.yml`](../.github/workflows/nightly-paper.yml)
- Schedule: daily at 02:23 UTC (10:23, Asia/Shanghai).
- Default target: Paper 26.3, latest build whose PaperMC API channel is `STABLE`.
- Manual dispatch: accepts an explicit Paper Minecraft version; defaults to 26.3.
- Runtime: GitHub-hosted ephemeral Ubuntu runner with JDK 25.
- The workflow builds the template and runs the same disposable Paper smoke runner used by pull-request CI.

## Authorization and isolation

The `paper-nightly` job runs only when the organization variable `MINECRAFT_EULA_ACCEPTED` equals the literal string `true`. The value is passed to the runner, which independently checks it before creating `eula.txt`.

The smoke server is isolated and loopback-only:

- binds to `127.0.0.1`;
- disables RCON and Query;
- uses `online-mode=false`, one player, and low view/simulation distance;
- runs only on the temporary GitHub-hosted runner;
- does not connect to or modify the production OrzMC server.

Do not remove either EULA gate. Do not move this workflow to a persistent or production host.

## Latest STABLE resolution and integrity

The workflow queries PaperMC's v3 API for the selected version, filters builds to `channel == STABLE`, and selects the highest build ID. It passes the build ID and published server JAR SHA-256 to `scripts/smoke-paper.py`.

The runner independently re-queries PaperMC metadata, checks that the requested build is still STABLE and that the published checksum matches, downloads only from the expected HTTPS PaperMC data host, and verifies the downloaded JAR SHA-256 before launch.

## Evidence

Each run uploads `paper-nightly-evidence-<run-id>`, containing only:

- `smoke-report.json`
- `logs/server.log`
- `server.properties`
- `eula.txt`

The report records Paper version/build/checksum, Java runtime, plugin artifact checksum, command/reload results, clean shutdown, and process exit status. The artifact intentionally excludes generated world data and caches.

## Failure-path regression coverage

The normal PR/push CI runs `python3 -m unittest discover -s tests -p 'test_*.py' -v`. Current tests cover:

- EULA authorization absent/false: fail closed before writing EULA/server properties or starting Paper;
- startup log marker timeout;
- command response timeout;
- startup failure followed by graceful `stop`, then `terminate`, then `kill` when the process remains unresponsive.

The first scheduled nightly execution is still pending; the workflow implementation and the PR-triggered Paper smoke are separate validations. Do not describe the scheduled workflow as operationally verified until its first scheduled or manually dispatched run succeeds.
