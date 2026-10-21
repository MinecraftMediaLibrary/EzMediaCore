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

"""Independent MCV2 v3 format, serializer, decoder and six-bit transport."""

import struct
import zlib
from collections.abc import Mapping, Sequence
from dataclasses import dataclass
from enum import Enum, auto

import numpy

MAGIC = b"MCV2"
VERSION = 3
VERSION_1_MAGIC = b"MCV1"
VERSION_2_PREFIX = b"MCV2\x02"

HEADER = struct.Struct("<4sIHHII")
HEADER_BYTES = 20
MAGIC_OFFSET = 0
VERSION_OFFSET = 4
WIDTH_OFFSET = 8
HEIGHT_OFFSET = 10
FRAME_ID_OFFSET = 12
REFERENCE_ID_OFFSET = 16

WORD_BYTES = 4
WORDS_FORMAT = "<{}I"
LEVEL_COUNTS = struct.Struct("<III")

BITS_PER_BYTE = 8
BIT_ORDER = "little"

KEYFRAME = 1

MINIMUM_DIMENSION = 1
MAXIMUM_DIMENSION = 4096

MINIMUM_FRAME_BYTES = 20
MAXIMUM_FRAME_BYTES = 131071

ID_MASK = 0xFFFFFFFF
ID_HALF_RANGE = 0x80000000

SUPERBLOCK_SIZE = 32
LEVEL_ONE_SIZE = 16
SMALLEST_LEAF_SIZE = 8
LEAF_SIZES = (SUPERBLOCK_SIZE, LEVEL_ONE_SIZE, SMALLEST_LEAF_SIZE)

SPLIT_CHILDREN = 4
QUADRANTS_PER_SIDE = 2
QUADRANT_OFFSETS = ((0, 0), (1, 0), (0, 1), (1, 1))

MASK_BITS = 32
DIRECTORY_GROUPS = 8
WALK_STRIDE = 8
WALK_CURSOR_BITS = 17

SKIP, MOTION, SOLID, PALETTE, PATTERN, COMPACT, SPLIT = range(7)
MODE_MASK = 31
QUANTIZER_SHIFT = 5
MAXIMUM_QUANTIZER = 2

CHANNELS = 3
MAXIMUM_CHANNEL = 255
ROUNDING_OFFSET = 0.5

MOTION_BYTES = 2
SOLID_BYTES = CHANNELS
RECORD_COLORS = 2
COLORS_BYTES = RECORD_COLORS * CHANNELS
PATTERN_ORIENTATION_INDEX = COLORS_BYTES
PATTERN_HEADER_BYTES = COLORS_BYTES + 1
VERTICAL_STRIPES = 0
HORIZONTAL_STRIPES = 1
COMPACT_BYTES = 10

GRID_SIDE = 4
NIBBLE_BITS = 4
NIBBLE_MASK = (1 << NIBBLE_BITS) - 1
NIBBLE_SIGN = 1 << (NIBBLE_BITS - 1)
NIBBLE_RANGE = 1 << NIBBLE_BITS
GRID_BYTES = GRID_SIDE * GRID_SIDE * NIBBLE_BITS // BITS_PER_BYTE
HALF_PIXEL = 0.5

PAGE_MAGIC = b"MCP1"
PAGE_VERSION = 1
PAGE_HEADER = struct.Struct("<4sBBHIIHHIII")
PAGE_HEADER_BYTES = 32
PAGE_CRC = struct.Struct("<I")
PAGE_CRC_OFFSET = 28
MAXIMUM_PAGE_FLAGS = KEYFRAME
DEFAULT_STREAM_ID = 1

SYMBOL_BITS = 6
ALPHABET_SIZE = 1 << SYMBOL_BITS
MAP_SIDE = 128
PAGE_SYMBOLS = MAP_SIDE * MAP_SIDE
PAGE_HEADER_SYMBOLS = (PAGE_HEADER_BYTES * BITS_PER_BYTE + SYMBOL_BITS - 1) // SYMBOL_BITS
PAGE_CAPACITY = PAGE_SYMBOLS * SYMBOL_BITS // BITS_PER_BYTE - PAGE_HEADER_BYTES
PACKET_OVERHEAD = 18

MAXIMUM_PENDING = 4
MAXIMUM_PAGE_SLOTS = 8
SCREEN_CAPACITY = MAXIMUM_PAGE_SLOTS * PAGE_CAPACITY

INVALID_DIMENSIONS_MESSAGE: str = "The picture dimensions are invalid!"
NONZERO_QUANTIZER_MESSAGE: str = "A non-COMPACT quantizer must be zero!"
TEMPORAL_KEYFRAME_MESSAGE: str = "A keyframe must not use a temporal mode!"
LENGTH_LIMIT_MESSAGE: str = "The frame exceeds the length limit!"




