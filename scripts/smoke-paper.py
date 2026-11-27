#!/usr/bin/env python3
"""Run an isolated, disposable Paper smoke test for the current plugin artifact."""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import shutil
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path
from typing import Any

PROJECT = "paper"
USER_AGENT = "OrzMC-PluginTemplate-CI/1.0.0 (https://github.com/OrzMC/PluginTemplate)"
DEFAULT_VERSION = "26.3"
DEFAULT_BUILD = 263
DEFAULT_SHA256 = "949a1b05063d5bfd9ca324509d1d4726f2051e5bc81bc5f6f5d96f56f3a6e392"


def request_json(url: str) -> Any:
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT, "Accept": "application/json"})
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.loads(response.read().decode("utf-8"))


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def download_paper(version: str, build_number: int, expected_sha256: str, destination: Path) -> dict[str, Any]:
    api_url = f"https://fill.papermc.io/v3/projects/{PROJECT}/versions/{version}/builds"
    builds = request_json(api_url)
    if not isinstance(builds, list):
        raise RuntimeError(f"Unexpected PaperMC API response: {builds!r}")

    build = next(
        (item for item in builds if item.get("id") == build_number and item.get("channel") == "STABLE"),
        None,
    )
    if build is None:
        raise RuntimeError(f"Paper {version} stable build {build_number} was not found")

    download = build.get("downloads", {}).get("server:default", {})
    url = download.get("url")
    published_sha256 = download.get("checksums", {}).get("sha256")
    if not url or not published_sha256:
        raise RuntimeError("PaperMC API response is missing the server download URL or SHA-256")
    parsed = urllib.parse.urlparse(url)
    if parsed.scheme != "https" or parsed.hostname != "fill-data.papermc.io":
        raise RuntimeError(f"Refusing unexpected Paper download URL: {url}")

    if published_sha256.lower() != expected_sha256.lower():
        raise RuntimeError(
            f"Paper checksum metadata changed: expected {expected_sha256}, "
            f"API reports {published_sha256}"
        )

    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    digest = hashlib.sha256()
    with urllib.request.urlopen(request, timeout=90) as response, destination.open("wb") as output:
        while chunk := response.read(1024 * 1024):
            digest.update(chunk)
            output.write(chunk)

    actual_sha256 = digest.hexdigest()
    if actual_sha256.lower() != expected_sha256.lower():
        destination.unlink(missing_ok=True)
        raise RuntimeError(f"Paper JAR SHA-256 mismatch: expected {expected_sha256}, got {actual_sha256}")

    return {
        "version": version,
        "build": build_number,
        "channel": build.get("channel"),
        "download_url": url,
        "sha256": actual_sha256,
    }


def probe_paper_help(paper_jar: Path, work_dir: Path) -> dict[str, Any]:
    """Probe CLI help with a hard timeout before creating the EULA file."""
    command = ["java", "-jar", str(paper_jar), "--help"]
    result: dict[str, Any] = {"command": command, "timeout_seconds": 20, "recognized": False}
    try:
        completed = subprocess.run(command, cwd=work_dir, capture_output=True, text=True,
                                   encoding="utf-8", errors="replace", timeout=20, check=False)
        output = (completed.stdout + completed.stderr).strip()
        result.update({"exit_code": completed.returncode,
                       "recognized": completed.returncode == 0 and any(
                           marker in output.lower() for marker in ("usage", "options", "--nogui")),
                       "output": output[-12000:]})
    except subprocess.TimeoutExpired as error:
        stdout = error.stdout or ""
        stderr = error.stderr or ""
        if isinstance(stdout, bytes): stdout = stdout.decode("utf-8", errors="replace")
        if isinstance(stderr, bytes): stderr = stderr.decode("utf-8", errors="replace")
        result.update({"timed_out": True, "output": (str(stdout) + str(stderr))[-12000:]})
    return result

