# This file is part of mcav, a media playback library for Java
# Copyright (C) Brandon Li <https://brandonli.me/>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.


"""Exact event matching for the MCV2 latency report, without JFR or a client."""

import json
import sys
import tempfile
import unittest
from contextlib import redirect_stdout
from io import StringIO
from pathlib import Path
from unittest.mock import patch

import mcv2_tools


def event(frame, arrived, sent, sent_to=1):
    return dict(frame=frame, arrived=arrived, sent=sent, sent_to=sent_to, number=7,
                keyframe=True, bytes=64, colors=64, behind=0, waiting=0, backlog=0)


class LatencyTest(unittest.TestCase):

    def report(self, later):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory, "report.json")
            arguments = ["mcv2_tools.py", "unused.jfr", "unused.nut", "--json", str(output)]
            with patch.object(sys, "argv", arguments), patch.object(mcv2_tools, "latency_events", return_value=[event(0, 0, 100), later]),\
                    patch.object(mcv2_tools, "latency_captures", return_value=[(200, 7), (210, 7)]), redirect_stdout(StringIO()):
                mcv2_tools.latency_main()
            return json.loads(output.read_text())

    def test_should_use_a_frame_sent_before_the_capture_for_a_repeated_number(self):
        result = self.report(event(1, 150, 250))
        displayed = result["displayed"]
        self.assertEqual(1, displayed)

        for statistic in ("mean", "p50", "p95", "max"):
            glass_to_glass = result["glass_to_glass_ms_" + statistic]
            self.assertEqual(200, glass_to_glass)

    def test_should_not_replace_the_displayed_frame_with_an_unsent_occurrence(self):
        result = self.report(event(1, 150, 175, sent_to=0))
        glass_to_glass = result["glass_to_glass_ms_mean"]
        self.assertEqual(200, glass_to_glass)

    def test_should_display_a_later_sent_occurrence(self):
        result = self.report(event(1, 150, 175))
        glass_to_glass = result["glass_to_glass_ms_mean"]
        self.assertEqual(50, glass_to_glass)

    def test_should_not_display_an_occurrence_with_a_nonfinite_send_time(self):
        for invalid in (float("nan"), float("inf")):
            with self.subTest(invalid=invalid):
                result = self.report(event(1, 150, invalid))
                glass_to_glass = result["glass_to_glass_ms_mean"]
                self.assertEqual(200, glass_to_glass)


if __name__ == "__main__":
    unittest.main()
