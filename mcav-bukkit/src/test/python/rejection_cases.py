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

"""Malformed frames shared by the validation tests and the public rejected corpus."""

import struct

import mcv2_reference
from mcv2_reference import Node, pack_frame, parse_frame

BYTE_ORDER = '<'
BYTE_CODE = 'B'
HALF_WORD_CODE = 'H'
WORD_CODE = 'I'
WORD = struct.Struct(BYTE_ORDER + WORD_CODE)

FRAME_KEY = 'frame'

HEADER_RULE = '9.1'
INDEX_RULE = '9.3'
WALK_RULE = '9.4'
DESCRIPTOR_RULE = '9.5'
END_RULE = '9.6'

FRAME_LENGTH_REASON = 'frame length'
NOT_MCV2_REASON = 'not an MCV2'
DIRECTORY_PREFIX_REASON = 'directory prefix'
TRUNCATED_DESCRIPTORS_REASON = 'truncated descriptors'
WALK_CHECKPOINT_REASON = 'walk checkpoint'
PATTERN_ORIENTATION_REASON = 'PATTERN orientation'

# Index offsets of a frame with a single presence mask.
MASK_OFFSET = mcv2_reference.HEADER_BYTES
DIRECTORY_OFFSET = MASK_OFFSET + mcv2_reference.WORD_BYTES
COUNTS_OFFSET = DIRECTORY_OFFSET + mcv2_reference.WORD_BYTES
DESCRIPTORS_OFFSET = COUNTS_OFFSET + mcv2_reference.LEVEL_COUNTS.size

TWO_LEVEL_DESCRIPTORS = 1 + mcv2_reference.SPLIT_CHILDREN + mcv2_reference.SPLIT_CHILDREN * mcv2_reference.SPLIT_CHILDREN
TWO_LEVEL_WALK_OFFSET = DESCRIPTORS_OFFSET + TWO_LEVEL_DESCRIPTORS

WALK_SPLIT = 1 << mcv2_reference.WALK_CURSOR_BITS
QUANTIZER_STEP = 1 << mcv2_reference.QUANTIZER_SHIFT
QUANTIZER_VALUES = 1 << (mcv2_reference.BITS_PER_BYTE - mcv2_reference.QUANTIZER_SHIFT)
INVALID_ORIENTATION = mcv2_reference.HORIZONTAL_STRIPES + 1


def rejected_frames():
    cases = {}
    _add_header_cases(cases)
    _add_index_cases(cases)
    _add_walk_cases(cases)
    _add_descriptor_cases(cases)
    _add(cases, 'payload-gap', _solid_frame() + b'\0', END_RULE, 'records do not end')

    return cases


def _add_header_cases(cases):
    empty = _frame()
    _add(cases, 'short-frame', bytes(mcv2_reference.MINIMUM_FRAME_BYTES - 1), HEADER_RULE, FRAME_LENGTH_REASON)
    _add(cases, 'oversize-frame', bytes(mcv2_reference.MAXIMUM_FRAME_BYTES + 1), HEADER_RULE, FRAME_LENGTH_REASON)
    _add(cases, 'bad-magic', b'ABCD' + empty[len(mcv2_reference.MAGIC):], HEADER_RULE, NOT_MCV2_REASON)
    unknown_version = changed(empty, mcv2_reference.VERSION_OFFSET, mcv2_reference.VERSION + 1)
    _add(cases, 'unknown-version', unknown_version, HEADER_RULE, NOT_MCV2_REASON)
    for offset in range(mcv2_reference.VERSION_OFFSET + 1, mcv2_reference.WIDTH_OFFSET):
        _add(cases, f'version-byte-{offset}', changed(empty, offset, 1), HEADER_RULE, NOT_MCV2_REASON)
    _add_retired_version_cases(cases)
    for offset in (mcv2_reference.WIDTH_OFFSET, mcv2_reference.HEIGHT_OFFSET):
        for value in (0, mcv2_reference.MAXIMUM_DIMENSION + 1):
            _add(cases, f'dimension-{offset}-{value}', changed(empty, offset, value, HALF_WORD_CODE), HEADER_RULE, 'dimensions')


def _frame(node=None):
    return pack_frame(1, 1, 0, 0, {} if node is None else {0: node})


def _add(cases, name, data, rule, reason):
    cases[name] = {FRAME_KEY: data.hex(), 'rule': rule, 'reason': reason}


def changed(data, offset, value, code=BYTE_CODE):
    result = bytearray(data)
    struct.pack_into(BYTE_ORDER + code, result, offset, value)
    return bytes(result)


