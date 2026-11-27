from __future__ import annotations

import argparse
import importlib.util
import os
import tempfile
import unittest
from pathlib import Path
from unittest.mock import Mock, patch


ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / "scripts" / "smoke-paper.py"
SPEC = importlib.util.spec_from_file_location("smoke_paper", SCRIPT)
assert SPEC is not None and SPEC.loader is not None
smoke_paper = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(smoke_paper)


class EulaFailClosedTests(unittest.TestCase):
    def test_runner_does_not_write_eula_or_start_paper_without_authorization(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            plugin_jar = root / "plugin.jar"
            plugin_jar.write_bytes(b"test plugin jar")
            work_dir = root / "paper-smoke"
            args = argparse.Namespace(
                work_dir=work_dir,
                plugin_jar=plugin_jar,
                paper_version="26.3",
                paper_build=263,
                paper_sha256="0" * 64,
            )

            with patch.dict(os.environ, {"MINECRAFT_EULA_ACCEPTED": "false"}):
                with (
                    patch.object(smoke_paper, "download_paper", return_value={"version": "26.3"}) as download,
                    patch.object(smoke_paper, "probe_paper_help", return_value={"recognized": True}) as probe,
                    patch.object(smoke_paper.subprocess, "Popen") as popen,
                ):
                    with self.assertRaisesRegex(RuntimeError, "EULA not authorized"):
                        smoke_paper.run_smoke(args)

            download.assert_called_once()
            probe.assert_called_once()
            popen.assert_not_called()
            self.assertFalse((work_dir / "eula.txt").exists())
            self.assertFalse((work_dir / "server.properties").exists())


class FakeInput:
    def __init__(self) -> None:
        self.writes: list[str] = []

    def write(self, value: str) -> None:
        self.writes.append(value)

    def flush(self) -> None:
        pass


class FailurePathTests(unittest.TestCase):
    def test_wait_for_log_reports_startup_timeout(self) -> None:
        process = Mock()
        process.poll.return_value = None
        with (
            patch.object(smoke_paper, "read_log", return_value=""),
            patch.object(smoke_paper.time, "monotonic", side_effect=[0.0, 2.0]),
            patch.object(smoke_paper.time, "sleep"),
        ):
            with self.assertRaisesRegex(TimeoutError, "Timed out waiting for Paper log marker"):
                smoke_paper.wait_for_log(process, Path("server.log"), "Done (", timeout=1)

    def test_send_command_reports_response_timeout(self) -> None:
        process = unittest.mock.Mock()
        process.poll.return_value = None
        process.stdin = FakeInput()
        with (
            patch.object(smoke_paper, "read_log", return_value=""),
            patch.object(smoke_paper.time, "monotonic", side_effect=[0.0, 2.0]),
            patch.object(smoke_paper.time, "sleep"),
        ):
            with self.assertRaisesRegex(TimeoutError, "Timed out waiting for command response"):
                smoke_paper.send_command(process, Path("server.log"), "template", "expected", timeout=1)
        self.assertEqual(process.stdin.writes[0].strip(), "template")

    def test_startup_failure_stops_and_kills_unresponsive_paper_process(self) -> None:
        class StuckProcess:
            def __init__(self) -> None:
                self.stdin = FakeInput()
                self.wait_calls = 0
                self.terminated = False
                self.killed = False

            def poll(self):
                return None

            def wait(self, timeout=None):
                self.wait_calls += 1
                if self.wait_calls <= 2:
                    raise smoke_paper.subprocess.TimeoutExpired("java", timeout)
                return 0

            def terminate(self):
                self.terminated = True

            def kill(self):
                self.killed = True

        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            plugin_jar = root / "plugin.jar"
            plugin_jar.write_bytes(b"test plugin jar")
            work_dir = root / "paper-smoke"
            process = StuckProcess()
            args = argparse.Namespace(
                work_dir=work_dir,
                plugin_jar=plugin_jar,
                paper_version="26.3",
                paper_build=263,
                paper_sha256="0" * 64,
            )
            with (
                patch.dict(os.environ, {"MINECRAFT_EULA_ACCEPTED": "true"}),
                patch.object(smoke_paper, "download_paper", return_value={"version": "26.3"}),
                patch.object(smoke_paper, "probe_paper_help", return_value={"recognized": True}),
                patch.object(smoke_paper.subprocess, "Popen", return_value=process),
                patch.object(smoke_paper, "wait_for_log", side_effect=TimeoutError("startup timeout")),
            ):
                with self.assertRaisesRegex(TimeoutError, "startup timeout"):
                    smoke_paper.run_smoke(args)

            self.assertEqual(process.stdin.writes[0].strip(), "stop")
            self.assertTrue(process.terminated)
            self.assertTrue(process.killed)
            self.assertEqual(process.wait_calls, 3)


if __name__ == "__main__":
    unittest.main()
