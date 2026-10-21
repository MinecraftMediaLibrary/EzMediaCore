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


"""Specification validation and serializer tests, including literal wire examples."""

import random
import unittest

from rejection_cases import rejected_frames
import mcv2_reference
from mcv2_reference import Node, pack_frame, parse_frame

BYTE_VALUES = 1 << mcv2_reference.BITS_PER_BYTE
SPLIT_PROBABILITY = 0.8


class FormatTest(unittest.TestCase):
    def test_should_pack_and_parse_the_literal_frame_layout(self):
        expected = bytes.fromhex(
            '4d435632 03000000 0100 0100 09000000 09000000'
            '01000000 00000000 01000000 00000000 00000000 02 00000000 abcdef')
        actual = pack_frame(1, 1, 9, 9, {0: Node(mcv2_reference.SOLID, record=b'\xab\xcd\xef')})
        self.assertEqual(expected, actual)

        frame = parse_frame(expected)
        header = (frame.width, frame.height, frame.frame_id, frame.reference_id, frame.keyframe, frame.payload_start, frame.total)
        self.assertEqual((1, 1, 9, 9, True, 45, 48), header)

        leaf = frame.leaves[0]
        placement = (leaf.pixel_x, leaf.pixel_y, leaf.size, leaf.mode, leaf.quantizer, leaf.offset)
        self.assertEqual((0, 0, 32, mcv2_reference.SOLID, 0, 45), placement)

    def test_should_reject_a_frame_for_every_validation_rule(self):
        cases = rejected_frames()
        rules = {case['rule'] for case in cases.values()}
        self.assertEqual({'9.1', '9.3', '9.4', '9.5', '9.6'}, rules)

        for name, case in cases.items():
            with self.subTest(name=name, rule=case['rule']):
                with self.assertRaisesRegex(ValueError, case['reason']):
                    parse_frame(bytes.fromhex(case['frame']))

    def test_should_place_leaves_in_level_order_with_their_coordinates_and_offsets(self):
        solids = tuple(Node(mcv2_reference.SOLID, record=bytes([index, index, index])) for index in range(mcv2_reference.SPLIT_CHILDREN))
        fine = Node(mcv2_reference.SPLIT, children=solids)
        roots = {1: Node(mcv2_reference.SPLIT, children=(Node(mcv2_reference.SKIP), fine, Node(mcv2_reference.SKIP),
                                                         Node(mcv2_reference.SKIP))),
                 0: Node(mcv2_reference.SOLID, record=b'abc')}
        frame = parse_frame(pack_frame(65, 17, 1, 1, roots))
        level_counts = frame.level_counts
        self.assertEqual((2, 4, 4), level_counts)

        descriptors = frame.descriptors
        self.assertEqual(bytes([2, 6, 0, 6, 0, 0, 2, 2, 2, 2]), descriptors)

        walk = frame.walk
        self.assertEqual((0, 9 | 2 << mcv2_reference.WALK_CURSOR_BITS), walk)

        placements = [(leaf.pixel_x, leaf.pixel_y, leaf.size) for leaf in frame.leaves]
        self.assertEqual([(0, 0, 32), (32, 0, 16), (32, 16, 16), (48, 16, 16),
                          (48, 0, 8), (56, 0, 8), (48, 8, 8), (56, 8, 8), (64, 0, 32)], placements)

        parsed_roots = frame.roots
        self.assertEqual(roots, parsed_roots)

    def test_should_round_trip_whole_patterns_at_every_size(self):
        randomizer = random.Random(1541)
        def leaf(size):
            return Node(mcv2_reference.PATTERN, record=bytes(randomizer.randrange(BYTE_VALUES) for _ in range(mcv2_reference.COLORS_BYTES))
                        + bytes([randomizer.randrange(mcv2_reference.HORIZONTAL_STRIPES + 1)])
                        + bytes(randomizer.randrange(BYTE_VALUES) for _ in range(size // mcv2_reference.BITS_PER_BYTE)))
        def tree(size):
            if size > mcv2_reference.SMALLEST_LEAF_SIZE and randomizer.random() < SPLIT_PROBABILITY:
                children = tuple(tree(size // mcv2_reference.QUADRANTS_PER_SIDE) for _ in range(mcv2_reference.SPLIT_CHILDREN))
                return Node(mcv2_reference.SPLIT, children=children)
            return leaf(size)
        for _ in range(16):
            roots = {0: tree(mcv2_reference.SUPERBLOCK_SIZE), 1: leaf(mcv2_reference.SUPERBLOCK_SIZE),
                     2: tree(mcv2_reference.SUPERBLOCK_SIZE), 3: Node(mcv2_reference.SKIP)}
            data = pack_frame(51, 35, 44, 44, roots)
            frame = parse_frame(data)
            parsed_roots = frame.roots
            self.assertEqual(roots, parsed_roots)

            covered = frame.payload_start + sum(len(leaf.record) for leaf in frame.leaves)
            self.assertEqual(len(data), covered)

            repacked = pack_frame(frame.width, frame.height, frame.frame_id, frame.reference_id, frame.roots)
            self.assertEqual(data, repacked)

    def test_should_accept_present_skips_and_every_compact_quantizer(self):
        for quantizer in range(mcv2_reference.MAXIMUM_QUANTIZER + 1):
            frame = parse_frame(pack_frame(1, 1, 1, 0, {0: Node(mcv2_reference.COMPACT, quantizer, bytes(mcv2_reference.COMPACT_BYTES))}))
            quantizer_and_length = (frame.leaves[0].quantizer, len(frame.leaves[0].record))
            self.assertEqual((quantizer, mcv2_reference.COMPACT_BYTES), quantizer_and_length)

        frame = parse_frame(pack_frame(1, 1, 0, 0, {0: Node(mcv2_reference.SKIP)}))
        roots = frame.roots
        self.assertEqual({0: Node(mcv2_reference.SKIP)}, roots)

    def test_should_build_the_directory_at_the_maximum_dimensions(self):
        roots = {index: Node(mcv2_reference.SKIP) for index in (0, 31, 32, 255, 256, 16383)}
        frame = parse_frame(pack_frame(mcv2_reference.MAXIMUM_DIMENSION, mcv2_reference.MAXIMUM_DIMENSION, 0, 0, roots))
        directory = frame.directory[:3]
        self.assertEqual((0, 4, 5), directory)

        parsed_roots = frame.roots
        self.assertEqual(roots, parsed_roots)

        leaf_count = len(frame.leaves)
        self.assertEqual(16384, leaf_count)

    def test_should_include_the_frame_length_limit(self):
        roots = {index: Node(mcv2_reference.PALETTE, record=bytes(134)) for index in range(965)}
        roots[965] = Node(mcv2_reference.PATTERN, record=bytes(11))
        roots.update({index: Node(mcv2_reference.SOLID, record=bytes(mcv2_reference.SOLID_BYTES)) for index in range(966, 993)})
        data = pack_frame(1024, 1024, 0, 0, roots)
        length = len(data)
        self.assertEqual(mcv2_reference.MAXIMUM_FRAME_BYTES, length)

        total = parse_frame(data).total
        self.assertEqual(mcv2_reference.MAXIMUM_FRAME_BYTES, total)

        with self.assertRaisesRegex(ValueError, 'frame length'):
            parse_frame(data + b'\0')

        roots[993] = Node(mcv2_reference.SOLID, record=bytes(mcv2_reference.SOLID_BYTES))
        with self.assertRaisesRegex(ValueError, 'length limit'):
            pack_frame(1024, 1024, 0, 0, roots)

    def test_should_refuse_to_serialize_invalid_trees(self):
        bad = [
            Node(mcv2_reference.SPLIT),
            Node(mcv2_reference.SKIP, 1),
            Node(mcv2_reference.SKIP, record=b'x'),
            Node(mcv2_reference.SOLID, record=b'x'),
            Node(mcv2_reference.COMPACT, record=b'\0'),
            Node(mcv2_reference.COMPACT, record=bytes(mcv2_reference.COMPACT_BYTES + 1)),
            Node(mcv2_reference.COMPACT, mcv2_reference.MAXIMUM_QUANTIZER + 1, bytes(mcv2_reference.COMPACT_BYTES)),
            Node(mcv2_reference.SOLID, 1, bytes(mcv2_reference.SOLID_BYTES)),
            Node(mcv2_reference.PATTERN, record=bytes(2)),
            Node(mcv2_reference.PATTERN, record=bytes(12)),
            Node(mcv2_reference.PATTERN, record=bytes(mcv2_reference.COLORS_BYTES) + b'\2' + bytes(4)),
            Node(mcv2_reference.SKIP, children=(Node(mcv2_reference.SKIP),)),
            Node(mcv2_reference.MODE_MASK),
        ]
        too_deep_split = Node(mcv2_reference.SPLIT, children=(Node(mcv2_reference.SKIP),) * mcv2_reference.SPLIT_CHILDREN)
        for _ in range(2):
            too_deep_split = Node(mcv2_reference.SPLIT, children=(too_deep_split,) * mcv2_reference.SPLIT_CHILDREN)
        bad.append(too_deep_split)
        for node in bad:
            with self.subTest(node=node), self.assertRaises(ValueError):
                pack_frame(mcv2_reference.SUPERBLOCK_SIZE, mcv2_reference.SUPERBLOCK_SIZE, 1, 0, {0: node})

if __name__ == '__main__':
    unittest.main()
