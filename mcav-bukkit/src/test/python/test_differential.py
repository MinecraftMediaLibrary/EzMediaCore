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

PLACEHOLDER = 'unused'
SEED = 42

ARCHIVE_NAME = 'tree-0000'
ARCHIVE_PATH = f'archives/{ARCHIVE_NAME}.mcs'
SUMMARY_NAME = 'summary.json'
CORPUS_NAME = 'test.mcs'

TOOLS_CLASS = 'me.brandonli.mcav.bukkit.media.mcv2.Mcv2Tools'
TOKEN_SEPARATOR = ' '

BUILD_ARCHIVES = 'differential_build_archives'
RUN_FUNCTION = 'run'

DIGEST_DIGITS = 64
REFERENCE_DIGEST = '0' * DIGEST_DIGITS

UNSUPPORTED = 'unsupported'
REJECT = 'reject'

FRAME = b'frame'
FIRST_RECORD = b'\1\2\3'
FOREIGN_FRAME = b'MCV1'

COUNTS_KEY = 'counts'
DISAGREEMENTS_KEY = 'disagreements'
DECODED_KEY = 'decoded'
FRAMES_KEY = 'frames'


class DifferentialTest(unittest.TestCase):
    def test_should_not_exempt_supported_frames_as_unsupported(self):
        status, result = self.report([FRAME], [UNSUPPORTED])
        self.assertEqual(1, status)

        counts = result[COUNTS_KEY]
        disagreements = counts[DISAGREEMENTS_KEY]
        self.assertEqual(1, disagreements)

        self.assertNotIn('unsupported_skipped', counts)

        decoded = counts[DECODED_KEY]
        self.assertEqual(0, decoded)

    def test_should_compare_a_supported_control(self):
        status, result = self.report([FRAME], [REFERENCE_DIGEST])
        self.assertEqual(0, status)

        decoded = result[COUNTS_KEY][DECODED_KEY]
        self.assertEqual(1, decoded)

    def test_should_compare_every_frame_after_a_disagreement(self):
        status, result = self.report([b'one', b'two', b'three'], [UNSUPPORTED, REJECT, REFERENCE_DIGEST])
        self.assertEqual(1, status)

        counts = result[COUNTS_KEY]
        frames = counts[FRAMES_KEY]
        self.assertEqual(3, frames)

        disagreements = counts[DISAGREEMENTS_KEY]
        self.assertEqual(2, disagreements)

        decoded = counts[DECODED_KEY]
        self.assertEqual(1, decoded)

        disagreeing_frames = [entry['frame'] for entry in result[DISAGREEMENTS_KEY]]
        self.assertEqual([0, 1], disagreeing_frames)

    def test_should_count_rejections_pixels_and_missing_and_extra_results(self):
        expected = {'same': [REJECT, 'a' * DIGEST_DIGITS, 'b' * DIGEST_DIGITS], 'missing': [REJECT]}
        actual = {'same': [REJECT, 'a' * DIGEST_DIGITS, 'c' * DIGEST_DIGITS], 'extra': [REJECT]}
        counts, failures = mcv2_tools.differential_compare(expected, actual)
        frames = counts[FRAMES_KEY]
        self.assertEqual(4, frames)

        decoded = counts[DECODED_KEY]
        self.assertEqual(1, decoded)

        refused = counts['refused']
        self.assertEqual(1, refused)

        disagreements = counts[DISAGREEMENTS_KEY]
        self.assertEqual(4, disagreements)

        reference = failures[0]['reference']
        self.assertEqual('b' * DIGEST_DIGITS, reference)

    def test_should_treat_only_a_value_error_of_the_reference_as_a_rejection(self):
        first = pack_frame(1, 1, 0, 0, {0: Node(mcv2_reference.SOLID, record=FIRST_RECORD)})
        skipped = pack_frame(1, 1, 2, 1, {})
        recovery = pack_frame(1, 1, 3, 3, {0: Node(mcv2_reference.SOLID, record=b'\4\5\6')})
        tokens = mcv2_tools.differential_reference_tokens([first, FOREIGN_FRAME, skipped, recovery])
        self.assertEqual([hashlib.sha256(FIRST_RECORD).hexdigest(), REJECT, REJECT, hashlib.sha256(b'\4\5\6').hexdigest()], tokens)

        with patch.object(mcv2_tools.Decoder, 'accept', side_effect=RuntimeError('The decoder has a bug!')):
            with self.assertRaisesRegex(RuntimeError, 'bug'):
                mcv2_tools.differential_reference_tokens([first])

    def test_should_mutate_both_random_and_committed_inputs(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            original = pack_frame(1, 1, 0, 0, {0: Node(mcv2_reference.SOLID, record=FIRST_RECORD)})
            (root / CORPUS_NAME).write_bytes(archive([original]))
            arguments = SimpleNamespace(seed=19, streams=2, conformance=2, mutants=2, corpus=root)
            chunks = mcv2_tools.differential_build_archives(arguments)
            archive_count = len(chunks)
            self.assertEqual(12, archive_count)

            conformance = chunks['conformance-0000-test']
            self.assertEqual([original], conformance)

            conformance_mutant = chunks['conformance-0000-test-mutant-0']
            self.assertNotEqual([original], conformance_mutant)

            tree_mutant = chunks['tree-0000-mutant-1']
            self.assertNotEqual(chunks[ARCHIVE_NAME], tree_mutant)

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
            arguments = SimpleNamespace(out=Path(directory), java=PLACEHOLDER, classpath=PLACEHOLDER, seed=SEED)
            with patch.object(mcv2_tools, BUILD_ARCHIVES, return_value={ARCHIVE_NAME: [FOREIGN_FRAME]}),\
                    patch.object(mcv2_tools.subprocess, RUN_FUNCTION, return_value=SimpleNamespace(returncode=2, stderr='crash')),\
                    redirect_stderr(StringIO()):
                status = mcv2_tools.differential_run(arguments)
                self.assertEqual(2, status)

    def report(self, frames, tokens):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            arguments = SimpleNamespace(out=output, java=PLACEHOLDER, classpath=PLACEHOLDER, seed=SEED)
            archive_path = str(output / ARCHIVE_PATH)
            response = SimpleNamespace(returncode=0, stderr='', stdout=archive_path + TOKEN_SEPARATOR + TOKEN_SEPARATOR.join(tokens))
            with patch.object(mcv2_tools, BUILD_ARCHIVES, return_value={ARCHIVE_NAME: frames}),\
                    patch.object(mcv2_tools, 'differential_reference_tokens', return_value=[REFERENCE_DIGEST] * len(frames)),\
                    patch.object(mcv2_tools.subprocess, RUN_FUNCTION, return_value=response) as launch, redirect_stdout(StringIO()):
                status = mcv2_tools.differential_run(arguments)

            command = launch.call_args.args[0]
            self.assertEqual([PLACEHOLDER, '-cp', PLACEHOLDER, TOOLS_CLASS, 'digests', archive_path], command)

            return status, json.loads((output / SUMMARY_NAME).read_text())


if __name__ == '__main__':
    unittest.main()
