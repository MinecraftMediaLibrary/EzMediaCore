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


def changed(data, offset, value, code=BYTE_CODE):
    result = bytearray(data)
    struct.pack_into(BYTE_ORDER + code, result, offset, value)
    return bytes(result)


def rejected_frames():
    cases = {}

    def add(name, data, rule, reason):
        cases[name] = {FRAME_KEY: data.hex(), 'rule': rule, 'reason': reason}

    def frame(node=None, key=True):
        return pack_frame(1, 1, 0 if key else 1, 0, {} if node is None else {0: node})

    empty = frame()
    solid = frame(Node(mcv2_reference.SOLID, record=b'\x10\x20\x30'))
    add('short-frame', bytes(mcv2_reference.MINIMUM_FRAME_BYTES - 1), HEADER_RULE, FRAME_LENGTH_REASON)
    add('oversize-frame', bytes(mcv2_reference.MAXIMUM_FRAME_BYTES + 1), HEADER_RULE, FRAME_LENGTH_REASON)
    add('bad-magic', b'ABCD' + empty[len(mcv2_reference.MAGIC):], HEADER_RULE, NOT_MCV2_REASON)
    add('unknown-version', changed(empty, mcv2_reference.VERSION_OFFSET, mcv2_reference.VERSION + 1), HEADER_RULE, NOT_MCV2_REASON)
    for offset in range(mcv2_reference.VERSION_OFFSET + 1, mcv2_reference.WIDTH_OFFSET):
        add(f'version-byte-{offset}', changed(empty, offset, 1), HEADER_RULE, NOT_MCV2_REASON)
    add('mcv1', bytes.fromhex(
        '4d4356310102010001000100090000000900000000000000010000003400000037000000'
        '00000000000000000000000034000002102030'), HEADER_RULE, 'version 1 is no longer supported')
    add('version-2', bytes.fromhex(
        '4d4356320205730001000100000000000000000000000000010000004500000075000000000000000000000000000000010000000000000001000000'
        '0000010600000000003224e3e9bafb4931c49e41fca6fb71c25d4744121177650106704ecf2c3e8fa04eaa462d163cd0d18bc59cbc0fbaead8'),
        HEADER_RULE, 'version 2 is no longer supported')
    for offset in (mcv2_reference.WIDTH_OFFSET, mcv2_reference.HEIGHT_OFFSET):
        for value in (0, mcv2_reference.MAXIMUM_DIMENSION + 1):
            add(f'dimension-{offset}-{value}', changed(empty, offset, value, HALF_WORD_CODE), HEADER_RULE, 'dimensions')
    for length in (20, 24, 28, 32, 36, 39):
        add(f'truncated-index-{length}', empty[:length], INDEX_RULE, 'truncated index')
    add('unused-mask-bit', changed(empty, MASK_OFFSET, 2, WORD_CODE), INDEX_RULE, 'presence mask')
    add('directory-zero', changed(empty, DIRECTORY_OFFSET, 1, WORD_CODE), INDEX_RULE, DIRECTORY_PREFIX_REASON)
    wide = pack_frame(4096, 65, 0, 0, {0: Node(mcv2_reference.SKIP), 256: Node(mcv2_reference.SKIP)})
    later_prefix = MASK_OFFSET + 12 * mcv2_reference.WORD_BYTES + mcv2_reference.WORD_BYTES
    add('directory-later-prefix', changed(wide, later_prefix, 0, WORD_CODE), INDEX_RULE, DIRECTORY_PREFIX_REASON)
    add('n0-popcount', changed(solid, COUNTS_OFFSET, 0, WORD_CODE), INDEX_RULE, 'level 0 count')
    level_one_count = COUNTS_OFFSET + mcv2_reference.WORD_BYTES
    add('count-overflow', changed(empty, level_one_count, 0xFFFFFFFF, WORD_CODE), INDEX_RULE, TRUNCATED_DESCRIPTORS_REASON)
    split = Node(mcv2_reference.SPLIT, children=(Node(mcv2_reference.SKIP),) * mcv2_reference.SPLIT_CHILDREN)
    branch = frame(split)
    add('n1-children', changed(branch, DESCRIPTORS_OFFSET, mcv2_reference.SKIP), INDEX_RULE, 'level 1 count')
    deep = frame(Node(mcv2_reference.SPLIT, children=(split,) * mcv2_reference.SPLIT_CHILDREN))
    add('n2-children', changed(deep, DESCRIPTORS_OFFSET + 1, mcv2_reference.SKIP), INDEX_RULE, 'level 2 count')
    # Removing that split also changes the later walk counts; repair those so this isolates n2.
    data = bytearray(bytes.fromhex(cases['n2-children'][FRAME_KEY]))
    for offset in (TWO_LEVEL_WALK_OFFSET + mcv2_reference.WORD_BYTES, TWO_LEVEL_WALK_OFFSET + 2 * mcv2_reference.WORD_BYTES):
        value = WORD.unpack_from(data, offset)[0]
        WORD.pack_into(data, offset, value - WALK_SPLIT)
    cases['n2-children'][FRAME_KEY] = data.hex()
    add('split-at-level-2', changed(deep, DESCRIPTORS_OFFSET + 5, mcv2_reference.SPLIT), INDEX_RULE, 'SPLIT in level 2')
    add('truncated-descriptor-walk', solid[:44], INDEX_RULE, TRUNCATED_DESCRIPTORS_REASON)
    add('walk-zero', changed(solid, DESCRIPTORS_OFFSET + 1, 1, WORD_CODE), WALK_RULE, WALK_CHECKPOINT_REASON)
    long = frame(Node(
        mcv2_reference.SPLIT,
        children=(Node(
            mcv2_reference.SPLIT,
            children=(Node(mcv2_reference.SOLID, record=bytes(mcv2_reference.SOLID_BYTES)),) * mcv2_reference.SPLIT_CHILDREN,
        ),) * mcv2_reference.SPLIT_CHILDREN,
    ))
    offset = TWO_LEVEL_WALK_OFFSET + mcv2_reference.WORD_BYTES
    checkpoint = WORD.unpack_from(long, offset)[0]
    add('walk-cursor', changed(long, offset, checkpoint + 1, WORD_CODE), WALK_RULE, WALK_CHECKPOINT_REASON)
    add('walk-splits', changed(long, offset, checkpoint + WALK_SPLIT, WORD_CODE), WALK_RULE, WALK_CHECKPOINT_REASON)
    for mode in range(mcv2_reference.SPLIT + 1, mcv2_reference.MODE_MASK + 1):
        add(f'invalid-mode-{mode}', changed(solid, DESCRIPTORS_OFFSET, mode), DESCRIPTOR_RULE, 'descriptor mode')
    for mode in (
        mcv2_reference.SKIP,
        mcv2_reference.MOTION,
        mcv2_reference.SOLID,
        mcv2_reference.PALETTE,
        mcv2_reference.PATTERN,
        mcv2_reference.SPLIT,
    ):
        add(f'quantizer-mode-{mode}', changed(solid, DESCRIPTORS_OFFSET, mode | QUANTIZER_STEP), DESCRIPTOR_RULE, 'quantizer')
    compact = frame(Node(mcv2_reference.COMPACT, record=bytes(mcv2_reference.COMPACT_BYTES)), key=False)
    for quantizer in range(mcv2_reference.MAXIMUM_QUANTIZER + 1, QUANTIZER_VALUES):
        add(
            f'compact-quantizer-{quantizer}',
            changed(compact, DESCRIPTORS_OFFSET, mcv2_reference.COMPACT | quantizer << mcv2_reference.QUANTIZER_SHIFT),
            DESCRIPTOR_RULE, 'COMPACT quantizer above 2',
        )
    for mode in (mcv2_reference.MOTION, mcv2_reference.COMPACT):
        add(f'keyframe-mode-{mode}', changed(solid, DESCRIPTORS_OFFSET, mode), DESCRIPTOR_RULE, 'temporal mode')
    nodes = [Node(mcv2_reference.MOTION, record=bytes(mcv2_reference.MOTION_BYTES)),
             Node(mcv2_reference.SOLID, record=bytes(mcv2_reference.SOLID_BYTES)),
             Node(mcv2_reference.PALETTE, record=bytes(134)), Node(mcv2_reference.PATTERN, record=bytes(11)),
             Node(mcv2_reference.COMPACT, record=bytes(mcv2_reference.COMPACT_BYTES))]
    for node in nodes:
        data = frame(node, key=False)
        add(f'truncated-record-{node.mode}', data[:-1], DESCRIPTOR_RULE, 'record exceeds')
    pattern = frame(Node(mcv2_reference.PATTERN, record=bytes(11)))
    pattern_orientation = parse_frame(pattern).payload_start + mcv2_reference.PATTERN_ORIENTATION_INDEX
    add('pattern-orientation', changed(pattern, pattern_orientation, INVALID_ORIENTATION), DESCRIPTOR_RULE, PATTERN_ORIENTATION_REASON)
    offscreen = frame(Node(
        mcv2_reference.SPLIT,
        children=(
            Node(mcv2_reference.SKIP),
            Node(mcv2_reference.SKIP),
            Node(mcv2_reference.SKIP),
            Node(mcv2_reference.PATTERN, record=bytes(9)),
        ),
    ))
    offscreen_orientation = parse_frame(offscreen).payload_start + mcv2_reference.PATTERN_ORIENTATION_INDEX
    add(
        'off-picture-orientation',
        changed(offscreen, offscreen_orientation, INVALID_ORIENTATION),
        DESCRIPTOR_RULE, PATTERN_ORIENTATION_REASON,
    )
    eight = frame(Node(mcv2_reference.SPLIT, children=(Node(mcv2_reference.SPLIT, children=(Node(mcv2_reference.PATTERN, record=bytes(8)),)
                                                     + (Node(mcv2_reference.SKIP),) * 3),) + (Node(mcv2_reference.SKIP),) * 3))
    eight_orientation = parse_frame(eight).payload_start + mcv2_reference.PATTERN_ORIENTATION_INDEX
    add('pattern-orientation-8', changed(eight, eight_orientation, 255), DESCRIPTOR_RULE, PATTERN_ORIENTATION_REASON)
    add('payload-gap', solid + b'\0', END_RULE, 'records do not end')
    return cases
