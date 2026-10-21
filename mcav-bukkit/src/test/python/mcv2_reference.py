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
from collections.abc import Mapping
from dataclasses import dataclass

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


def _require(condition: bool, message: str) -> None:
    if not condition:
        raise ValueError(message)


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


def parse_frame(data: bytes) -> Frame:
    """Accept exactly spec section 9 syntax; check each field extent before reading it, or raise ValueError."""
    if data[: len(VERSION_1_MAGIC)] == VERSION_1_MAGIC:
        message: str = "MCV1 version 1 is no longer supported; re-encode the video!"
        raise ValueError(message)
    if data[: len(VERSION_2_PREFIX)] == VERSION_2_PREFIX:
        message: str = "MCV2 version 2 is no longer supported; re-encode the video!"
        raise ValueError(message)
    total = len(data)
    _require(MINIMUM_FRAME_BYTES <= total <= MAXIMUM_FRAME_BYTES, "The frame length is invalid!")
    magic, version, width, height, frame_id, reference_id = HEADER.unpack_from(data)
    _require(magic == MAGIC and version == VERSION, "The data is not an MCV2 version 3 frame!")
    _require(
        MINIMUM_DIMENSION <= width <= MAXIMUM_DIMENSION and MINIMUM_DIMENSION <= height <= MAXIMUM_DIMENSION,
        INVALID_DIMENSIONS_MESSAGE,
    )
    keyframe = frame_id == reference_id
    columns = (width + SUPERBLOCK_SIZE - 1) // SUPERBLOCK_SIZE
    root_count = columns * ((height + SUPERBLOCK_SIZE - 1) // SUPERBLOCK_SIZE)
    groups = (root_count + MASK_BITS - 1) // MASK_BITS
    checkpoints = (groups + DIRECTORY_GROUPS - 1) // DIRECTORY_GROUPS
    counts_offset = HEADER_BYTES + WORD_BYTES * groups + WORD_BYTES * checkpoints
    _require(counts_offset + LEVEL_COUNTS.size <= total, "The frame has a truncated index!")
    masks = struct.unpack_from(WORDS_FORMAT.format(groups), data, HEADER_BYTES)
    directory = struct.unpack_from(WORDS_FORMAT.format(checkpoints), data, HEADER_BYTES + WORD_BYTES * groups)
    _require(masks[-1] >> (((root_count - 1) % MASK_BITS) + 1) == 0, "A presence mask has out-of-picture superblocks!")
    present = 0
    for index, mask in enumerate(masks):
        if index % DIRECTORY_GROUPS == 0:
            _require(directory[index // DIRECTORY_GROUPS] == present, "A directory prefix does not match the presence masks!")
        present += mask.bit_count()
    level_zero_count, level_one_count, level_two_count = levels = LEVEL_COUNTS.unpack_from(data, counts_offset)
    _require(level_zero_count == present, "The level 0 count differs from the presence masks!")
    count = level_zero_count + level_one_count + level_two_count
    walk_count = (count + WALK_STRIDE - 1) // WALK_STRIDE
    descriptor_offset = counts_offset + LEVEL_COUNTS.size
    payload = descriptor_offset + count + WORD_BYTES * walk_count
    _require(payload <= total, "The frame has truncated descriptors or walk!")
    descriptors = data[descriptor_offset : descriptor_offset + count]
    walk = struct.unpack_from(WORDS_FORMAT.format(walk_count), data, descriptor_offset + count)
    offsets, records = ([], [])
    cursor = splits = level_zero_splits = level_one_splits = 0
    for index, descriptor in enumerate(descriptors):
        if index % WALK_STRIDE == 0:
            _require(walk[index // WALK_STRIDE] == (cursor | (splits << WALK_CURSOR_BITS)), "The frame has a walk checkpoint mismatch!")
        size = (
            SUPERBLOCK_SIZE
            if index < level_zero_count
            else LEVEL_ONE_SIZE if index < level_zero_count + level_one_count else SMALLEST_LEAF_SIZE
        )
        mode, quantizer = (descriptor & MODE_MASK, descriptor >> QUANTIZER_SHIFT)
        _require(mode <= SPLIT, "The frame has an invalid descriptor mode!")
        _require(mode == COMPACT or quantizer == 0, NONZERO_QUANTIZER_MESSAGE)
        _require(quantizer <= MAXIMUM_QUANTIZER, "The frame has a COMPACT quantizer above 2!")
        _require(not keyframe or mode not in (MOTION, COMPACT), TEMPORAL_KEYFRAME_MESSAGE)
        _require(mode != SPLIT or size != SMALLEST_LEAF_SIZE, "The frame has a SPLIT in level 2!")
        offset = payload + cursor
        length = _record_length(mode, size)
        _require(offset + length <= total, "A record exceeds the frame!")
        record = data[offset : offset + length]
        if mode == PATTERN:
            _require(record[PATTERN_ORIENTATION_INDEX] <= HORIZONTAL_STRIPES, "A record has an invalid PATTERN orientation!")
        offsets.append(offset)
        records.append(record)
        cursor += length
        if mode == SPLIT:
            splits += 1
            level_zero_splits += index < level_zero_count
            level_one_splits += level_zero_count <= index < level_zero_count + level_one_count
    _require(level_one_count == SPLIT_CHILDREN * level_zero_splits, "The level 1 count differs from the SPLIT children!")
    _require(level_two_count == SPLIT_CHILDREN * level_one_splits, "The level 2 count differs from the SPLIT children!")
    _require(payload + cursor == total, "The records do not end at the end of the frame!")
    root_indexes = [index for index in range(root_count) if (masks[index // MASK_BITS] >> (index % MASK_BITS)) & 1]
    coordinates = [(SUPERBLOCK_SIZE * (index % columns), SUPERBLOCK_SIZE * (index // columns), SUPERBLOCK_SIZE) for index in root_indexes]
    leaves, children = ([], {})
    for index, descriptor in enumerate(descriptors):
        pixel_x, pixel_y, size = coordinates[index]
        mode, quantizer = (descriptor & MODE_MASK, descriptor >> QUANTIZER_SHIFT)
        if mode == SPLIT:
            half = size // QUADRANTS_PER_SIDE
            children[index] = len(coordinates)
            coordinates.extend(
                (pixel_x + delta_x * half, pixel_y + delta_y * half, half)
                for delta_x, delta_y in QUADRANT_OFFSETS
            )
        else:
            leaves.append(
                Leaf(pixel_x, pixel_y, size, mode, quantizer, offsets[index], records[index], index)
            )
    for index in range(root_count):
        if not ((masks[index // MASK_BITS] >> (index % MASK_BITS)) & 1):
            leaves.append(
                Leaf(SUPERBLOCK_SIZE * (index % columns), SUPERBLOCK_SIZE * (index // columns), SUPERBLOCK_SIZE, SKIP, 0, None, b"", None)
            )
    nodes = {}
    for index in reversed(range(count)):
        descriptor = descriptors[index]
        mode, quantizer = (descriptor & MODE_MASK, descriptor >> QUANTIZER_SHIFT)
        if mode == SPLIT:
            nodes[index] = Node(
                mode, children=tuple(nodes[children[index] + entry_index] for entry_index in range(SPLIT_CHILDREN))
            )
        else:
            nodes[index] = Node(mode, quantizer, records[index])
    roots = {root: nodes[index] for index, root in enumerate(root_indexes)}
    return Frame(
        width,
        height,
        frame_id,
        reference_id,
        keyframe,
        payload,
        total,
        tuple(leaves),
        masks,
        directory,
        levels,
        descriptors,
        walk,
        roots,
    )


def pack_frame(width: int, height: int, frame_id: int, reference_id: int, roots: Mapping[int, Node]) -> bytes:
    """Serialize a tree canonically. Absent roots stay absent; a frame is a keyframe when its ids are equal."""
    _require(
        MINIMUM_DIMENSION <= width <= MAXIMUM_DIMENSION and MINIMUM_DIMENSION <= height <= MAXIMUM_DIMENSION,
        INVALID_DIMENSIONS_MESSAGE,
    )
    _require(0 <= frame_id <= ID_MASK and 0 <= reference_id <= ID_MASK, "The frame and reference ids must be u32!")
    keyframe = frame_id == reference_id
    root_count = (width + SUPERBLOCK_SIZE - 1) // SUPERBLOCK_SIZE * ((height + SUPERBLOCK_SIZE - 1) // SUPERBLOCK_SIZE)
    _require(
        all(isinstance(index, int) and 0 <= index < root_count for index in roots), "A root index is out of range!"
    )
    masks = [0] * ((root_count + MASK_BITS - 1) // MASK_BITS)
    for index in roots:
        masks[index // MASK_BITS] |= 1 << (index % MASK_BITS)
    directory, present = ([], 0)
    for index, mask in enumerate(masks):
        if index % DIRECTORY_GROUPS == 0:
            directory.append(present)
        present += mask.bit_count()
    levels, descriptors, records, walk = ([], bytearray(), bytearray(), [])
    current = [roots[index] for index in sorted(roots)]
    splits = 0
    for size in LEAF_SIZES:
        levels.append(len(current))
        following = []
        for node in current:
            _require(
                0 <= node.mode <= SPLIT and 0 <= node.quantizer <= MAXIMUM_QUANTIZER, "A node has an invalid descriptor!"
            )
            _require(node.mode == COMPACT or node.quantizer == 0, NONZERO_QUANTIZER_MESSAGE)
            _require(not keyframe or node.mode not in (MOTION, COMPACT), TEMPORAL_KEYFRAME_MESSAGE)
            if len(descriptors) % WALK_STRIDE == 0:
                walk.append(len(records) | (splits << WALK_CURSOR_BITS))
            descriptors.append(node.mode | (node.quantizer << QUANTIZER_SHIFT))
            if node.mode == SPLIT:
                _require(
                    size > SMALLEST_LEAF_SIZE and len(node.children) == SPLIT_CHILDREN and (not node.record),
                    "A SPLIT node is invalid!",
                )
                following.extend(node.children)
                splits += 1
                continue
            _require(not node.children, "A leaf must not have children!")
            record = node.record
            if node.mode == PATTERN:
                _require(
                    len(record) == PATTERN_HEADER_BYTES + size // BITS_PER_BYTE
                    and record[PATTERN_ORIENTATION_INDEX] <= HORIZONTAL_STRIPES,
                    "A PATTERN record is invalid!",
                )
            length = _record_length(node.mode, size)
            _require(len(record) == length, "A leaf record has an invalid length!")
            records.extend(record)
        current = following
        _require(len(records) + len(descriptors) <= MAXIMUM_FRAME_BYTES, LENGTH_LIMIT_MESSAGE)
    index = (
        struct.pack(WORDS_FORMAT.format(len(masks)), *masks)
        + struct.pack(WORDS_FORMAT.format(len(directory)), *directory)
        + LEVEL_COUNTS.pack(*levels)
        + descriptors
        + struct.pack(WORDS_FORMAT.format(len(walk)), *walk)
    )
    total = HEADER_BYTES + len(index) + len(records)
    _require(total <= MAXIMUM_FRAME_BYTES, LENGTH_LIMIT_MESSAGE)
    return HEADER.pack(MAGIC, VERSION, width, height, frame_id, reference_id) + index + records


def _signed(value: int, bits: int = BITS_PER_BYTE) -> int:
    return value - (1 << bits) if value & (1 << (bits - 1)) else value


def _prediction(
    reference: numpy.ndarray, pixel_x: int, pixel_y: int, size: int, delta_x: int, delta_y: int
) -> numpy.ndarray:
    height, width, _ = reference.shape
    columns = numpy.clip(numpy.arange(size) + pixel_x + delta_x, 0, width - 1)
    rows = numpy.clip(numpy.arange(size) + pixel_y + delta_y, 0, height - 1)
    return reference[rows[:, None], columns]


def _grid(body: bytes, size: int) -> numpy.ndarray:
    packed = numpy.frombuffer(body[:GRID_BYTES], numpy.uint8)
    nibbles = numpy.stack((packed & NIBBLE_MASK, packed >> NIBBLE_BITS), axis=1).astype(numpy.int32).ravel()
    nodes = numpy.where(nibbles >= NIBBLE_SIGN, nibbles - NIBBLE_RANGE, nibbles).reshape(GRID_SIDE, GRID_SIDE)
    position = numpy.clip((numpy.arange(size) + HALF_PIXEL) * GRID_SIDE / size - HALF_PIXEL, 0, GRID_SIDE - 1)
    lower = position.astype(numpy.int32)
    upper = numpy.minimum(lower + 1, GRID_SIDE - 1)
    fraction = position - lower
    horizontal = nodes[:, lower] * (1 - fraction) + nodes[:, upper] * fraction
    return horizontal[lower] * (1 - fraction[:, None]) + horizontal[upper] * fraction[:, None]


def _decode(frame: Frame, reference: numpy.ndarray | None, reference_id: int | None) -> numpy.ndarray:
    if not frame.keyframe:
        if reference is None or reference_id != frame.reference_id:
            message: str = "The reference id is missing or does not match!"
            raise ValueError(message)
        if reference.shape != (frame.height, frame.width, CHANNELS) or reference.dtype != numpy.uint8:
            message: str = "The reference must be a matching height x width x 3 uint8 picture!"
            raise ValueError(message)
    picture = numpy.empty((frame.height, frame.width, CHANNELS), numpy.uint8)
    for leaf in frame.leaves:
        pixel_x, pixel_y, size, mode, record = (leaf.pixel_x, leaf.pixel_y, leaf.size, leaf.mode, leaf.record)
        if mode == SKIP:
            block = (
                numpy.zeros((size, size, CHANNELS), numpy.uint8)
                if frame.keyframe
                else _prediction(reference, pixel_x, pixel_y, size, 0, 0)
            )
        elif mode == SOLID:
            block = numpy.broadcast_to(numpy.frombuffer(record, numpy.uint8), (size, size, CHANNELS))
        elif mode in (PALETTE, PATTERN):
            if mode == PATTERN:
                axis = numpy.unpackbits(numpy.frombuffer(record[PATTERN_HEADER_BYTES:], numpy.uint8), bitorder=BIT_ORDER)
                selectors = numpy.broadcast_to(
                    axis[None, :] if record[PATTERN_ORIENTATION_INDEX] == VERTICAL_STRIPES else axis[:, None], (size, size)
                )
            else:
                selectors = numpy.unpackbits(
                    numpy.frombuffer(record[COLORS_BYTES:], numpy.uint8), bitorder=BIT_ORDER
                ).reshape(size, size)
            colors = numpy.frombuffer(record[:COLORS_BYTES], numpy.uint8).reshape(RECORD_COLORS, CHANNELS)
            block = colors[selectors]
        elif mode == MOTION:
            block = _prediction(reference, pixel_x, pixel_y, size, _signed(record[0]), _signed(record[1]))
        else:
            prediction = _prediction(
                reference, pixel_x, pixel_y, size, _signed(record[0]), _signed(record[1])
            ).astype(numpy.float64)
            residual = _grid(record[MOTION_BYTES:], size)[..., None]
            block = numpy.clip(
                numpy.floor(prediction + (1 << leaf.quantizer) * residual + ROUNDING_OFFSET), 0, MAXIMUM_CHANNEL
            ).astype(numpy.uint8)
        shown_width, shown_height = (min(size, frame.width - pixel_x), min(size, frame.height - pixel_y))
        if shown_width > 0 and shown_height > 0:
            picture[pixel_y : pixel_y + shown_height, pixel_x : pixel_x + shown_width] = block[
                :shown_height, :shown_width
            ]
    return picture


def decode(
    data: bytes, reference: numpy.ndarray | None = None, reference_id: int | None = None
) -> numpy.ndarray:
    """Validate and decode into a new height x width x 3 uint8 RGB picture.

    A P frame requires its matching reference shape, uint8 dtype and id. Invalid syntax or reference raises ValueError.
    """
    return _decode(parse_frame(data), reference, reference_id)


class Decoder:
    """Keep the last committed picture on every error; accept only newer u32 ids."""

    def __init__(self):
        self.reference: numpy.ndarray | None = None
        self.frame_id: int | None = None

    def accept(self, data: bytes) -> numpy.ndarray:
        frame = parse_frame(data)
        if self.frame_id is not None and (
            not 0 < ((frame.frame_id - self.frame_id) & ID_MASK) < ID_HALF_RANGE
        ):
            message: str = "The frame id is not newer under the half-range rule!"
            raise ValueError(message)
        picture = _decode(frame, self.reference, self.frame_id)
        # The caller may mutate the returned picture without changing the next frame's committed reference.
        self.reference = picture.copy()
        self.frame_id = frame.frame_id
        return picture



def page_capacity(symbol_bits: int = SYMBOL_BITS) -> int:
    if symbol_bits != SYMBOL_BITS:
        message: str = "Only 6-bit transport symbols are supported!"
        raise ValueError(message)
    return PAGE_CAPACITY


def to_symbols(data: bytes, symbol_bits: int = SYMBOL_BITS) -> bytes:
    """Return exact, unpadded map symbol bytes (map colours are these values plus four)."""
    page_capacity(symbol_bits)
    bits = numpy.unpackbits(numpy.frombuffer(data, numpy.uint8), bitorder=BIT_ORDER)
    bits = numpy.pad(bits, (0, -len(bits) % SYMBOL_BITS)).reshape(-1, SYMBOL_BITS)
    return (bits @ (1 << numpy.arange(SYMBOL_BITS))).astype(numpy.uint8).tobytes()


def from_symbols(symbols: bytes, symbol_bits: int, byte_count: int) -> bytes:
    page_capacity(symbol_bits)
    if byte_count < 0 or len(symbols) != (byte_count * BITS_PER_BYTE + SYMBOL_BITS - 1) // SYMBOL_BITS:
        message: str = "The symbol extent is invalid!"
        raise ValueError(message)
    values = numpy.frombuffer(symbols, numpy.uint8)
    if numpy.any(values >= ALPHABET_SIZE):
        message: str = "A symbol is out of the alphabet!"
        raise ValueError(message)
    bits = (values[:, None] >> numpy.arange(SYMBOL_BITS) & 1).astype(numpy.uint8).ravel()
    if numpy.any(bits[byte_count * BITS_PER_BYTE :]):
        message: str = "The symbol padding is not zero!"
        raise ValueError(message)
    return numpy.packbits(bits[: byte_count * BITS_PER_BYTE], bitorder=BIT_ORDER).tobytes()


def make_pages(frame_data: bytes, stream_id: int = DEFAULT_STREAM_ID, symbol_bits: int = SYMBOL_BITS) -> list[bytes]:
    capacity = page_capacity(symbol_bits)
    if not 0 <= stream_id <= ID_MASK:
        message: str = "The stream id must be u32!"
        raise ValueError(message)
    frame = parse_frame(frame_data)
    count = (frame.total + capacity - 1) // capacity
    pages = []
    for number in range(count):
        payload = frame_data[number * capacity : (number + 1) * capacity]
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
        pages.append(to_symbols(header[:PAGE_CRC_OFFSET] + PAGE_CRC.pack(crc) + payload))
    return pages


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


def read_page(symbols: bytes, symbol_bits: int = SYMBOL_BITS) -> Page:
    page_capacity(symbol_bits)
    if not PAGE_HEADER_SYMBOLS <= len(symbols) <= PAGE_SYMBOLS:
        message: str = "The map page extent is invalid!"
        raise ValueError(message)
    values = numpy.frombuffer(symbols[:PAGE_HEADER_SYMBOLS], numpy.uint8)
    # The 43rd symbol also holds the first two payload bits; it is not header padding.
    bits = (values[:, None] >> numpy.arange(SYMBOL_BITS) & 1).astype(numpy.uint8).ravel()
    header = numpy.packbits(bits[: PAGE_HEADER_BYTES * BITS_PER_BYTE], bitorder=BIT_ORDER).tobytes()
    magic, version, width, flags, stream, frame, number, count, reference, total, crc = PAGE_HEADER.unpack(
        header
    )
    if magic != PAGE_MAGIC or version != PAGE_VERSION or width != SYMBOL_BITS or (flags > MAXIMUM_PAGE_FLAGS):
        message: str = "The page header is not supported!"
        raise ValueError(message)
    if (
        not MINIMUM_FRAME_BYTES <= total <= MAXIMUM_FRAME_BYTES
        or count != (total + PAGE_CAPACITY - 1) // PAGE_CAPACITY
        or number >= count
    ):
        message: str = "The page metadata is invalid!"
        raise ValueError(message)
    size = min(PAGE_CAPACITY, total - number * PAGE_CAPACITY)
    raw = from_symbols(symbols, SYMBOL_BITS, PAGE_HEADER.size + size)
    if zlib.crc32(raw[:PAGE_CRC_OFFSET] + bytes(PAGE_CRC.size) + raw[PAGE_HEADER_BYTES:]) != crc:
        message: str = "The page CRC does not match!"
        raise ValueError(message)
    return Page(stream, frame, number, count, reference, total, flags, width, raw[PAGE_HEADER_BYTES:])


def wire_bytes(pages: list[bytes], full_maps: bool = False, packet_overhead: int = PACKET_OVERHEAD) -> int:
    """Charge map colour bytes rounded to 128-colour rows (or full maps), plus each page's packet allowance."""
    return sum(
        (PAGE_SYMBOLS if full_maps else (len(page) + MAP_SIDE - 1) // MAP_SIDE * MAP_SIDE) + packet_overhead for page in pages
    )


class Assembler:
    """Keep at most four pending frames; identical duplicates are safe, a conflict discards that pending frame."""

    def __init__(self, stream_id: int = DEFAULT_STREAM_ID, symbol_bits: int = SYMBOL_BITS):
        page_capacity(symbol_bits)
        self.stream_id = stream_id
        self.symbol_bits = symbol_bits
        self.pending: dict[int, dict[int, Page]] = {}

    def push(self, symbols: bytes) -> bytes | None:
        page = read_page(symbols, self.symbol_bits)
        if page.stream_id != self.stream_id:
            message: str = "The page belongs to the wrong stream!"
            raise ValueError(message)
        if page.frame_id not in self.pending and len(self.pending) >= MAXIMUM_PENDING:
            del self.pending[next(iter(self.pending))]
        parts = self.pending.setdefault(page.frame_id, {})
        if parts:
            first = next(iter(parts.values()))
            if (page.count, page.reference_id, page.frame_bytes, page.flags) != (
                first.count,
                first.reference_id,
                first.frame_bytes,
                first.flags,
            ):
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
        data = b"".join(parts[number].payload for number in range(page.count))
        del self.pending[page.frame_id]
        frame = parse_frame(data)
        if (frame.frame_id, frame.reference_id, frame.flags) != (
            page.frame_id,
            page.reference_id,
            page.flags,
        ):
            message: str = "The pages have a frame identity mismatch!"
            raise ValueError(message)
        return data
