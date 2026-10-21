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


"""Checks mcv2_tools shader_timing without a GPU: its main() runs on a fake GL context, with a fake post chain
that decodes as each test says, so what the run counts as a failure is tested on any machine.

    python -m unittest discover -s mcav-bukkit/src/test/python -p test_shader_timing.py

Needs numpy, as shader_timing does; moderngl and the reference's make_pages are replaced.
"""

import json
import struct
import sys
import tempfile
import types
import unittest
from contextlib import redirect_stderr, redirect_stdout
from io import StringIO
from pathlib import Path
from unittest import mock

import mcv2_reference
import mcv2_tools

PROGRAM = "mcv2_tools.py shader_timing"
STREAM_NAME = "stream.mcs"
TIMINGS_NAME = "timings.json"
BASELINE_NAME = "baseline.json"
JSON_OPTION = "--json"

WIDTH = 64
HEIGHT = 32
KEYFRAME_ID = 1
PREDICTED_FRAME_ID = 2

VERSION_TWO = 2
VERSION_TWO_HEADER_BYTES = 32
VERSION_TWO_FLAGS_OFFSET = 6
DIMENSIONS = struct.Struct("<HH")

DECODE_MILLISECONDS = 1.0
SCREEN_MILLISECONDS = 2.0

MODERNGL_MODULE = "moderngl"
GL_VERSION = "3.3"

STREAMS_KEY = "streams"
NEW_KEYFRAME_KEY = "new_keyframe"
NEW_PREDICTED_KEY = "new_p"
TOTAL_KEY = "total"
COUNT_KEY = "n"

PACKAGE = "mcvideo"
PACKAGE_INIT = "__init__.py"
TRANSPORT_MODULE = "transport.py"


class ExitCodeTest(unittest.TestCase):
    def test_should_pass_a_run_whose_frames_all_decode_once_to_the_same_pictures(self):
        status = run()
        self.assertEqual(0, status)

    def test_should_fail_the_run_when_the_pages_of_a_frame_do_not_decode_it(self):
        status = run(missed={(1, 1)})
        self.assertEqual(1, status)

    def test_should_fail_the_run_when_a_frame_decodes_again_from_the_same_pages(self):
        status = run(again={(0, 0)})
        self.assertEqual(1, status)

    def test_should_fail_the_run_when_a_later_round_decodes_another_picture(self):
        status = run(changed={(2, 1)})
        self.assertEqual(1, status)

    def test_should_refuse_a_run_that_would_time_nothing(self):
        # the first round only warms the GPU up
        for options in (("--rounds", "1"), ("--repeats", "0"), ("--slots", "0")):
            with self.subTest(options=options):
                status = run(*options)
                self.assertEqual(2, status)


