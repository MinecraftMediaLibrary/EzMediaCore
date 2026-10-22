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


"""Version 3 fixture coverage, archive framing and Java-owned fixture preservation."""

import hashlib
import itertools
import json
import shutil
import random
import tempfile
import unittest
from contextlib import redirect_stderr
from io import StringIO
from pathlib import Path

import mcv2_reference
import mcv2_tools
from mcv2_reference import Node, pack_frame, parse_frame

ROOT = Path(__file__).resolve().parents[4]
RESOURCES = ROOT / 'mcav-bukkit/src/test/resources/mcv2'
STREAM_PATTERN = '*.mcs'
STREAM_SEPARATOR = '/'

CONFORMANCE_FOLDER = 'conformance'
EDGE_FOLDER = 'edge'
ENCODER_FOLDER = 'encoder'
PAGES_NAME = 'pages.json'
DIGESTS_NAME = 'digests.json'
PATTERNS_STREAM = 'edge-patterns.mcs'
OLD_STREAM = 'old.mcs'
NEW_STREAM = 'new.mcs'
GOLDEN_STREAM = 'golden.mcs'

SYMBOL_BITS_KEY = 'symbol_bits'
FRAMES_KEY = 'frames'
STREAM_KEY = 'stream'

OLD_STREAM_BYTES = mcv2_tools.archive_bytes([b'MCV2\2' + bytes(27)])
OLD_DIGESTS = '{"old.mcs": {"unchanged": true}}\n'

WALK_CURSOR_MASK = (1 << mcv2_reference.WALK_CURSOR_BITS) - 1
VECTOR_BYTES = mcv2_reference.COMPACT_BYTES - mcv2_reference.GRID_BYTES


class FixtureCoverageTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.streams = mcv2_tools.edge_streams_build_streams()

    def test_should_cover_every_leaf_size_quantizer_vector_extreme_and_pattern_axis(self):
        frames = [parse_frame(data) for stream in self.streams.values() for data in stream]
        leaves = [leaf for frame in frames for leaf in frame.leaves]
        modes = {(leaf.mode, leaf.size) for leaf in leaves}
        self.assertEqual({(mode, size) for mode in range(mcv2_reference.SPLIT) for size in mcv2_reference.LEAF_SIZES}, modes)

        compact_leaves = [leaf for leaf in leaves if leaf.mode == mcv2_reference.COMPACT]
        compacts = {(leaf.size, leaf.quantizer) for leaf in compact_leaves}
        quantizers = range(mcv2_reference.MAXIMUM_QUANTIZER + 1)
        self.assertEqual({(size, quantizer) for size in mcv2_reference.LEAF_SIZES for quantizer in quantizers}, compacts)

        vectors = {leaf.record[:VECTOR_BYTES] for leaf in compact_leaves}
        extremes_covered = {b'\0\0', b'\x80\x7f', b'\x7f\x80', b'\x80\x80', b'\x7f\x7f'} <= vectors
        self.assertTrue(extremes_covered)

        dimensions = {(frame.width, frame.height) for frame in frames}
        dimensions_covered = {(1, 1), (1, 97), (97, 1), (97, 65), (4096, 4096)} <= dimensions
        self.assertTrue(dimensions_covered)

        checkpoints = [checkpoint for frame in frames for checkpoint in frame.walk]
        maximum_cursor = max(checkpoint & WALK_CURSOR_MASK for checkpoint in checkpoints)
        self.assertGreater(maximum_cursor, 128000)

        maximum_splits = max(checkpoint >> mcv2_reference.WALK_CURSOR_BITS for checkpoint in checkpoints)
        self.assertGreater(maximum_splits, 20000)

        maximum_length = max(frame.total for frame in frames)
        self.assertEqual(mcv2_reference.MAXIMUM_FRAME_BYTES, maximum_length)

        pattern_leaves = parse_frame(self.streams[PATTERNS_STREAM][0]).leaves
        patterns = {(leaf.size, leaf.record[mcv2_reference.PATTERN_ORIENTATION_INDEX], leaf.record[mcv2_reference.PATTERN_HEADER_BYTES:])
                    for leaf in pattern_leaves}
        orientations = (mcv2_reference.VERTICAL_STRIPES, mcv2_reference.HORIZONTAL_STRIPES)
        for size, orientation, axis in itertools.product(mcv2_reference.LEAF_SIZES, orientations, (0, 255, 0xA5)):
            pattern = (size, orientation, bytes([axis]) * (size // mcv2_reference.BITS_PER_BYTE))
            self.assertIn(pattern, patterns)

    def test_should_commit_edge_streams_equal_to_the_deterministic_serializer_output(self):
        root = RESOURCES / EDGE_FOLDER
        committed = {path.name for path in root.glob(STREAM_PATTERN)}
        self.assertEqual(set(self.streams), committed)

        for name, frames in self.streams.items():
            with self.subTest(stream=name):
                archive = mcv2_tools.archive_bytes(frames)
                self.assertEqual((root / name).read_bytes(), archive)

    def test_should_reproduce_and_decode_random_streams(self):
        left = mcv2_tools.edge_streams_random_stream(random.Random(57))
        right = mcv2_tools.edge_streams_random_stream(random.Random(57))
        self.assertEqual(left, right)

        digest_count = len(mcv2_tools.fixtures_digests(mcv2_tools.archive_bytes(left)))
        self.assertEqual(6, digest_count)


class FixtureToolTest(unittest.TestCase):
    def test_should_round_trip_archives_and_reject_truncations(self):
        values = [b'abcd', b'', b'MCV2']
        frames = list(mcv2_tools.archive_frames(mcv2_tools.archive_bytes(values)))
        self.assertEqual(values, frames)

        for raw in (b'\1', b'\1\0', b'\1\0\0', b'\2\0\0\0a'):
            with self.subTest(raw=raw), self.assertRaises(ValueError):
                list(mcv2_tools.archive_frames(raw))

    def test_should_skip_version_2_streams_with_a_warning(self):
        root = self.create_conformance_root()
        messages = StringIO()
        with redirect_stderr(messages):
            mcv2_tools.fixtures_conformance(root)

        warnings = messages.getvalue()
        self.assertIn('old.mcs, which is not MCV2 version 3.', warnings)

        digests = (root / CONFORMANCE_FOLDER / DIGESTS_NAME).read_text()
        self.assertEqual(OLD_DIGESTS, digests)

    def test_should_check_version_3_conformance_without_touching_streams(self):
        root = self.create_conformance_root()
        output = root / CONFORMANCE_FOLDER
        data = pack_frame(1, 1, 3, 3, {0: Node(mcv2_reference.SOLID, record=bytes([10, 20, 30]))})
        archive = mcv2_tools.archive_bytes([data])
        (output / NEW_STREAM).write_bytes(archive)
        with redirect_stderr(StringIO()):
            mcv2_tools.fixtures_conformance(root)
            mcv2_tools.fixtures_pages(root)

        result = json.loads((output / DIGESTS_NAME).read_text())
        old_result = result[OLD_STREAM]
        self.assertEqual({'unchanged': True}, old_result)

        new_digests = result[NEW_STREAM]['sha256_per_frame']
        self.assertEqual([hashlib.sha256(bytes([10, 20, 30])).hexdigest()], new_digests)

        old_bytes = (output / OLD_STREAM).read_bytes()
        self.assertEqual(OLD_STREAM_BYTES, old_bytes)

        new_bytes = (output / NEW_STREAM).read_bytes()
        self.assertEqual(archive, new_bytes)

        pages = json.loads((output / PAGES_NAME).read_text())
        source = pages['source']
        self.assertEqual('committed v3 conformance streams', source)

        symbol_bits = pages[SYMBOL_BITS_KEY]
        self.assertEqual(mcv2_reference.SYMBOL_BITS, symbol_bits)

        frame_count = len(pages[FRAMES_KEY])
        self.assertEqual(4, frame_count)

        streams = {entry[STREAM_KEY] for entry in pages[FRAMES_KEY]}
        self.assertEqual({'conformance/new.mcs'}, streams)

    def test_should_only_decode_encoder_fixtures_and_never_rewrite_them(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            output = root / ENCODER_FOLDER
            output.mkdir()
            data = mcv2_tools.archive_bytes([pack_frame(1, 1, 0, 0, {0: Node(mcv2_reference.SOLID, record=bytes([1, 2, 3]))})])
            golden = output / GOLDEN_STREAM
            golden.write_bytes(data)
            with redirect_stderr(StringIO()):
                mcv2_tools.fixtures_encoder(root)

            golden_bytes = golden.read_bytes()
            self.assertEqual(data, golden_bytes)

            golden.write_bytes(mcv2_tools.archive_bytes([pack_frame(1, 1, 1, 0, {})]))
            with self.assertRaisesRegex(ValueError, 'reference'):
                mcv2_tools.fixtures_encoder(root)

    def test_should_cover_conformance_and_multi_page_edges_in_the_committed_pages(self):
        table = json.loads((RESOURCES / CONFORMANCE_FOLDER / PAGES_NAME).read_text())
        symbol_bits = table[SYMBOL_BITS_KEY]
        self.assertEqual(mcv2_reference.SYMBOL_BITS, symbol_bits)

        frame_count = len(table[FRAMES_KEY])
        self.assertEqual(6, frame_count)

        page_counts = {len(entry['pages']) for entry in table[FRAMES_KEY]}
        self.assertEqual({1, 2, 11}, page_counts)

        folders = {entry[STREAM_KEY].split(STREAM_SEPARATOR)[0] for entry in table[FRAMES_KEY]}
        self.assertEqual({CONFORMANCE_FOLDER, EDGE_FOLDER}, folders)

    def test_should_regenerate_pages_that_match_every_committed_vector_byte_for_byte(self):
        expected = (RESOURCES / CONFORMANCE_FOLDER / PAGES_NAME).read_bytes()
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            for name in (CONFORMANCE_FOLDER, EDGE_FOLDER):
                shutil.copytree(RESOURCES / name, output / name)
            mcv2_tools.fixtures_pages(output)
            regenerated = (output / CONFORMANCE_FOLDER / PAGES_NAME).read_bytes()
            self.assertEqual(expected, regenerated)

    def create_conformance_root(self):
        root = Path(self.enterContext(tempfile.TemporaryDirectory()))
        output = root / CONFORMANCE_FOLDER
        output.mkdir()
        (output / OLD_STREAM).write_bytes(OLD_STREAM_BYTES)
        (output / DIGESTS_NAME).write_text(OLD_DIGESTS)

        return root


if __name__ == '__main__':
    unittest.main()
