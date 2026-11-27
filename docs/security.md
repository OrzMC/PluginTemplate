# Repository security and dependency hygiene

## Automated dependency inventory

`.github/workflows/dependency-submission.yml` submits a Gradle dependency graph on every push to `main` and supports manual dispatch. It uses the official Gradle dependency-submission action with the repository's JDK 25 toolchain and `contents: write` permission.

The dependency graph powers GitHub's dependency inventory and Dependabot vulnerability alerts. The workflow is intentionally separate from build/test CI so a missing repository security setting does not block plugin builds.

## Dependency update automation

`.github/dependabot.yml` checks the Gradle dependency declarations and GitHub Actions references weekly. Dependabot opens bounded update PRs (maximum five open PRs per ecosystem). Review update PRs through the normal build, unit-test, and real Paper smoke gates.

## Required repository settings

The GitHub connector used for repository changes cannot enable repository security settings. A repository administrator must verify these once in **Settings → Security & analysis**:

1. Enable **Dependency graph**.
2. Enable **Dependabot alerts**.
3. Enable **Dependabot security updates**.
4. Enable **Secret scanning** and alerts, if the controls are available for the repository.
5. Enable **Push protection** for the repository if the account/repository plan exposes it.

GitHub documents secret scanning as free for public repositories. Dependency graph and Dependabot alerts are not necessarily enabled by default for public repositories; the dependency submission workflow requires the dependency graph feature to be enabled before it can populate the graph.

## Security policy

See [SECURITY.md](../SECURITY.md) for private vulnerability reporting guidance. Do not place tokens, API keys, production configuration, or private server addresses in the template repository or its CI logs.

## Scope and limitations

- Dependency submission runs on trusted pushes to `main` and manual dispatch; it does not grant write permissions to untrusted fork pull-request workflows.
- Dependabot version updates and security alerts are not proof that dependencies are vulnerability-free. Review alerts and update PRs.
- GitHub secret scanning covers supported secret patterns; it does not replace review of logs, configs, or manual secret hygiene.
- The current CI continues to use read-only permissions except for the isolated dependency-submission workflow.