def _add_retired_version_cases(cases):
    version_1 = bytes.fromhex(
        '4d4356310102010001000100090000000900000000000000010000003400000037000000'
        '00000000000000000000000034000002102030')
    _add(cases, 'mcv1', version_1, HEADER_RULE, 'version 1 is no longer supported')
    version_2 = bytes.fromhex(
        '4d4356320205730001000100000000000000000000000000010000004500000075000000000000000000000000000000010000000000000001000000'
        '0000010600000000003224e3e9bafb4931c49e41fca6fb71c25d4744121177650106704ecf2c3e8fa04eaa462d163cd0d18bc59cbc0fbaead8')
    _add(cases, 'version-2', version_2, HEADER_RULE, 'version 2 is no longer supported')


def _add_index_cases(cases):
    _add_presence_cases(cases)
    _add_count_cases(cases)
    _add(cases, 'truncated-descriptor-walk', _solid_frame()[:44], INDEX_RULE, TRUNCATED_DESCRIPTORS_REASON)


def _add_presence_cases(cases):
    empty = _frame()
    for length in (20, 24, 28, 32, 36, 39):
        _add(cases, f'truncated-index-{length}', empty[:length], INDEX_RULE, 'truncated index')
    _add(cases, 'unused-mask-bit', changed(empty, MASK_OFFSET, 2, WORD_CODE), INDEX_RULE, 'presence mask')
    _add(cases, 'directory-zero', changed(empty, DIRECTORY_OFFSET, 1, WORD_CODE), INDEX_RULE, DIRECTORY_PREFIX_REASON)

    wide = pack_frame(4096, 65, 0, 0, {0: Node(mcv2_reference.SKIP), 256: Node(mcv2_reference.SKIP)})
    later_prefix = MASK_OFFSET + 12 * mcv2_reference.WORD_BYTES + mcv2_reference.WORD_BYTES
    _add(cases, 'directory-later-prefix', changed(wide, later_prefix, 0, WORD_CODE), INDEX_RULE, DIRECTORY_PREFIX_REASON)


def _add_count_cases(cases):
    _add(cases, 'n0-popcount', changed(_solid_frame(), COUNTS_OFFSET, 0, WORD_CODE), INDEX_RULE, 'level 0 count')
    level_one_count = COUNTS_OFFSET + mcv2_reference.WORD_BYTES
    count_overflow = changed(_frame(), level_one_count, 0xFFFFFFFF, WORD_CODE)
    _add(cases, 'count-overflow', count_overflow, INDEX_RULE, TRUNCATED_DESCRIPTORS_REASON)

    split = _skip_split()
    branch = _frame(split)
    _add(cases, 'n1-children', changed(branch, DESCRIPTORS_OFFSET, mcv2_reference.SKIP), INDEX_RULE, 'level 1 count')

    deep = _frame(Node(mcv2_reference.SPLIT, children=(split,) * mcv2_reference.SPLIT_CHILDREN))
    _add(cases, 'n2-children', _without_level_one_split(deep), INDEX_RULE, 'level 2 count')
    _add(cases, 'split-at-level-2', changed(deep, DESCRIPTORS_OFFSET + 5, mcv2_reference.SPLIT), INDEX_RULE, 'SPLIT in level 2')


def _solid_frame():
    return _frame(Node(mcv2_reference.SOLID, record=b'\x10\x20\x30'))


def _skip_split():
    return Node(mcv2_reference.SPLIT, children=(Node(mcv2_reference.SKIP),) * mcv2_reference.SPLIT_CHILDREN)


def _without_level_one_split(deep):
    data = bytearray(changed(deep, DESCRIPTORS_OFFSET + 1, mcv2_reference.SKIP))
    # Removing that split also changes the later walk counts; repair those so this isolates n2.
    for offset in (TWO_LEVEL_WALK_OFFSET + mcv2_reference.WORD_BYTES, TWO_LEVEL_WALK_OFFSET + 2 * mcv2_reference.WORD_BYTES):
        value = WORD.unpack_from(data, offset)[0]
        WORD.pack_into(data, offset, value - WALK_SPLIT)

    return bytes(data)


def _add_walk_cases(cases):
    _add(cases, 'walk-zero', changed(_solid_frame(), DESCRIPTORS_OFFSET + 1, 1, WORD_CODE), WALK_RULE, WALK_CHECKPOINT_REASON)
    solid_leaves = (Node(mcv2_reference.SOLID, record=bytes(mcv2_reference.SOLID_BYTES)),) * mcv2_reference.SPLIT_CHILDREN
    solid_split = Node(mcv2_reference.SPLIT, children=solid_leaves)
    long = _frame(Node(mcv2_reference.SPLIT, children=(solid_split,) * mcv2_reference.SPLIT_CHILDREN))
    offset = TWO_LEVEL_WALK_OFFSET + mcv2_reference.WORD_BYTES
    checkpoint = WORD.unpack_from(long, offset)[0]
    _add(cases, 'walk-cursor', changed(long, offset, checkpoint + 1, WORD_CODE), WALK_RULE, WALK_CHECKPOINT_REASON)
    _add(cases, 'walk-splits', changed(long, offset, checkpoint + WALK_SPLIT, WORD_CODE), WALK_RULE, WALK_CHECKPOINT_REASON)


