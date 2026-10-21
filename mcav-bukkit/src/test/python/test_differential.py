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


"""Strict differential comparisons, with no exemptions or hidden parser failures."""

import hashlib
import json
import random
import tempfile
import unittest
from contextlib import redirect_stdout, redirect_stderr
from io import StringIO
from pathlib import Path
from types import SimpleNamespace
from unittest.mock import patch

import mcv2_reference
import mcv2_tools
from mcv2_reference import Node, pack_frame
from mcv2_tools import archive_bytes as archive


class DifferentialTest(unittest.TestCase):
    def report(self, frames, tokens):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            arguments = SimpleNamespace(out=output, java='unused', classpath='unused', seed=42)
            response = SimpleNamespace(returncode=0, stderr='', stdout=str(output / 'archives/tree-0000.mcs') + ' ' + ' '.join(tokens))
            with patch.object(mcv2_tools, 'differential_build_archives', return_value={'tree-0000': frames}),\
                    patch.object(mcv2_tools, 'differential_reference_tokens', return_value=['0' * 64] * len(frames)),\
                    patch.object(mcv2_tools.subprocess, 'run', return_value=response) as launch, redirect_stdout(StringIO()):
                status = mcv2_tools.differential_run(arguments)

            command = launch.call_args.args[0]
            self.assertEqual(['unused', '-cp', 'unused', 'me.brandonli.mcav.bukkit.media.mcv2.Mcv2Tools', 'digests',
                              str(output / 'archives/tree-0000.mcs')], command)

            return status, json.loads((output / 'summary.json').read_text())

    def test_should_not_exempt_supported_frames_as_unsupported(self):
        status, result = self.report([b'frame'], ['unsupported'])
        self.assertEqual(1, status)

        counts = result['counts']
        disagreements = counts['disagreements']
        self.assertEqual(1, disagreements)

        self.assertNotIn('unsupported_skipped', counts)

        decoded = counts['decoded']
        self.assertEqual(0, decoded)

    def test_should_compare_a_supported_control(self):
        status, result = self.report([b'frame'], ['0' * 64])
        self.assertEqual(0, status)

        decoded = result['counts']['decoded']
        self.assertEqual(1, decoded)

    def test_should_compare_every_frame_after_a_disagreement(self):
        status, result = self.report([b'one', b'two', b'three'], ['unsupported', 'reject', '0' * 64])
        self.assertEqual(1, status)

        counts = result['counts']
        frames = counts['frames']
        self.assertEqual(3, frames)

        disagreements = counts['disagreements']
        self.assertEqual(2, disagreements)

        decoded = counts['decoded']
        self.assertEqual(1, decoded)

        disagreeing_frames = [entry['frame'] for entry in result['disagreements']]
        self.assertEqual([0, 1], disagreeing_frames)

    def test_should_count_rejections_pixels_and_missing_and_extra_results(self):
        counts, failures = mcv2_tools.differential_compare({'same': ['reject', 'a' * 64, 'b' * 64], 'missing': ['reject']},
                                                {'same': ['reject', 'a' * 64, 'c' * 64], 'extra': ['reject']})
        frames = counts['frames']
        self.assertEqual(4, frames)

        decoded = counts['decoded']
        self.assertEqual(1, decoded)

        refused = counts['refused']
        self.assertEqual(1, refused)

        disagreements = counts['disagreements']
        self.assertEqual(4, disagreements)

        reference = failures[0]['reference']
        self.assertEqual('b' * 64, reference)

    def test_should_treat_only_a_value_error_of_the_reference_as_a_rejection(self):
        first = pack_frame(1, 1, 0, 0, {0: Node(mcv2_reference.SOLID, record=b'\1\2\3')})
        skipped = pack_frame(1, 1, 2, 1, {})
        recovery = pack_frame(1, 1, 3, 3, {0: Node(mcv2_reference.SOLID, record=b'\4\5\6')})
        tokens = mcv2_tools.differential_reference_tokens([first, b'MCV1', skipped, recovery])
        self.assertEqual([hashlib.sha256(b'\1\2\3').hexdigest(), 'reject', 'reject', hashlib.sha256(b'\4\5\6').hexdigest()], tokens)

        with patch.object(mcv2_tools.Decoder, 'accept', side_effect=RuntimeError('The decoder has a bug!')):
            with self.assertRaisesRegex(RuntimeError, 'bug'):
                mcv2_tools.differential_reference_tokens([first])

    def test_should_mutate_both_random_and_committed_inputs(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            original = pack_frame(1, 1, 0, 0, {0: Node(mcv2_reference.SOLID, record=b'\1\2\3')})
            (root / 'test.mcs').write_bytes(archive([original]))
            arguments = SimpleNamespace(seed=19, streams=2, conformance=2, mutants=2, corpus=root)
            chunks = mcv2_tools.differential_build_archives(arguments)
            archive_count = len(chunks)
            self.assertEqual(12, archive_count)

            conformance = chunks['conformance-0000-test']
            self.assertEqual([original], conformance)

            conformance_mutant = chunks['conformance-0000-test-mutant-0']
            self.assertNotEqual([original], conformance_mutant)

            tree_mutant = chunks['tree-0000-mutant-1']
            self.assertNotEqual(chunks['tree-0000'], tree_mutant)

            rebuilt = mcv2_tools.differential_build_archives(arguments)
            self.assertEqual(chunks, rebuilt)

    def test_should_truncate_and_change_bytes_when_mutating(self):
        original = [b'abcdefgh', b'ijklmnop']
        mutations = [mcv2_tools.differential_mutant(original, random.Random(seed)) for seed in range(100)]
        truncated = any(any(len(frame) < 8 for frame in chunks) for chunks in mutations)
        self.assertTrue(truncated)

        changed_in_place = any(all(len(frame) == 8 for frame in chunks) for chunks in mutations)
        self.assertTrue(changed_in_place)

        all_changed = all(chunks != original for chunks in mutations)
        self.assertTrue(all_changed)

        self.assertEqual([b'abcdefgh', b'ijklmnop'], original)

    def test_should_report_java_failures_as_findings(self):
        with tempfile.TemporaryDirectory() as directory:
            arguments = SimpleNamespace(out=Path(directory), java='unused', classpath='unused', seed=42)
            with patch.object(mcv2_tools, 'differential_build_archives', return_value={'tree-0000': [b'MCV1']}),\
                    patch.object(mcv2_tools.subprocess, 'run', return_value=SimpleNamespace(returncode=2, stderr='crash')),\
                    redirect_stderr(StringIO()):
                status = mcv2_tools.differential_run(arguments)
                self.assertEqual(2, status)


if __name__ == '__main__':
    unittest.main()
