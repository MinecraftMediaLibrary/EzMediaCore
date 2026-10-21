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
import json
import shutil
import random
import tempfile
import unittest
from contextlib import redirect_stderr
from io import StringIO
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
import mcv2_reference
import mcv2_tools
from mcv2_reference import Node, pack_frame, parse_frame


class FixtureCoverageTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.streams = mcv2_tools.edge_streams_build_streams()

    def test_should_cover_every_leaf_size_quantizer_vector_extreme_and_pattern_axis(self):
        modes, compacts, vectors, dimensions = set(), set(), set(), set()
        maximum_cursor = maximum_splits = maximum_length = 0
        for frames in self.streams.values():
            for data in frames:
                frame = parse_frame(data)
                dimensions.add((frame.width, frame.height))
                maximum_length = max(maximum_length, frame.total)
                for checkpoint in frame.walk:
                    maximum_cursor = max(maximum_cursor, checkpoint & 131071)
                    maximum_splits = max(maximum_splits, checkpoint >> 17)
                for leaf in frame.leaves:
                    modes.add((leaf.mode, leaf.size))
                    if leaf.mode == mcv2_reference.COMPACT:
                        compacts.add((leaf.size, leaf.quantizer))
                        vectors.add(leaf.record[:2])

        self.assertEqual({(mode, size) for mode in range(6) for size in (8, 16, 32)}, modes)

        self.assertEqual({(size, quantizer) for size in (8, 16, 32) for quantizer in range(3)}, compacts)

        extremes_covered = {b'\0\0', b'\x80\x7f', b'\x7f\x80', b'\x80\x80', b'\x7f\x7f'} <= vectors
        self.assertTrue(extremes_covered)

        dimensions_covered = {(1, 1), (1, 97), (97, 1), (97, 65), (4096, 4096)} <= dimensions
        self.assertTrue(dimensions_covered)

        self.assertGreater(maximum_cursor, 128000)

        self.assertGreater(maximum_splits, 20000)

        self.assertEqual(131071, maximum_length)

        patterns = {(leaf.size, leaf.record[6], leaf.record[7:]) for leaf in
                    parse_frame(self.streams['edge-patterns.mcs'][0]).leaves}
        for size in (8, 16, 32):
            for orientation in (0, 1):
                for axis in (0, 255, 0xA5):
                    self.assertIn((size, orientation, bytes([axis]) * (size // 8)), patterns)

    def test_should_commit_edge_streams_equal_to_the_deterministic_serializer_output(self):
        root = ROOT / 'mcav-bukkit/src/test/resources/mcv2/edge'
        committed = {path.name for path in root.glob('*.mcs')}
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

    def test_should_skip_version_2_and_check_version_3_conformance_without_touching_streams(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            output = root / 'conformance'
            output.mkdir()
            old_stream = mcv2_tools.archive_bytes([b'MCV2\2' + bytes(27)])
            old_digests = '{"old.mcs": {"unchanged": true}}\n'
            (output / 'old.mcs').write_bytes(old_stream)
            (output / 'digests.json').write_text(old_digests)
            messages = StringIO()
            with redirect_stderr(messages):
                mcv2_tools.fixtures_conformance(root)

            warnings = messages.getvalue()
            self.assertIn('old.mcs, which is not MCV2 version 3.', warnings)

            digests = (output / 'digests.json').read_text()
            self.assertEqual(old_digests, digests)

            data = pack_frame(1, 1, 3, 3, {0: Node(mcv2_reference.SOLID, record=bytes([10, 20, 30]))})
            archive = mcv2_tools.archive_bytes([data])
            (output / 'new.mcs').write_bytes(archive)
            with redirect_stderr(StringIO()):
                mcv2_tools.fixtures_conformance(root)
                mcv2_tools.fixtures_pages(root)

            result = json.loads((output / 'digests.json').read_text())
            old_result = result['old.mcs']
            self.assertEqual({'unchanged': True}, old_result)

            new_digests = result['new.mcs']['sha256_per_frame']
            self.assertEqual([hashlib.sha256(bytes([10, 20, 30])).hexdigest()], new_digests)

            old_bytes = (output / 'old.mcs').read_bytes()
            self.assertEqual(old_stream, old_bytes)

            new_bytes = (output / 'new.mcs').read_bytes()
            self.assertEqual(archive, new_bytes)

            pages = json.loads((output / 'pages.json').read_text())
            source = pages['source']
            self.assertEqual('committed v3 conformance streams', source)

            symbol_bits = pages['symbol_bits']
            self.assertEqual(6, symbol_bits)

            frame_count = len(pages['frames'])
            self.assertEqual(4, frame_count)

            streams = {entry['stream'] for entry in pages['frames']}
            self.assertEqual({'conformance/new.mcs'}, streams)

    def test_should_only_decode_encoder_fixtures_and_never_rewrite_them(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            output = root / 'encoder'
            output.mkdir()
            data = mcv2_tools.archive_bytes([pack_frame(1, 1, 0, 0, {0: Node(mcv2_reference.SOLID, record=bytes([1, 2, 3]))})])
            golden = output / 'golden.mcs'
            golden.write_bytes(data)
            with redirect_stderr(StringIO()):
                mcv2_tools.fixtures_encoder(root)

            golden_bytes = golden.read_bytes()
            self.assertEqual(data, golden_bytes)

            golden.write_bytes(mcv2_tools.archive_bytes([pack_frame(1, 1, 1, 0, {})]))
            with self.assertRaisesRegex(ValueError, 'reference'):
                mcv2_tools.fixtures_encoder(root)

    def test_should_cover_conformance_and_multi_page_edges_in_the_committed_pages(self):
        root = ROOT / 'mcav-bukkit/src/test/resources/mcv2'
        table = json.loads((root / 'conformance/pages.json').read_text())
        symbol_bits = table['symbol_bits']
        self.assertEqual(6, symbol_bits)

        frame_count = len(table['frames'])
        self.assertEqual(6, frame_count)

        page_counts = {len(entry['pages']) for entry in table['frames']}
        self.assertEqual({1, 2, 11}, page_counts)

        folders = {entry['stream'].split('/')[0] for entry in table['frames']}
        self.assertEqual({'conformance', 'edge'}, folders)


    def test_should_regenerate_pages_that_match_every_committed_vector_byte_for_byte(self):
        root = ROOT / 'mcav-bukkit/src/test/resources/mcv2'
        expected = (root / 'conformance/pages.json').read_bytes()
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            for name in ('conformance', 'edge'):
                shutil.copytree(root / name, output / name)
            mcv2_tools.fixtures_pages(output)
            regenerated = (output / 'conformance/pages.json').read_bytes()
            self.assertEqual(expected, regenerated)


if __name__ == '__main__':
    unittest.main()
