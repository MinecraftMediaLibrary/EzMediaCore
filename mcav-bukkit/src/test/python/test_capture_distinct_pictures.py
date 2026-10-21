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


"""Capture coverage distinguishes pictures from indistinguishable frame occurrences."""

import json
import sys
import tempfile
import unittest
from contextlib import redirect_stdout
from io import StringIO
from pathlib import Path
from unittest.mock import patch

import numpy
from PIL import Image

import mcv2_tools


class CaptureCheckTest(unittest.TestCase):

    def report(self, references, captures):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            reference = root / "reference.rgb"
            reference.write_bytes(b"".join(picture.tobytes() for picture in references))
            for index, picture in enumerate(captures):
                Image.fromarray(picture).save(root / ("capture-%02d.png" % index))
            height, width = references[0].shape[:2]
            arguments = ["mcv2_tools.py", str(reference), str(width), str(height), str(root), "--top", "0"]
            output = StringIO()
            with patch.object(sys, "argv", arguments), redirect_stdout(output), self.assertRaises(SystemExit) as ended:
                mcv2_tools.capture_check_main()
            return ended.exception.code, json.loads(output.getvalue().splitlines()[-1]), output.getvalue()

    def test_should_not_count_identical_reference_frames_as_unmatchable_missing_pictures(self):
        picture = numpy.full((8, 8, 3), 37, numpy.uint8)
        status, result, text = self.report([picture, picture], [picture, picture])
        self.assertEqual(0, status)

        exact_captures = result["exact_captures"]
        self.assertEqual(2, exact_captures)

        pictures_seen_exactly = result["pictures_seen_exactly"]
        self.assertEqual(1, pictures_seen_exactly)

        distinct_pictures = result["distinct_pictures"]
        self.assertEqual(1, distinct_pictures)

        ambiguous_reference_frames = result["ambiguous_reference_frames"]
        self.assertEqual(2, ambiguous_reference_frames)

        frames_seen_exactly = result["frames_seen_exactly"]
        self.assertEqual(0, frames_seen_exactly)

        self.assertIn("indistinguishable", text)

    def test_should_fail_when_a_distinct_picture_is_missing(self):
        first = numpy.full((8, 8, 3), 37, numpy.uint8)
        second = numpy.full((8, 8, 3), 92, numpy.uint8)
        status, result, _ = self.report([first, first, second], [first, first])
        self.assertEqual(1, status)

        exact_captures = result["exact_captures"]
        self.assertEqual(2, exact_captures)

    def test_should_find_different_frames_indistinguishable_when_the_visible_crop_hides_the_difference(self):
        first = numpy.full((16, 8, 3), 37, numpy.uint8)
        second = first.copy()
        second[8:] = 92
        status, result, _ = self.report([first, second], [first[:8], second[:8]])
        self.assertEqual(0, status)

        ambiguous_reference_frames = result["ambiguous_reference_frames"]
        self.assertEqual(2, ambiguous_reference_frames)


if __name__ == "__main__":
    unittest.main()