@dataclass(frozen=True)
class Node:
    """A leaf with its record, or a SPLIT with four children in quadrant order."""

    mode: int
    quantizer: int = 0
    record: bytes = b""
    children: tuple["Node", ...] = ()


@dataclass(frozen=True)
class Leaf:
    pixel_x: int
    pixel_y: int
    size: int
    mode: int
    quantizer: int
    offset: int | None
    record: bytes
    descriptor_index: int | None


@dataclass(frozen=True)
class Frame:
    width: int
    height: int
    frame_id: int
    reference_id: int
    keyframe: bool
    payload_start: int
    total: int
    leaves: tuple[Leaf, ...]
    masks: tuple[int, ...]
    directory: tuple[int, ...]
    level_counts: tuple[int, int, int]
    descriptors: bytes
    walk: tuple[int, ...]
    roots: dict[int, Node]

    @property
    def flags(self) -> int:
        return int(self.keyframe)


@dataclass(frozen=True)
class Page:
    stream_id: int
    frame_id: int
    number: int
    count: int
    reference_id: int
    frame_bytes: int
    flags: int
    symbol_bits: int
    payload: bytes


class MapCharge(Enum):
    """How wire_bytes charges the map colours of a page: rounded up to whole 128-colour rows, or as a full map."""

    ROWS = auto()
    FULL_MAPS = auto()


@dataclass(frozen=True)
class _FrameIndex:
    masks: tuple[int, ...]
    directory: tuple[int, ...]
    levels: tuple[int, int, int]
    descriptors: bytes
    walk: tuple[int, ...]
    payload: int


def parse_frame(data: bytes) -> Frame:
    """Accept exactly spec section 9 syntax; check each field extent before reading it, or raise ValueError."""
    width, height, frame_id, reference_id = _read_header(data)
    keyframe = frame_id == reference_id
    root_count = _root_count(width, height)
    frame_index = _read_index(data, root_count)
    offsets, records = _read_records(data, frame_index, keyframe)

    columns = _divide_rounding_up(width, SUPERBLOCK_SIZE)
    root_indexes = [root for root in range(root_count) if _is_present(frame_index.masks, root)]
    absent_roots = [root for root in range(root_count) if not _is_present(frame_index.masks, root)]
    coordinates, first_children = _place_descriptors(frame_index.descriptors, root_indexes, columns)
    present_leaves = _present_leaves(frame_index.descriptors, coordinates, offsets, records)
    absent_leaves = [_absent_leaf(root, columns) for root in absent_roots]
    roots = _build_roots(frame_index.descriptors, records, first_children, root_indexes)
    return Frame(
        width,
        height,
        frame_id,
        reference_id,
        keyframe,
        frame_index.payload,
        len(data),
        tuple(present_leaves + absent_leaves),
        frame_index.masks,
        frame_index.directory,
        frame_index.levels,
        frame_index.descriptors,
        frame_index.walk,
        roots,
    )


def _read_header(data: bytes) -> tuple[int, int, int, int]:
    _reject_retired_versions(data)
    _require(MINIMUM_FRAME_BYTES <= len(data) <= MAXIMUM_FRAME_BYTES, "The frame length is invalid!")

    magic, version, width, height, frame_id, reference_id = HEADER.unpack_from(data)
    _require(magic == MAGIC and version == VERSION, "The data is not an MCV2 version 3 frame!")
    _require(_is_dimension_valid(width) and _is_dimension_valid(height), INVALID_DIMENSIONS_MESSAGE)
    return width, height, frame_id, reference_id


def _reject_retired_versions(data: bytes) -> None:
    if data[: len(VERSION_1_MAGIC)] == VERSION_1_MAGIC:
        message: str = "MCV1 version 1 is no longer supported; re-encode the video!"
        raise ValueError(message)

    if data[: len(VERSION_2_PREFIX)] == VERSION_2_PREFIX:
        message: str = "MCV2 version 2 is no longer supported; re-encode the video!"
        raise ValueError(message)


def _require(condition: bool, message: str) -> None:
    if not condition:
        raise ValueError(message)


def _is_dimension_valid(length: int) -> bool:
    return MINIMUM_DIMENSION <= length <= MAXIMUM_DIMENSION


def _root_count(width: int, height: int) -> int:
    columns = _divide_rounding_up(width, SUPERBLOCK_SIZE)
    rows = _divide_rounding_up(height, SUPERBLOCK_SIZE)
    return columns * rows


def _divide_rounding_up(dividend: int, divisor: int) -> int:
    return (dividend + divisor - 1) // divisor


