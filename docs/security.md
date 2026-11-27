# Repository security and dependency hygiene

## Automated dependency inventory

`.github/workflows/dependency-submission.yml` submits a Gradle dependency graph on every push to `main` and supports manual dispatch. It uses the official Gradle dependency-submission action with the repository's JDK 25 toolchain and `contents: write` permission.

The dependency graph powers GitHub's dependency inventory and Dependabot vulnerability alerts. The workflow is intentionally separate from build/test CI so a missing repository security setting does not block plugin builds.

## Dependency update automation

`.github/dependabot.yml` checks the Gradle dependency declarations and GitHub Actions references weekly. Dependabot opens bounded update PRs (maximum five open PRs per ecosystem). Review update PRs through the normal build, unit-test, and real Paper smoke gates.

## CodeQL static analysis

`.github/workflows/codeql.yml` runs advanced CodeQL analysis for Java/Kotlin on pushes to `main`, pull requests targeting `main`, weekly on Monday at 11:30 Beijing time, and manual dispatch.

The workflow uses CodeQL Action v4, JDK 25, Gradle's configured wrapper and a manual build. It disables Gradle's build cache and reruns tasks so CodeQL observes the Java compiler invocations instead of restoring compiled classes from cache. Building between CodeQL initialization and analysis improves Java extraction accuracy for this Gradle project. Fork pull requests are analyzed but do not upload SARIF because their `GITHUB_TOKEN` is read-only.

**Do not enable CodeQL default setup at the same time as this advanced workflow.** GitHub treats default and advanced setup as alternative configuration modes. If default setup is already enabled, review it under Settings → Security → Advanced Security before relying on this workflow; choose one mode and disable the other.

The workflow requires the repository to permit GitHub Actions and the `security-events: write` permission. CodeQL results appear under Security → Code scanning after the first successful upload. A successful build alone does not mean CodeQL analysis or SARIF upload succeeded.

## Required repository settings

The GitHub connector used for repository changes cannot enable repository security settings. A repository administrator must verify these once in **Settings → Security & analysis**:

1. Enable **Dependency graph**.
2. Enable **Dependabot alerts**.
3. Enable **Dependabot security updates**.
4. Enable **Secret scanning** and alerts, if the controls are available for the repository.
5. Enable **Push protection** for the repository if the account/repository plan exposes it.
6. Confirm CodeQL is configured in exactly one mode: the advanced workflow in this repository or GitHub's default setup, not both.

GitHub documents secret scanning as free for public repositories. Dependency graph and Dependabot alerts are not necessarily enabled by default for public repositories; the dependency submission workflow requires the dependency graph feature to be enabled before it can populate the graph.

## Security policy

See [SECURITY.md](../SECURITY.md) for private vulnerability reporting guidance. Do not place tokens, API keys, production configuration, or private server addresses in the template repository or its CI logs.

## Scope and limitations

- Dependency submission runs on trusted pushes to `main` and manual dispatch; it does not grant write permissions to untrusted fork pull-request workflows.
- Dependabot version updates and security alerts are not proof that dependencies are vulnerability-free. Review alerts and update PRs.
- GitHub secret scanning covers supported secret patterns; it does not replace review of logs, configs, or manual secret hygiene.
- The normal build/test CI continues to use read-only permissions. Dependency submission and CodeQL use separate workflows with narrowly scoped write permissions.