class TimerAccountingTest(unittest.TestCase):
    def test_should_count_each_copy_and_not_time_the_vanilla_outline_blit(self):
        steps = [
            ("mcv2_copy", "copy", {}, "mcav:mcv2_previous_0"),
            ("mcv2_copy", "copy", {}, "mcav:mcv2_state_0"),
            ("blit swap -> entity_outline", "vanilla", {}, "minecraft:entity_outline"),
        ]
        chain = object.__new__(mcv2_tools.TimedShaderChain)
        chain.context = types.SimpleNamespace(query=lambda **_: FakeQuery())
        chain.queries = {}
        chain.warm = lambda: None
        chain.steps = lambda: steps
        drawn = []
        chain.draw = lambda program, inputs, output: drawn.append(output)
        chain.target = lambda name: types.SimpleNamespace(read=lambda: b"\x01\x00\x00\x00")
        decoded, times = chain.timed_frame(5)
        self.assertTrue(decoded)

        self.assertEqual({"mcv2_copy -> previous_0": 2.0, "mcv2_copy -> state_0": 2.0}, times)

        total = sum(times.values())
        self.assertEqual(4.0, total)

        self.assertEqual(["mcav:mcv2_previous_0", "mcav:mcv2_state_0", "minecraft:entity_outline"], drawn)

    def test_should_classify_keyframes_and_predicted_frames_separately_by_their_version_three_ids(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / TIMINGS_NAME
            status = run(JSON_OPTION, str(path))
            self.assertEqual(0, status)

            stream = json.loads(path.read_text())[STREAMS_KEY][STREAM_NAME]
            keyframes = stream[NEW_KEYFRAME_KEY][TOTAL_KEY][COUNT_KEY]
            self.assertEqual(2, keyframes)

            predicted_frames = stream[NEW_PREDICTED_KEY][TOTAL_KEY][COUNT_KEY]
            self.assertEqual(2, predicted_frames)

            idle = stream["idle"][TOTAL_KEY][COUNT_KEY]
            self.assertEqual(4, idle)

    def test_should_still_classify_frames_by_the_flags_of_the_archived_version_two_baseline(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / BASELINE_NAME
            status = run(JSON_OPTION, str(path), version=VERSION_TWO)
            self.assertEqual(0, status)

            stream = json.loads(path.read_text())[STREAMS_KEY][STREAM_NAME]
            keyframes = stream[NEW_KEYFRAME_KEY][TOTAL_KEY][COUNT_KEY]
            self.assertEqual(2, keyframes)

            predicted_frames = stream[NEW_PREDICTED_KEY][TOTAL_KEY][COUNT_KEY]
            self.assertEqual(2, predicted_frames)

    def test_should_import_the_transport_of_the_reference_option_from_the_archived_package_directory(self):
        with tempfile.TemporaryDirectory() as directory:
            package = Path(directory) / PACKAGE
            package.mkdir()
            (package / PACKAGE_INIT).write_text("")
            (package / TRANSPORT_MODULE).write_text(
                "def make_pages(data, stream_id, symbol_bits):\n"
                "    return [b'archived-v2:' + bytes((stream_id, symbol_bits)) + data]\n"
            )
            received = []
            with mock.patch.object(sys, "path", sys.path.copy()), mock.patch.dict(sys.modules):
                sys.modules.pop(PACKAGE, None)
                sys.modules.pop(f"{PACKAGE}.transport", None)
                status = run("--reference", directory, version=VERSION_TWO, received=received)
                self.assertEqual(0, status)

            expected = [
                [b"archived-v2:\x07\x06" + frame(keyframe, VERSION_TWO)] for keyframe in (True, True, False, False)
            ]
            self.assertEqual(expected * 3, received)


def run(*options, missed=(), again=(), changed=(), version=mcv2_reference.VERSION, received=None):
    """Runs main() on a stream of a keyframe and a P frame, and returns its exit code; 0 when it returns."""
    context = types.SimpleNamespace(info={"GL_RENDERER": "fake", "GL_VERSION": GL_VERSION})
    moderngl = types.ModuleType(MODERNGL_MODULE)
    moderngl.create_standalone_context = lambda **_: context
    chain = type(
        "Chain",
        (FakeChain,),
        dict(missed=frozenset(missed), again=frozenset(again), changed=frozenset(changed), received=received),
    )
    argv = [PROGRAM, STREAM_NAME, *options]
    with (
        mock.patch.dict(sys.modules, {MODERNGL_MODULE: moderngl}),
        mock.patch.object(sys, "argv", argv),
        mock.patch.object(mcv2_tools, "make_pages", lambda data, stream_id, symbol_bits: [data]),
        mock.patch.object(mcv2_tools, "TimedShaderChain", chain),
        mock.patch.object(
            mcv2_tools, "read_archive", lambda stream: [frame(True, version), frame(False, version)]
        ),
        redirect_stdout(StringIO()),
        redirect_stderr(StringIO()),
    ):
        try:
            mcv2_tools.shader_timing_main()
        except SystemExit as exception:
            return exception.code

    return 0


class FakeChain:
    """A post chain that decodes every frame once, to a picture of its own, except where a test says otherwise. The
    sets hold (round, frame) pairs."""

    missed = frozenset()
    again = frozenset()
    changed = frozenset()
    received = None

    def __init__(self, context, width, height, slots):
        self.round = -1
        self.index = 0
        self.first = True

    def reset(self):
        self.round += 1
        self.index = 0
        self.first = True

    def show(self, pages):
        if self.received is not None:
            self.received.append(pages)

    def timed_frame(self, repeats):
        # each frame is shown twice: with its new pages, which decode it, then with the same pages again
        key = (self.round, self.index)
        times = {"mcv2_decode": DECODE_MILLISECONDS, "mcv2_screen": SCREEN_MILLISECONDS}
        if self.first:
            self.first = False
            return key not in self.missed, times

        self.first = True
        return key in self.again, times

    def target(self, name):
        key = (self.round, self.index)
        self.index += 1
        picture = b"another picture" if key in self.changed else b"picture %d" % key[1]
        return types.SimpleNamespace(read=lambda: picture)


def frame(keyframe, version=mcv2_reference.VERSION):
    """The header of a 64x32 frame, which is all shader_timing reads of a frame itself."""
    if version == mcv2_reference.VERSION:
        frame_id = KEYFRAME_ID if keyframe else PREDICTED_FRAME_ID
        return mcv2_reference.HEADER.pack(mcv2_reference.MAGIC, mcv2_reference.VERSION, WIDTH, HEIGHT, frame_id, KEYFRAME_ID)

    data = bytearray(VERSION_TWO_HEADER_BYTES)
    data[:len(mcv2_reference.MAGIC)] = mcv2_reference.MAGIC
    data[mcv2_reference.VERSION_OFFSET] = version
    data[VERSION_TWO_FLAGS_OFFSET] = int(keyframe)
    DIMENSIONS.pack_into(data, mcv2_reference.WIDTH_OFFSET, WIDTH, HEIGHT)
    return bytes(data)


class FakeQuery:
    elapsed = 2_000_000

    def __enter__(self):
        return self

    def __exit__(self, *unused):
        pass


if __name__ == "__main__":
    unittest.main()