def _read_index(data: bytes, root_count: int) -> _FrameIndex:
    groups = _divide_rounding_up(root_count, MASK_BITS)
    checkpoints = _divide_rounding_up(groups, DIRECTORY_GROUPS)
    directory_offset = HEADER_BYTES + WORD_BYTES * groups
    counts_offset = directory_offset + WORD_BYTES * checkpoints
    _require(counts_offset + LEVEL_COUNTS.size <= len(data), "The frame has a truncated index!")

    masks = _read_words(data, HEADER_BYTES, groups)
    directory = _read_words(data, directory_offset, checkpoints)
    _check_presence(masks, directory, root_count)

    levels = LEVEL_COUNTS.unpack_from(data, counts_offset)
    level_zero_count = levels[0]
    present = sum(mask.bit_count() for mask in masks)
    _require(level_zero_count == present, "The level 0 count differs from the presence masks!")

    count = sum(levels)
    walk_count = _divide_rounding_up(count, WALK_STRIDE)
    descriptor_offset = counts_offset + LEVEL_COUNTS.size
    walk_offset = descriptor_offset + count
    payload = walk_offset + WORD_BYTES * walk_count
    _require(payload <= len(data), "The frame has truncated descriptors or walk!")

    descriptors = data[descriptor_offset:walk_offset]
    walk = _read_words(data, walk_offset, walk_count)
    return _FrameIndex(masks, directory, levels, descriptors, walk, payload)


def _read_words(data: bytes, offset: int, count: int) -> tuple[int, ...]:
    words_format = WORDS_FORMAT.format(count)
    return struct.unpack_from(words_format, data, offset)


def _check_presence(masks: tuple[int, ...], directory: tuple[int, ...], root_count: int) -> None:
    last_mask_bits = (root_count - 1) % MASK_BITS + 1
    _require(masks[-1] >> last_mask_bits == 0, "A presence mask has out-of-picture superblocks!")

    expected_directory = _build_directory(masks)
    _require(list(directory) == expected_directory, "A directory prefix does not match the presence masks!")


def _build_directory(masks: Sequence[int]) -> list[int]:
    directory = []
    present = 0
    for group, mask in enumerate(masks):
        if group % DIRECTORY_GROUPS == 0:
            directory.append(present)
        present += mask.bit_count()

    return directory