def read_log(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8", errors="replace")
    except FileNotFoundError:
        return ""


def wait_for_log(process: subprocess.Popen[str], log_path: Path, marker: str, timeout: int) -> None:
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        log = read_log(log_path)
        if marker in log:
            return
        if process.poll() is not None:
            raise RuntimeError(f"Paper exited with status {process.returncode} before log marker: {marker}")
        time.sleep(1)
    raise TimeoutError(f"Timed out waiting for Paper log marker: {marker}")


def send_command(process: subprocess.Popen[str], log_path: Path, command: str, marker: str, timeout: int = 30) -> None:
    if process.stdin is None or process.poll() is not None:
        raise RuntimeError(f"Cannot send command to Paper: {command}")
    before = len(read_log(log_path))
    process.stdin.write(command + "\n")
    process.stdin.flush()
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        log = read_log(log_path)
        if marker in log[before:]:
            return
        if process.poll() is not None:
            raise RuntimeError(f"Paper exited while waiting for command response: {command}")
        time.sleep(0.25)
    raise TimeoutError(f"Timed out waiting for command response to: {command}")


def tail(path: Path, count: int = 100) -> str:
    lines = read_log(path).splitlines()
    return "\n".join(lines[-count:])


def run_smoke(args: argparse.Namespace) -> dict[str, Any]:
    work_dir = args.work_dir.resolve()
    if work_dir.exists():
        shutil.rmtree(work_dir)
    work_dir.mkdir(parents=True)
    plugins_dir = work_dir / "plugins"
    plugins_dir.mkdir()
    logs_dir = work_dir / "logs"
    logs_dir.mkdir()

    paper_jar = work_dir / "paper.jar"
    plugin_jar = args.plugin_jar.resolve()
    if not plugin_jar.is_file():
        raise FileNotFoundError(f"Plugin artifact not found: {plugin_jar}")

    plugin_sha256 = sha256_file(plugin_jar)
    paper = download_paper(args.paper_version, args.paper_build, args.paper_sha256, paper_jar)
    shutil.copy2(plugin_jar, plugins_dir / plugin_jar.name)

    # Probe before generating eula.txt; a broken/unsupported --help must not hang CI.
    paper_help = probe_paper_help(paper_jar, work_dir)

    # Defense in depth: the workflow job gate is authoritative, and the runner
    # itself also fails closed if invoked outside that authorized job.
    if os.environ.get("MINECRAFT_EULA_ACCEPTED") != "true":
        raise RuntimeError("EULA not authorized: set MINECRAFT_EULA_ACCEPTED=true only after accepting the Minecraft EULA")
    (work_dir / "eula.txt").write_text("eula=true\n", encoding="utf-8")
    (work_dir / "server.properties").write_text(
        "\n".join(
            [
                "server-ip=127.0.0.1",
                "server-port=25565",
                "enable-rcon=false",
                "enable-query=false",
                "online-mode=false",
                "motd=PluginTemplate CI smoke",
                "max-players=1",
                "view-distance=2",
                "simulation-distance=2",
                "spawn-protection=0",
                "level-name=world",
                "",
            ]
        ),
        encoding="utf-8",
    )

    log_path = logs_dir / "server.log"
    report: dict[str, Any] = {
        "status": "running",
        "paper": paper,
        "paper_help_probe": paper_help,
        "plugin": {"file": plugin_jar.name, "sha256": plugin_sha256},
        "runtime": {
            "java_version": subprocess.run(["java", "-version"], capture_output=True, text=True, encoding="utf-8", errors="replace", check=False).stderr.strip(),
            "launch_command": ["java", "-Xms512M", "-Xmx1G", "-jar", str(paper_jar), "--nogui"],
            "server_properties": {"server-ip": "127.0.0.1", "server-port": 25565, "online-mode": False, "enable-rcon": False, "enable-query": False},
            "eula_file_generated_after_workflow_authorization": True,
        },
        "checks": {"paper_help_probe_returned": "timed_out" not in paper_help},
    }
    process: subprocess.Popen[str] | None = None

    with log_path.open("w", encoding="utf-8") as log_stream:
        try:
            process = subprocess.Popen(
                ["java", "-Xms512M", "-Xmx1G", "-jar", str(paper_jar), "--nogui"],
                cwd=work_dir,
                stdin=subprocess.PIPE,
                stdout=log_stream,
                stderr=subprocess.STDOUT,
                text=True,
                encoding="utf-8",
                errors="replace",
                bufsize=1,
            )

            wait_for_log(process, log_path, "Done (", timeout=240)
            startup_log = read_log(log_path)
            if "Plugin enabled." not in startup_log or "Template module started." not in startup_log:
                raise RuntimeError("Paper started, but PluginTemplate startup markers were not found")
            report["checks"]["plugin_enabled"] = True

            send_command(
                process, log_path, "template", "OrzMC PluginTemplate is running.", timeout=30
            )
            report["checks"]["template_command"] = True

            send_command(
                process, log_path, "template reload", "Configuration reloaded.", timeout=30
            )
            report["checks"]["config_reload"] = True

            send_command(process, log_path, "stop", "Stopping server", timeout=30)
            exit_code = process.wait(timeout=60)
            if exit_code != 0:
                raise RuntimeError(f"Paper exited with non-zero status: {exit_code}")
            shutdown_log = read_log(log_path)
            if "Template module stopped." not in shutdown_log:
                raise RuntimeError("Plugin module shutdown marker was not found")
            report["checks"]["clean_shutdown"] = True
            report["process_exit_code"] = exit_code
            report["status"] = "passed"
            return report
        finally:
            if process is not None and process.poll() is None:
                try:
                    if process.stdin is not None:
                        process.stdin.write("stop\n")
                        process.stdin.flush()
                    process.wait(timeout=15)
                except Exception:
                    process.terminate()
                    try:
                        process.wait(timeout=10)
                    except subprocess.TimeoutExpired:
                        process.kill()
                        process.wait(timeout=10)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--plugin-jar", type=Path, required=True)
    parser.add_argument("--work-dir", type=Path, required=True)
    parser.add_argument("--paper-version", default=DEFAULT_VERSION)
    parser.add_argument("--paper-build", type=int, default=DEFAULT_BUILD)
    parser.add_argument("--paper-sha256", default=DEFAULT_SHA256)
    args = parser.parse_args()

    report_path = args.work_dir.resolve() / "smoke-report.json"
    report: dict[str, Any]
    try:
        report = run_smoke(args)
    except Exception as error:
        report = {"status": "failed", "error": f"{type(error).__name__}: {error}"}
        print(f"Paper smoke failed: {report['error']}", file=sys.stderr)
        print("\n".join(tail(args.work_dir.resolve() / "logs" / "server.log").splitlines()), file=sys.stderr)
    finally:
        args.work_dir.resolve().mkdir(parents=True, exist_ok=True)
        # The successful return value is assigned below; failure details are saved in except.
        if "report" in locals():
            report_path.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")

    if report["status"] != "passed":
        return 1
    print(json.dumps(report, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