def _add_descriptor_cases(cases):
    _add_mode_cases(cases)
    _add_quantizer_cases(cases)
    _add_keyframe_mode_cases(cases)
    _add_truncated_record_cases(cases)
    _add_orientation_cases(cases)


def _add_mode_cases(cases):
    solid = _solid_frame()
    for mode in range(mcv2_reference.SPLIT + 1, mcv2_reference.MODE_MASK + 1):
        _add(cases, f'invalid-mode-{mode}', changed(solid, DESCRIPTORS_OFFSET, mode), DESCRIPTOR_RULE, 'descriptor mode')


def _add_quantizer_cases(cases):
    solid = _solid_frame()
    for mode in (
        mcv2_reference.SKIP,
        mcv2_reference.MOTION,
        mcv2_reference.SOLID,
        mcv2_reference.PALETTE,
        mcv2_reference.PATTERN,
        mcv2_reference.SPLIT,
    ):
        _add(cases, f'quantizer-mode-{mode}', changed(solid, DESCRIPTORS_OFFSET, mode | QUANTIZER_STEP), DESCRIPTOR_RULE, 'quantizer')

    compact = _predicted_frame(Node(mcv2_reference.COMPACT, record=bytes(mcv2_reference.COMPACT_BYTES)))
    for quantizer in range(mcv2_reference.MAXIMUM_QUANTIZER + 1, QUANTIZER_VALUES):
        descriptor = mcv2_reference.COMPACT | quantizer << mcv2_reference.QUANTIZER_SHIFT
        data = changed(compact, DESCRIPTORS_OFFSET, descriptor)
        _add(cases, f'compact-quantizer-{quantizer}', data, DESCRIPTOR_RULE, 'COMPACT quantizer above 2')


def _predicted_frame(node):
    return pack_frame(1, 1, 1, 0, {0: node})


def _add_keyframe_mode_cases(cases):
    solid = _solid_frame()
    for mode in (mcv2_reference.MOTION, mcv2_reference.COMPACT):
        _add(cases, f'keyframe-mode-{mode}', changed(solid, DESCRIPTORS_OFFSET, mode), DESCRIPTOR_RULE, 'temporal mode')


def _add_truncated_record_cases(cases):
    nodes = [Node(mcv2_reference.MOTION, record=bytes(mcv2_reference.MOTION_BYTES)),
             Node(mcv2_reference.SOLID, record=bytes(mcv2_reference.SOLID_BYTES)),
             Node(mcv2_reference.PALETTE, record=bytes(134)), Node(mcv2_reference.PATTERN, record=bytes(11)),
             Node(mcv2_reference.COMPACT, record=bytes(mcv2_reference.COMPACT_BYTES))]
    for node in nodes:
        data = _predicted_frame(node)
        _add(cases, f'truncated-record-{node.mode}', data[:-1], DESCRIPTOR_RULE, 'record exceeds')


def _add_orientation_cases(cases):
    pattern = _frame(Node(mcv2_reference.PATTERN, record=bytes(11)))
    _add(cases, 'pattern-orientation', _with_invalid_orientation(pattern, INVALID_ORIENTATION), DESCRIPTOR_RULE, PATTERN_ORIENTATION_REASON)

    offscreen_children = (
        Node(mcv2_reference.SKIP),
        Node(mcv2_reference.SKIP),
        Node(mcv2_reference.SKIP),
        Node(mcv2_reference.PATTERN, record=bytes(9)),
    )
    offscreen = _frame(Node(mcv2_reference.SPLIT, children=offscreen_children))
    offscreen_orientation = _with_invalid_orientation(offscreen, INVALID_ORIENTATION)
    _add(cases, 'off-picture-orientation', offscreen_orientation, DESCRIPTOR_RULE, PATTERN_ORIENTATION_REASON)

    skips = (Node(mcv2_reference.SKIP),) * 3
    corner = Node(mcv2_reference.SPLIT, children=(Node(mcv2_reference.PATTERN, record=bytes(8)),) + skips)
    eight = _frame(Node(mcv2_reference.SPLIT, children=(corner,) + skips))
    _add(cases, 'pattern-orientation-8', _with_invalid_orientation(eight, 255), DESCRIPTOR_RULE, PATTERN_ORIENTATION_REASON)


def _with_invalid_orientation(data, orientation):
    offset = parse_frame(data).payload_start + mcv2_reference.PATTERN_ORIENTATION_INDEX
    return changed(data, offset, orientation)
