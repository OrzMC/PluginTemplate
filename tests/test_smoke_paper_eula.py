from __future__ import annotations

import argparse
import importlib.util
import os
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch


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


if __name__ == "__main__":
    unittest.main()