def _read_records(data: bytes, frame_index: _FrameIndex, keyframe: bool) -> tuple[list[int], list[bytes]]:
    offsets = []
    records = []
    cursor = 0
    splits = 0
    for position, descriptor in enumerate(frame_index.descriptors):
        if position % WALK_STRIDE == 0:
            checkpoint = frame_index.walk[position // WALK_STRIDE]
            _require(checkpoint == _walk_checkpoint(cursor, splits), "The frame has a walk checkpoint mismatch!")
        size = _block_size(position, frame_index.levels)
        mode, quantizer = _unpack_descriptor(descriptor)
        _check_descriptor(mode, quantizer, size, keyframe)
        offset = frame_index.payload + cursor
        record = _read_record(data, offset, mode, size)
        offsets.append(offset)
        records.append(record)
        cursor += len(record)
        if mode == SPLIT:
            splits += 1

    _check_split_children(frame_index.descriptors, frame_index.levels)
    _require(frame_index.payload + cursor == len(data), "The records do not end at the end of the frame!")
    return offsets, records


def _walk_checkpoint(cursor: int, splits: int) -> int:
    return cursor | (splits << WALK_CURSOR_BITS)


def _block_size(position: int, levels: tuple[int, int, int]) -> int:
    level_zero_count, level_one_count, _ = levels
    if position < level_zero_count:
        return SUPERBLOCK_SIZE

    if position < level_zero_count + level_one_count:
        return LEVEL_ONE_SIZE

    return SMALLEST_LEAF_SIZE


def _unpack_descriptor(descriptor: int) -> tuple[int, int]:
    return descriptor & MODE_MASK, descriptor >> QUANTIZER_SHIFT


def _check_descriptor(mode: int, quantizer: int, size: int, keyframe: bool) -> None:
    _require(mode <= SPLIT, "The frame has an invalid descriptor mode!")
    _require(mode == COMPACT or quantizer == 0, NONZERO_QUANTIZER_MESSAGE)
    _require(quantizer <= MAXIMUM_QUANTIZER, "The frame has a COMPACT quantizer above 2!")
    _require(not keyframe or mode not in (MOTION, COMPACT), TEMPORAL_KEYFRAME_MESSAGE)
    _require(mode != SPLIT or size != SMALLEST_LEAF_SIZE, "The frame has a SPLIT in level 2!")


def _read_record(data: bytes, offset: int, mode: int, size: int) -> bytes:
    length = _record_length(mode, size)
    _require(offset + length <= len(data), "A record exceeds the frame!")

    record = data[offset : offset + length]
    if mode == PATTERN:
        _require(record[PATTERN_ORIENTATION_INDEX] <= HORIZONTAL_STRIPES, "A record has an invalid PATTERN orientation!")

    return record


def _record_length(mode: int, size: int) -> int:
    if mode in (SKIP, SPLIT):
        return 0

    if mode == MOTION:
        return MOTION_BYTES

    if mode == SOLID:
        return SOLID_BYTES

    if mode == PALETTE:
        return COLORS_BYTES + size * size // BITS_PER_BYTE

    if mode == PATTERN:
        return PATTERN_HEADER_BYTES + size // BITS_PER_BYTE

    return COMPACT_BYTES


def _check_split_children(descriptors: bytes, levels: tuple[int, int, int]) -> None:
    level_zero_count, level_one_count, level_two_count = levels
    level_two_start = level_zero_count + level_one_count
    level_zero_splits = _count_splits(descriptors[:level_zero_count])
    level_one_splits = _count_splits(descriptors[level_zero_count:level_two_start])
    _require(level_one_count == SPLIT_CHILDREN * level_zero_splits, "The level 1 count differs from the SPLIT children!")
    _require(level_two_count == SPLIT_CHILDREN * level_one_splits, "The level 2 count differs from the SPLIT children!")


def _count_splits(descriptors: bytes) -> int:
    modes = [descriptor & MODE_MASK for descriptor in descriptors]
    return modes.count(SPLIT)


def _is_present(masks: Sequence[int], root: int) -> bool:
    mask = masks[root // MASK_BITS]
    return (mask >> (root % MASK_BITS)) & 1 == 1


def _place_descriptors(
    descriptors: bytes, root_indexes: list[int], columns: int
) -> tuple[list[tuple[int, int, int]], dict[int, int]]:
    coordinates = [_root_coordinates(root, columns) for root in root_indexes]
    first_children = {}
    for position, descriptor in enumerate(descriptors):
        mode, _ = _unpack_descriptor(descriptor)
        if mode == SPLIT:
            first_children[position] = len(coordinates)
            coordinates.extend(_quadrant_coordinates(coordinates[position]))

    return coordinates, first_children


def _root_coordinates(root: int, columns: int) -> tuple[int, int, int]:
    pixel_x = SUPERBLOCK_SIZE * (root % columns)
    pixel_y = SUPERBLOCK_SIZE * (root // columns)
    return pixel_x, pixel_y, SUPERBLOCK_SIZE


def _quadrant_coordinates(coordinate: tuple[int, int, int]) -> list[tuple[int, int, int]]:
    pixel_x, pixel_y, size = coordinate
    half = size // QUADRANTS_PER_SIDE
    return [(pixel_x + delta_x * half, pixel_y + delta_y * half, half) for delta_x, delta_y in QUADRANT_OFFSETS]


def _present_leaves(
    descriptors: bytes, coordinates: list[tuple[int, int, int]], offsets: list[int], records: list[bytes]
) -> list[Leaf]:
    leaves = []
    for position, descriptor in enumerate(descriptors):
        mode, quantizer = _unpack_descriptor(descriptor)
        if mode != SPLIT:
            pixel_x, pixel_y, size = coordinates[position]
            leaves.append(Leaf(pixel_x, pixel_y, size, mode, quantizer, offsets[position], records[position], position))

    return leaves


def _absent_leaf(root: int, columns: int) -> Leaf:
    pixel_x, pixel_y, size = _root_coordinates(root, columns)
    return Leaf(pixel_x, pixel_y, size, SKIP, 0, None, b"", None)


def _build_roots(
    descriptors: bytes, records: list[bytes], first_children: dict[int, int], root_indexes: list[int]
) -> dict[int, Node]:
    nodes = {}
    for position in reversed(range(len(descriptors))):
        mode, quantizer = _unpack_descriptor(descriptors[position])
        if mode == SPLIT:
            first_child = first_children[position]
            nodes[position] = Node(mode, children=tuple(nodes[first_child + quadrant] for quadrant in range(SPLIT_CHILDREN)))
            continue
        nodes[position] = Node(mode, quantizer, records[position])

    return {root: nodes[position] for position, root in enumerate(root_indexes)}


def pack_frame(width: int, height: int, frame_id: int, reference_id: int, roots: Mapping[int, Node]) -> bytes:
    """Serialize a tree canonically. Absent roots stay absent; a frame is a keyframe when its ids are equal."""
    _require(_is_dimension_valid(width) and _is_dimension_valid(height), INVALID_DIMENSIONS_MESSAGE)
    _require(_is_id_valid(frame_id) and _is_id_valid(reference_id), "The frame and reference ids must be u32!")

    keyframe = frame_id == reference_id
    root_count = _root_count(width, height)
    _require(all(_is_root_valid(root, root_count) for root in roots), "A root index is out of range!")

    masks = _presence_masks(roots, root_count)
    directory = _build_directory(masks)
    writer = _TreeWriter(keyframe)
    writer.write_tree(roots)
    index = (
        _pack_words(masks)
        + _pack_words(directory)
        + LEVEL_COUNTS.pack(*writer.levels)
        + writer.descriptors
        + _pack_words(writer.walk)
    )
    total = HEADER_BYTES + len(index) + len(writer.records)
    _require(total <= MAXIMUM_FRAME_BYTES, LENGTH_LIMIT_MESSAGE)

    header = HEADER.pack(MAGIC, VERSION, width, height, frame_id, reference_id)
    return header + index + writer.records


def _is_id_valid(identifier: int) -> bool:
    return 0 <= identifier <= ID_MASK


def _is_root_valid(root: int, root_count: int) -> bool:
    return isinstance(root, int) and 0 <= root < root_count


def _presence_masks(roots: Mapping[int, Node], root_count: int) -> list[int]:
    masks = [0] * _divide_rounding_up(root_count, MASK_BITS)
    for root in roots:
        masks[root // MASK_BITS] |= 1 << (root % MASK_BITS)

    return masks


class _TreeWriter:
    def __init__(self, keyframe: bool):
        self.keyframe = keyframe
        self.levels = []
        self.descriptors = bytearray()
        self.records = bytearray()
        self.walk = []
        self.splits = 0

    def write_tree(self, roots: Mapping[int, Node]) -> None:
        current = [roots[root] for root in sorted(roots)]
        for size in LEAF_SIZES:
            self.levels.append(len(current))
            current = self.write_level(current, size)
            _require(len(self.records) + len(self.descriptors) <= MAXIMUM_FRAME_BYTES, LENGTH_LIMIT_MESSAGE)

    def write_level(self, nodes: list[Node], size: int) -> list[Node]:
        following = []
        for node in nodes:
            self.write_descriptor(node)
            if node.mode == SPLIT:
                _require(_is_split_valid(node, size), "A SPLIT node is invalid!")
                following.extend(node.children)
                self.splits += 1
                continue
            self.write_record(node, size)

        return following

    def write_descriptor(self, node: Node) -> None:
        _require(_is_descriptor_valid(node), "A node has an invalid descriptor!")
        _require(node.mode == COMPACT or node.quantizer == 0, NONZERO_QUANTIZER_MESSAGE)
        _require(not self.keyframe or node.mode not in (MOTION, COMPACT), TEMPORAL_KEYFRAME_MESSAGE)
        if len(self.descriptors) % WALK_STRIDE == 0:
            self.walk.append(_walk_checkpoint(len(self.records), self.splits))

        self.descriptors.append(node.mode | (node.quantizer << QUANTIZER_SHIFT))

    def write_record(self, node: Node, size: int) -> None:
        _require(not node.children, "A leaf must not have children!")
        if node.mode == PATTERN:
            _require(_is_pattern_valid(node.record, size), "A PATTERN record is invalid!")

        length = _record_length(node.mode, size)
        _require(len(node.record) == length, "A leaf record has an invalid length!")
        self.records.extend(node.record)


def _is_split_valid(node: Node, size: int) -> bool:
    return size > SMALLEST_LEAF_SIZE and len(node.children) == SPLIT_CHILDREN and not node.record


def _is_descriptor_valid(node: Node) -> bool:
    return 0 <= node.mode <= SPLIT and 0 <= node.quantizer <= MAXIMUM_QUANTIZER


def _is_pattern_valid(record: bytes, size: int) -> bool:
    return len(record) == PATTERN_HEADER_BYTES + size // BITS_PER_BYTE and record[PATTERN_ORIENTATION_INDEX] <= HORIZONTAL_STRIPES


def _pack_words(words: Sequence[int]) -> bytes:
    words_format = WORDS_FORMAT.format(len(words))
    return struct.pack(words_format, *words)


def decode(
    data: bytes, reference: numpy.ndarray | None = None, reference_id: int | None = None
) -> numpy.ndarray:
    """Validate and decode into a new height x width x 3 uint8 RGB picture.

    A P frame requires its matching reference shape, uint8 dtype and id. Invalid syntax or reference raises ValueError.
    """
    frame = parse_frame(data)
    return _decode(frame, reference, reference_id)


def _decode(frame: Frame, reference: numpy.ndarray | None, reference_id: int | None) -> numpy.ndarray:
    if not frame.keyframe:
        _check_reference(frame, reference, reference_id)

    picture = numpy.empty((frame.height, frame.width, CHANNELS), numpy.uint8)
    for leaf in frame.leaves:
        block = _leaf_block(frame, leaf, reference)
        _draw_block(picture, leaf, block)

    return picture


def _check_reference(frame: Frame, reference: numpy.ndarray | None, reference_id: int | None) -> None:
    if reference is None or reference_id != frame.reference_id:
        message: str = "The reference id is missing or does not match!"
        raise ValueError(message)

    if reference.shape != (frame.height, frame.width, CHANNELS) or reference.dtype != numpy.uint8:
        message: str = "The reference must be a matching height x width x 3 uint8 picture!"
        raise ValueError(message)


def _leaf_block(frame: Frame, leaf: Leaf, reference: numpy.ndarray | None) -> numpy.ndarray:
    if leaf.mode == SKIP:
        return _skipped_block(frame, leaf, reference)

    if leaf.mode == SOLID:
        return _solid_block(leaf)

    if leaf.mode == PALETTE:
        return _palette_block(leaf)

    if leaf.mode == PATTERN:
        return _pattern_block(leaf)

    if leaf.mode == MOTION:
        return _motion_block(reference, leaf)

    return _compact_block(reference, leaf)


def _skipped_block(frame: Frame, leaf: Leaf, reference: numpy.ndarray | None) -> numpy.ndarray:
    if frame.keyframe:
        return numpy.zeros((leaf.size, leaf.size, CHANNELS), numpy.uint8)

    return _prediction(reference, leaf, 0, 0)


def _prediction(reference: numpy.ndarray, leaf: Leaf, delta_x: int, delta_y: int) -> numpy.ndarray:
    height, width, _ = reference.shape
    columns = numpy.clip(numpy.arange(leaf.size) + leaf.pixel_x + delta_x, 0, width - 1)
    rows = numpy.clip(numpy.arange(leaf.size) + leaf.pixel_y + delta_y, 0, height - 1)
    return reference[rows[:, None], columns]


def _solid_block(leaf: Leaf) -> numpy.ndarray:
    color = numpy.frombuffer(leaf.record, numpy.uint8)
    return numpy.broadcast_to(color, (leaf.size, leaf.size, CHANNELS))


def _palette_block(leaf: Leaf) -> numpy.ndarray:
    packed = numpy.frombuffer(leaf.record[COLORS_BYTES:], numpy.uint8)
    selectors = numpy.unpackbits(packed, bitorder=BIT_ORDER).reshape(leaf.size, leaf.size)
    return _two_color_block(leaf.record, selectors)


def _two_color_block(record: bytes, selectors: numpy.ndarray) -> numpy.ndarray:
    colors = numpy.frombuffer(record[:COLORS_BYTES], numpy.uint8).reshape(RECORD_COLORS, CHANNELS)
    return colors[selectors]


def _pattern_block(leaf: Leaf) -> numpy.ndarray:
    packed = numpy.frombuffer(leaf.record[PATTERN_HEADER_BYTES:], numpy.uint8)
    axis = numpy.unpackbits(packed, bitorder=BIT_ORDER)
    vertical = leaf.record[PATTERN_ORIENTATION_INDEX] == VERTICAL_STRIPES
    stripes = axis[None, :] if vertical else axis[:, None]
    selectors = numpy.broadcast_to(stripes, (leaf.size, leaf.size))
    return _two_color_block(leaf.record, selectors)


def _motion_block(reference: numpy.ndarray, leaf: Leaf) -> numpy.ndarray:
    delta_x, delta_y = _motion(leaf.record)
    return _prediction(reference, leaf, delta_x, delta_y)


def _motion(record: bytes) -> tuple[int, int]:
    return _signed(record[0]), _signed(record[1])


def _signed(value: int, bits: int = BITS_PER_BYTE) -> int:
    return value - (1 << bits) if value & (1 << (bits - 1)) else value


def _compact_block(reference: numpy.ndarray, leaf: Leaf) -> numpy.ndarray:
    delta_x, delta_y = _motion(leaf.record)
    prediction = _prediction(reference, leaf, delta_x, delta_y).astype(numpy.float64)
    residual = _grid(leaf.record[MOTION_BYTES:], leaf.size)[..., None]
    corrected = numpy.floor(prediction + (1 << leaf.quantizer) * residual + ROUNDING_OFFSET)
    return numpy.clip(corrected, 0, MAXIMUM_CHANNEL).astype(numpy.uint8)


def _grid(body: bytes, size: int) -> numpy.ndarray:
    nodes = _grid_nodes(body)
    position = numpy.clip((numpy.arange(size) + HALF_PIXEL) * GRID_SIDE / size - HALF_PIXEL, 0, GRID_SIDE - 1)
    lower = position.astype(numpy.int32)
    upper = numpy.minimum(lower + 1, GRID_SIDE - 1)
    fraction = position - lower
    horizontal = nodes[:, lower] * (1 - fraction) + nodes[:, upper] * fraction
    return horizontal[lower] * (1 - fraction[:, None]) + horizontal[upper] * fraction[:, None]


def _grid_nodes(body: bytes) -> numpy.ndarray:
    packed = numpy.frombuffer(body[:GRID_BYTES], numpy.uint8)
    nibbles = numpy.stack((packed & NIBBLE_MASK, packed >> NIBBLE_BITS), axis=1).astype(numpy.int32).ravel()
    return numpy.where(nibbles >= NIBBLE_SIGN, nibbles - NIBBLE_RANGE, nibbles).reshape(GRID_SIDE, GRID_SIDE)


def _draw_block(picture: numpy.ndarray, leaf: Leaf, block: numpy.ndarray) -> None:
    height, width, _ = picture.shape
    shown_width = min(leaf.size, width - leaf.pixel_x)
    shown_height = min(leaf.size, height - leaf.pixel_y)
    if shown_width <= 0 or shown_height <= 0:
        return

    rows = slice(leaf.pixel_y, leaf.pixel_y + shown_height)
    columns = slice(leaf.pixel_x, leaf.pixel_x + shown_width)
    picture[rows, columns] = block[:shown_height, :shown_width]


class Decoder:
    """Keep the last committed picture on every error; accept only newer u32 ids."""

    def __init__(self):
        self.reference: numpy.ndarray | None = None
        self.frame_id: int | None = None

    def accept(self, data: bytes) -> numpy.ndarray:
        frame = parse_frame(data)
        if self.frame_id is not None and not _is_newer(frame.frame_id, self.frame_id):
            message: str = "The frame id is not newer under the half-range rule!"
            raise ValueError(message)

        picture = _decode(frame, self.reference, self.frame_id)
        # The caller may mutate the returned picture without changing the next frame's committed reference.
        self.reference = picture.copy()
        self.frame_id = frame.frame_id

        return picture


def _is_newer(frame_id: int, previous_id: int) -> bool:
    distance = (frame_id - previous_id) & ID_MASK
    return 0 < distance < ID_HALF_RANGE


def page_capacity(symbol_bits: int = SYMBOL_BITS) -> int:
    _check_symbol_bits(symbol_bits)
    return PAGE_CAPACITY


def _check_symbol_bits(symbol_bits: int) -> None:
    if symbol_bits != SYMBOL_BITS:
        message: str = "Only 6-bit transport symbols are supported!"
        raise ValueError(message)


def make_pages(frame_data: bytes, stream_id: int = DEFAULT_STREAM_ID, symbol_bits: int = SYMBOL_BITS) -> list[bytes]:
    _check_symbol_bits(symbol_bits)
    if not _is_id_valid(stream_id):
        message: str = "The stream id must be u32!"
        raise ValueError(message)

    frame = parse_frame(frame_data)
    count = _page_count(frame.total)
    return [_make_page(frame_data, frame, stream_id, number) for number in range(count)]


def _page_count(frame_bytes: int) -> int:
    return _divide_rounding_up(frame_bytes, PAGE_CAPACITY)


def _make_page(frame_data: bytes, frame: Frame, stream_id: int, number: int) -> bytes:
    count = _page_count(frame.total)
    start = number * PAGE_CAPACITY
    payload = frame_data[start : start + PAGE_CAPACITY]
    header = PAGE_HEADER.pack(
        PAGE_MAGIC,
        PAGE_VERSION,
        SYMBOL_BITS,
        frame.flags,
        stream_id,
        frame.frame_id,
        number,
        count,
        frame.reference_id,
        frame.total,
        0,
    )
    crc = zlib.crc32(header + payload)
    page = header[:PAGE_CRC_OFFSET] + PAGE_CRC.pack(crc) + payload
    return to_symbols(page)


def to_symbols(data: bytes, symbol_bits: int = SYMBOL_BITS) -> bytes:
    """Return exact, unpadded map symbol bytes (map colours are these values plus four)."""
    _check_symbol_bits(symbol_bits)
    packed = numpy.frombuffer(data, numpy.uint8)
    bits = numpy.unpackbits(packed, bitorder=BIT_ORDER)
    padding = -len(bits) % SYMBOL_BITS
    symbol_rows = numpy.pad(bits, (0, padding)).reshape(-1, SYMBOL_BITS)
    weights = 1 << numpy.arange(SYMBOL_BITS)
    return (symbol_rows @ weights).astype(numpy.uint8).tobytes()


def read_page(symbols: bytes, symbol_bits: int = SYMBOL_BITS) -> Page:
    _check_symbol_bits(symbol_bits)
    if not PAGE_HEADER_SYMBOLS <= len(symbols) <= PAGE_SYMBOLS:
        message: str = "The map page extent is invalid!"
        raise ValueError(message)

    header = _read_page_header(symbols)
    magic, version, width, flags, stream, frame, number, count, reference, total, crc = PAGE_HEADER.unpack(header)
    if magic != PAGE_MAGIC or version != PAGE_VERSION or width != SYMBOL_BITS or flags > MAXIMUM_PAGE_FLAGS:
        message: str = "The page header is not supported!"
        raise ValueError(message)

    if not MINIMUM_FRAME_BYTES <= total <= MAXIMUM_FRAME_BYTES or count != _page_count(total) or number >= count:
        message: str = "The page metadata is invalid!"
        raise ValueError(message)

    size = min(PAGE_CAPACITY, total - number * PAGE_CAPACITY)
    raw = from_symbols(symbols, SYMBOL_BITS, PAGE_HEADER.size + size)
    if _page_crc(raw) != crc:
        message: str = "The page CRC does not match!"
        raise ValueError(message)

    return Page(stream, frame, number, count, reference, total, flags, width, raw[PAGE_HEADER_BYTES:])


def _read_page_header(symbols: bytes) -> bytes:
    values = numpy.frombuffer(symbols[:PAGE_HEADER_SYMBOLS], numpy.uint8)
    # The 43rd symbol also holds the first two payload bits; it is not header padding.
    bits = _unpack_symbols(values)
    return numpy.packbits(bits[: PAGE_HEADER_BYTES * BITS_PER_BYTE], bitorder=BIT_ORDER).tobytes()


def _unpack_symbols(values: numpy.ndarray) -> numpy.ndarray:
    return (values[:, None] >> numpy.arange(SYMBOL_BITS) & 1).astype(numpy.uint8).ravel()


def from_symbols(symbols: bytes, symbol_bits: int, byte_count: int) -> bytes:
    _check_symbol_bits(symbol_bits)
    bit_count = byte_count * BITS_PER_BYTE
    if byte_count < 0 or len(symbols) != _divide_rounding_up(bit_count, SYMBOL_BITS):
        message: str = "The symbol extent is invalid!"
        raise ValueError(message)

    values = numpy.frombuffer(symbols, numpy.uint8)
    if numpy.any(values >= ALPHABET_SIZE):
        message: str = "A symbol is out of the alphabet!"
        raise ValueError(message)

    bits = _unpack_symbols(values)
    if numpy.any(bits[bit_count:]):
        message: str = "The symbol padding is not zero!"
        raise ValueError(message)

    return numpy.packbits(bits[:bit_count], bitorder=BIT_ORDER).tobytes()


def _page_crc(raw: bytes) -> int:
    zeroed = raw[:PAGE_CRC_OFFSET] + bytes(PAGE_CRC.size) + raw[PAGE_HEADER_BYTES:]
    return zlib.crc32(zeroed)


def wire_bytes(pages: list[bytes], charge: MapCharge = MapCharge.ROWS, packet_overhead: int = PACKET_OVERHEAD) -> int:
    """Charge each page's map colour bytes as charge says, plus each page's packet allowance."""
    return sum(_map_bytes(page, charge) + packet_overhead for page in pages)


def _map_bytes(page: bytes, charge: MapCharge) -> int:
    if charge is MapCharge.FULL_MAPS:
        return PAGE_SYMBOLS

    rows = _divide_rounding_up(len(page), MAP_SIDE)
    return rows * MAP_SIDE


class Assembler:
    """Keep at most four pending frames; identical duplicates are safe, a conflict discards that pending frame."""

    def __init__(self, stream_id: int = DEFAULT_STREAM_ID, symbol_bits: int = SYMBOL_BITS):
        _check_symbol_bits(symbol_bits)
        self.stream_id = stream_id
        self.symbol_bits = symbol_bits
        self.pending: dict[int, dict[int, Page]] = {}

    def push(self, symbols: bytes) -> bytes | None:
        page = read_page(symbols, self.symbol_bits)
        if page.stream_id != self.stream_id:
            message: str = "The page belongs to the wrong stream!"
            raise ValueError(message)

        if page.frame_id not in self.pending and len(self.pending) >= MAXIMUM_PENDING:
            oldest = next(iter(self.pending))
            del self.pending[oldest]

        parts = self.pending.setdefault(page.frame_id, {})
        if _is_inconsistent(parts, page):
            del self.pending[page.frame_id]
            message: str = "The pages are inconsistent!"
            raise ValueError(message)

        if page.number in parts and parts[page.number] != page:
            del self.pending[page.frame_id]
            message: str = "A duplicate page conflicts with the first copy!"
            raise ValueError(message)

        parts[page.number] = page
        if len(parts) != page.count:
            return None

        return self._assemble(parts, page)

    def _assemble(self, parts: dict[int, Page], page: Page) -> bytes:
        data = b"".join(parts[number].payload for number in range(page.count))
        del self.pending[page.frame_id]

        frame = parse_frame(data)
        if (frame.frame_id, frame.reference_id, frame.flags) != (page.frame_id, page.reference_id, page.flags):
            message: str = "The pages have a frame identity mismatch!"
            raise ValueError(message)

        return data


def _is_inconsistent(parts: dict[int, Page], page: Page) -> bool:
    if not parts:
        return False

    first = next(iter(parts.values()))
    page_identity = (page.count, page.reference_id, page.frame_bytes, page.flags)
    first_identity = (first.count, first.reference_id, first.frame_bytes, first.flags)
    return page_identity != first_identity
