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

"""MCV2 measurements, fixture generation, shader checks and documentation figures."""

import argparse
import hashlib
import json
import logging
import math
import os
import random
import re
import struct
import subprocess
import sys
import tempfile
import time
from collections import Counter
from datetime import datetime
from pathlib import Path

import mcv2_reference
import numpy
from mcv2_reference import (
    BITS_PER_BYTE,
    BIT_ORDER,
    CHANNELS,
    COLORS_BYTES,
    FRAME_ID_OFFSET,
    GRID_BYTES,
    HEADER_BYTES,
    ID_HALF_RANGE,
    ID_MASK,
    LEAF_SIZES,
    LEVEL_COUNTS,
    MAGIC,
    MAXIMUM_CHANNEL,
    MAXIMUM_DIMENSION,
    MINIMUM_DIMENSION,
    MODE_MASK,
    MOTION_BYTES,
    PAGE_CAPACITY,
    PAGE_HEADER,
    PAGE_SYMBOLS,
    PATTERN_HEADER_BYTES,
    PATTERN_ORIENTATION_INDEX,
    QUADRANTS_PER_SIDE,
    QUANTIZER_SHIFT,
    REFERENCE_ID_OFFSET,
    SMALLEST_LEAF_SIZE,
    SOLID,
    SOLID_BYTES,
    SPLIT_CHILDREN,
    SUPERBLOCK_SIZE,
    SYMBOL_BITS,
    VERSION,
    VERSION_OFFSET,
    VERTICAL_STRIPES,
    WALK_CURSOR_BITS,
    WIDTH_OFFSET,
    WORD_BYTES,
    Decoder,
    MapCharge,
    Node,
    decode,
    make_pages,
    pack_frame,
    page_capacity,
    parse_frame,
    read_page,
    wire_bytes,
)

SUCCESS_EXIT_CODE = 0
FAILURE_EXIT_CODE = 1
USAGE_EXIT_CODE = 2

TOOLS_DIRECTORY_DEPTH = 4

REPOSITORY_ROOT = Path(__file__).resolve().parents[TOOLS_DIRECTORY_DEPTH]

LINE_SEPARATOR = "\n"
WORD_SEPARATOR = " "
LIST_SEPARATOR = ","

JSON_INDENT = 1
REPORT_INDENT = 2

PERCENT = 100

QUALITY_DECIMALS = 4
MEASUREMENT_DECIMALS = 6
SECONDS_DECIMALS = 1

BITS_PER_MEGABIT = 1000000.0

DECIBELS_PER_BEL = 10
IDENTICAL_PSNR = 99.0

BYTE_VALUES = 256

STDERR_TAIL_CHARACTERS = 2000

BD_RATE_FIT_DEGREE = 3
BD_RATE_MINIMUM_POINTS = 4
BD_RATE_DECIMALS = 3

FFMPEG_COMMAND = "ffmpeg"
FFPROBE_COMMAND = "ffprobe"
JAVA_COMMAND = "java"
JFR_COMMAND = "jfr"

HIDE_BANNER_FLAG = "-hide_banner"
NO_STANDARD_INPUT_FLAG = "-nostdin"
VERBOSITY_FLAG = "-v"
LOG_LEVEL_FLAG = "-loglevel"
VERBOSE_LOG_LEVEL = "verbose"
ERROR_LOG_LEVEL = "error"
OVERWRITE_FLAG = "-y"

FORMAT_FLAG = "-f"
RAW_VIDEO_FORMAT = "rawvideo"
NULL_FORMAT = "null"
RAW_PIXEL_FORMAT_FLAG = "-pixel_format"
PIXEL_FORMAT_FLAG = "-pix_fmt"
RGB_PIXEL_FORMAT = "rgb24"
YUV_PIXEL_FORMAT = "yuv420p"
VIDEO_SIZE_FLAG = "-video_size"
FRAME_SIZE_FLAG = "-s"
FRAME_RATE_FLAG = "-framerate"
RATE_FLAG = "-r"
INPUT_FLAG = "-i"
STANDARD_STREAM = "-"

FRAME_COUNT_FLAG = "-frames:v"
NO_AUDIO_FLAG = "-an"
FRAME_RATE_MODE_FLAG = "-fps_mode"
PASSTHROUGH_FRAME_RATE_MODE = "passthrough"
FILTER_GRAPH_FLAG = "-lavfi"
VERSION_FLAG = "-version"

VIDEO_CODEC_FLAG = "-c:v"
X264_ENCODER = "libx264"
PRESET_FLAG = "-preset"
CONSTANT_RATE_FACTOR_FLAG = "-crf"
KEYFRAME_INTERVAL_FLAG = "-g"

SELECT_STREAMS_FLAG = "-select_streams"
FIRST_VIDEO_STREAM = "v:0"
SHOW_ENTRIES_FLAG = "-show_entries"
LATENCY_PROBE_ENTRIES = "stream=width,height:frame=pts_time"
OUTPUT_FORMAT_FLAG = "-of"
JSON_FORMAT = "json"
FFPROBE_STREAMS_KEY = "streams"

CLASSPATH_FLAG = "-cp"
NATIVE_ACCESS_FLAG = "--enable-native-access=ALL-UNNAMED"
JAVA_HEAP_FLAG = "-Xmx8g"
MCV2_TOOLS_CLASS = "me.brandonli.mcav.bukkit.media.mcv2.Mcv2Tools"
DIGESTS_COMMAND = "digests"
BENCH_COMMAND = "bench"
SHADER_COMPILE_COMMAND = "shader-compile"
POST_ONLY_FLAG = "--post-only"

JFR_PRINT_COMMAND = "print"
JFR_JSON_FLAG = "--json"
JFR_EVENTS_FLAG = "--events"
SEND_EVENT = "me.brandonli.mcav.Mcv2Send"
FRAME_EVENT = "me.brandonli.mcav.Mcv2Frame"

JFR_RECORDING_KEY = "recording"
JFR_EVENTS_KEY = "events"
JFR_VALUES_KEY = "values"

FRAME_ID_FIELD = "frameId"
KEYFRAME_FIELD = "keyframe"
BYTES_FIELD = "bytes"
COLORS_FIELD = "colors"
ARRIVED_FIELD = "arrived"
SENT_FIELD = "sent"
SENT_TO_FIELD = "sentTo"
BEHIND_FIELD = "behind"
WAITING_FIELD = "waiting"
BACKLOG_FIELD = "backlog"
FINGERPRINT_FIELD = "fingerprint"

FFMPEG_OPTION = "--ffmpeg"
JAVA_OPTION = "--java"
SOURCE_OPTION = "--source"
WIDTH_OPTION = "--width"
HEIGHT_OPTION = "--height"
FRAMES_OPTION = "--frames"
FRAMES_PER_SECOND_OPTION = "--fps"
OUT_OPTION = "--out"
SLOTS_OPTION = "--slots"
BACKEND_OPTION = "--backend"
PACK_OPTION = "--pack"
SPIRV_OPTION = "--spirv"
JSON_OPTION = "--json"
CROP_OPTION = "--crop"
FRAME_OPTION = "--frame"

SPIRV_METAVAR = "CLASSPATH"

ALL_FIXTURES = "all"
TREE_COMMAND = "tree"
LEAVES_COMMAND = "leaves"
BYTES_COMMAND = "bytes"

EGL_BACKEND = "egl"
GLX_BACKEND = "glx"

GL_RENDERER_KEY = "GL_RENDERER"
GL_VERSION_KEY = "GL_VERSION"

VMAF_FRAMES_KEY = "frames"
VMAF_METRICS_KEY = "metrics"
VMAF_SCORE_KEY = "vmaf"

SOURCES_KEY = "sources"
SOURCE_KEY = "source"
CODECS_KEY = "codecs"
LEGACY_CODECS_KEY = "points"
CODEC_KEY = "codec"
QUALITY_KEY = "quality"
CONTAINER_RATE_KEY = "container_mbps"
ZLIB_RATE_KEY = "zlib_mbps"
VMAF_MEAN_KEY = "vmaf_mean"
VMAF_MINIMUM_KEY = "vmaf_min"

BD_RATE_KEY = "bd_rate"
FEATURE_ID_KEY = "id"
FEATURE_NAME_KEY = "name"

CODEC_CURVES_QUALITY_PLACEHOLDER = "{q}"

X264_CODEC = "x264"
MCV2_CODEC = "mcv2"

PROXY_SOURCE = "proxy30"
GAMEPLAY_SOURCE = "gameplay30"

REJECT_TOKEN = "reject"

TOTAL_TIMING_NAME = "total"

CONFORMANCE_RESOURCES_PATH = "mcav-bukkit/src/test/resources/mcv2/conformance"
DOCUMENTATION_IMAGES_PATH = "mcav-docs/images/mcv2"
DIFFERENTIAL_OUTPUT_PATH = "build/mcv2-differential"
ASSETS_NAME = "assets"
SHADER_INCLUDE_PATH = "shaders/include"
SHADER_POST_PATH = "assets/mcav/shaders/post"
COMPILED_SHADERS_NAME = "glsl"
SPIRV_DIRECTORY_PREFIX = "mcv2-spirv-"

CONFORMANCE_DIRECTORY = "conformance"
EDGE_DIRECTORY = "edge"
ENCODER_DIRECTORY = "encoder"
ARCHIVES_NAME = "archives"
CONFORMANCE_PAGES_PATH = "conformance/pages.json"

STREAM_GLOB = "*.mcs"
EDGE_STREAM_GLOB = "edge-*.mcs"
SCREENSHOT_GLOB = "*.png"

PARTIAL_SUFFIX = ".partial"
RAW_SUFFIX = ".rgb"
FRAGMENT_SHADER_SUFFIX = ".fsh"
VERTEX_SHADER_SUFFIX = ".vsh"

REFERENCE_PICTURES_NAME = "ref.rgb"
DISTORTED_PICTURES_NAME = "dis.rgb"
DECODED_PICTURES_NAME = "decoded.rgb"
VMAF_LOG_NAME = "vmaf.json"

DIGESTS_NAME = "digests.json"
REJECTED_NAME = "rejected.json"
SUMMARY_NAME = "summary.json"
CHAIN_NAME = "chain.json"
CODEC_CURVES_DATA_NAME = "codec_curves.json"
ABLATION_DATA_NAME = "ablation.json"

CODECS_FIGURE_NAME = "codecs.png"
FEATURES_FIGURE_NAME = "features.png"
LEAVES_FIGURE_NAME = "leaves.png"
TREE_FIGURE_NAME = "tree.png"

RESOURCE_NAMESPACE_SEPARATOR = ":"
RESOURCE_PATH_SEPARATOR = "/"

SCREENQUAD_PROGRAM = "minecraft:core/screenquad"

TARGETS_KEY = "targets"
PASSES_KEY = "passes"
INPUTS_KEY = "inputs"
TARGET_KEY = "target"
OUTPUT_KEY = "output"

PREVIOUS_TARGET = "previous"
STATUS_TARGET = "status"

SAMPLER_SUFFIX = "Sampler"
BYTES_HEIGHT_PLACEHOLDER = "BYTES_HEIGHT"
TOTAL_SLOTS_DECLARATION = "const int MCV2_TOTAL_SLOTS = %d;"

DIMENSIONS_FORMAT = "<HH"
FRAME_IDS_FORMAT = "<II"
LENGTH_FORMAT = "<I"
LENGTH_BYTES = 4
FLOAT_FORMAT = "<f"

STRIP_MAGIC = b"MCV"

FONT_MANAGER_LOGGER = "matplotlib.font_manager"

MARKDOWN_CELL_SEPARATOR = " | "
MARKDOWN_ROW_END = " |"
MARKDOWN_HEADER_RULE = "|---|"
MARKDOWN_RIGHT_ALIGNED_RULE = "---:|"

COUNTER_VIDEO_PRESET = "veryfast"
COUNTER_VIDEO_CONSTANT_RATE_FACTOR = "12"
COUNTER_VIDEO_KEYFRAME_SECONDS = 2
COUNTER_VIDEO_THRESHOLD = 128

LUMA_RED_WEIGHT = 0.299
LUMA_GREEN_WEIGHT = 0.587
LUMA_BLUE_WEIGHT = 0.114

SSIM_BLOCK = 8
SSIM_LUMINANCE_FACTOR = 0.01
SSIM_CONTRAST_FACTOR = 0.03

CAPTURE_CHECK_DEFAULT_TOP = 13

EDGE_STREAMS_FRAMES = 6
EDGE_STREAMS_MAXIMUM_WIDTH = 200
EDGE_STREAMS_MAXIMUM_HEIGHT = 130

EDGE_STREAMS_SPLIT_CHANCE = 0.65
EDGE_STREAMS_KEYFRAME_CHANCE = 0.2
EDGE_STREAMS_ABSENT_CHANCE = 0.2

PATTERN_ORIENTATIONS = 2

MOTION_MINIMUM_X_MAXIMUM_Y = b"\x80\x7f"
MOTION_MAXIMUM_X_MINIMUM_Y = b"\x7f\x80"
MOTION_MINIMUM_X_MINIMUM_Y = b"\x80\x80"
MOTION_MAXIMUM_X_MAXIMUM_Y = b"\x7f\x7f"

MAXIMUM_NIBBLE_PAIR = 119
MINIMUM_NIBBLE_PAIR = 136

EDGE_STREAMS_MODES_WIDTH = 512

EDGE_STREAMS_PATTERN_WIDTH = 256
EDGE_STREAMS_PATTERN_HEIGHT = 96

EDGE_STREAMS_FULL_AXIS = 255
EDGE_STREAMS_MIXED_AXIS = 165

EDGE_STREAMS_SKIP_PERIOD = 3

EDGE_STREAMS_SMALL_WIDTH = 33
EDGE_STREAMS_SMALL_HEIGHT = 17

EDGE_STREAMS_MOTION_QUANTIZER = 2

EDGE_STREAMS_LONG_WALK_WIDTH = 1024
EDGE_STREAMS_LONG_WALK_HEIGHT = 448
EDGE_STREAMS_LONG_WALK_ROOTS = 440

EDGE_STREAMS_MAXIMUM_SPLITS_ROOTS = 4086

EDGE_STREAMS_LENGTH_LIMIT_SIDE = 1024
EDGE_STREAMS_LENGTH_LIMIT_PALETTES = 965
EDGE_STREAMS_LENGTH_LIMIT_ROOTS = 993

FIXTURES_PAGES_CASES = 4
FIXTURES_PAGES_STREAM_ID = 7

FIXTURES_PAGES_COMMITTED_SOURCE = "committed v3 conformance streams"

DIFFERENTIAL_MUTATION_CHANCE = 0.15
DIFFERENTIAL_MAXIMUM_FLIPS = 4
DIFFERENTIAL_HEAD_CHANCE = 0.7
DIFFERENTIAL_HEAD_BYTES = 256

DIFFERENTIAL_DEFAULT_STREAMS = 200
DIFFERENTIAL_DEFAULT_CONFORMANCE = 40
DIFFERENTIAL_DEFAULT_SEED = 20260926

DEFAULT_WIDTH = 1920
DEFAULT_HEIGHT = 1080

RATE_QUALITY_DEFAULT_FRAMES = 60
RATE_QUALITY_DEFAULT_FRAMES_PER_SECOND = 60

GL_VERSION = 330

DEFAULT_SLOTS = 4

RGBA_CHANNELS = 4
ALPHA_CHANNEL = 3

TRIANGLE_VERTICES = 3

SHADER_CHECK_CELL_SIZE = 8
SHADER_CHECK_BYTES_WIDTH = 128
SHADER_CHECK_PAGE_TEXELS = 4
SHADER_CHECK_CRC_TEXELS = 64

SHADER_CHECK_PAGE_NUMBER_FIRST_BIT = 128
SHADER_CHECK_PAGE_NUMBER_BITS = 16

VERSION_2_FLAGS_OFFSET = 6

PAGE_NUMBER_FIELD = 6
PAGE_TOTAL_FIELD = 9

SHADER_TIMING_TOP_LEFT = (-3.0, 1.5, -3.0)
SHADER_TIMING_RIGHT = (1.0, 0.0, 0.0)
SHADER_TIMING_DOWN = (0.0, -1.0, 0.0)
SHADER_TIMING_CELLS = (6.0, 3.0)

SHADER_TIMING_FIRST_FLOAT_PIXEL = 2
SHADER_TIMING_PIXELS_PER_FLOAT = 2

SHADER_TIMING_WARM_UP_FRAMES = 20
SHADER_TIMING_DEFAULT_ROUNDS = 3
SHADER_TIMING_MINIMUM_ROUNDS = 2
SHADER_TIMING_DEFAULT_REPEATS = 5

MILLISECONDS_PER_SECOND = 1000.0
NANOSECONDS_PER_MILLISECOND = 1000000.0

MEDIAN_PERCENTILE = 50
TAIL_PERCENTILE = 95

STRIP_CHECK_MARGIN = 8
STRIP_CHECK_EXTRA_SQUARES = 5

STRIP_CHECK_PRINTED_FAILURES = 20

STRIP_FIT_CHECK_SEED = 7
STRIP_FIT_CHECK_TOTAL_CHECKS = 7

STRIP_FIT_CHECK_UNFIT_SCREENS = ((160, 90), (64, 400))
STRIP_FIT_CHECK_FIT_SCREEN = (854, 480)

SHADER_CHECK_TARGET_PATTERN = "mcav:mcv2_%s_%d"

CAPTURE_CHECK_FRAME_RATE = 30

LATENCY_LUMA_GREEN_WEIGHT = 2
LATENCY_LUMA_WEIGHT_TOTAL = 4

LATENCY_PRINTED_DIGITS = 3

CHARTS_FONT_SIZE = 10.5
CHARTS_TITLE_FONT_SIZE = 11.5
CHARTS_LEGEND_FONT_SIZE = 10
CHARTS_SMALL_FONT_SIZE = 9.5
CHARTS_BAR_LABEL_FONT_SIZE = 8.5

CHARTS_DOTS_PER_INCH = 120

CHARTS_CURVE_LINE_WIDTH = 1.6
CHARTS_AXIS_LINE_WIDTH = 0.8
CHARTS_GRID_LINE_WIDTH = 0.6
CHARTS_FINE_LINE_WIDTH = 0.5
CHARTS_HAIRLINE_WIDTH = 0.4

CHARTS_MARKER_SIZE = 5.5
CHARTS_MARKER_EDGE_WIDTH = 0.8

CHARTS_GRID_LAYER = 0
CHARTS_BACK_LAYER = 2
CHARTS_FRONT_LAYER = 3
CHARTS_LABEL_LAYER = 4

CHARTS_CODECS_FIGURE_SIZE = (8, 9.6)
CHARTS_LOWEST_SCORE = 20
CHARTS_HIGHEST_SCORE = 102
CHARTS_RATE_MARGIN = 0.18
CHARTS_LEGEND_HEIGHT = 0.04

CHARTS_OUTLIER_RATIO = 2.5
CHARTS_CLIPPED_HEADROOM = 1.35
CHARTS_MINIMUM_CLIPPED_LIMIT = 10
CHARTS_CLIPPED_MARGIN = 1.02

CHARTS_BAR_HEIGHT = 0.38
CHARTS_BAR_GAP = 0.02
CHARTS_BAR_LABEL_OFFSET = (-4, 0)

CHARTS_FEATURES_WIDTH = 9.5
CHARTS_FEATURES_ROW_HEIGHT = 0.55
CHARTS_FEATURES_BASE_HEIGHT = 1.6

SAMPLES_FRAME_ROWS = 2
SAMPLES_COLUMN_WIDTH = 5.4
SAMPLES_FIGURE_HEIGHT = 6.6

SAMPLES_HALF_PIXEL = 0.5
SAMPLES_LEAF_OUTLINE = "#ffffff"

SAMPLES_LEGEND_COLUMNS_PER_FRAME = 3
SAMPLES_LEGEND_ROW_HEIGHT = 0.05
SAMPLES_ROW_PADDING = 2

SAMPLES_LEAF_PANELS = 4
SAMPLES_LEAVES_FIGURE_SIZE = (11, 6.2)
SAMPLES_LIGHT_THRESHOLD = 382
SAMPLES_BITS_COLOUR_LIMIT = 1.6

SAMPLES_HEXDUMP_WIDTH = 16

HASH_CHUNK_BYTES = 1 << 20

VMAF_THREADS = 8

ASCENDING_LEAF_SIZES = tuple(reversed(LEAF_SIZES))

EDGE_STREAMS_PATTERN_ENDPOINTS = (bytes([1, 254, 3, 255, 0, 129]), bytes([127, 128, 77, 2, 253, 250]))

EDGE_STREAMS_RANDOM_CASES = (
    ("tiny", 1, 1),
    ("vertical", 1, 97),
    ("horizontal", 97, 1),
    ("cropped", 97, 65),
    ("directory", MAXIMUM_DIMENSION, 65),
)

SUPERBLOCK_PALETTE_BYTES = COLORS_BYTES + SUPERBLOCK_SIZE * SUPERBLOCK_SIZE // BITS_PER_BYTE

VERSION_3_PREFIX = MAGIC + bytes([VERSION])

FIXTURES_PAGES_EDGE_CASES = (("edge-modes", 0), ("edge-modes", 1), ("edge-tiny", 0), ("edge-long-walk", 1))
FIXTURES_PAGES_EXTRA_CASES = (("edge-directory", 2), ("edge-length-limit", 0))

SYMBOLS_PER_PIXEL = 4
SYMBOL_MASK = (1 << SYMBOL_BITS) - 1
SYMBOL_SHIFTS = tuple(index * SYMBOL_BITS for index in range(SYMBOLS_PER_PIXEL))
PAGE_PIXELS = PAGE_SYMBOLS // SYMBOLS_PER_PIXEL

SHADER_CHECK_RED = (201, 19, 31)
SHADER_CHECK_GREEN = (7, 231, 49)
SHADER_CHECK_BLUE = (23, 57, 211)

SHADER_CHECK_RESTART_CASES = (
    (100, 100, SHADER_CHECK_BLUE, True, SHADER_CHECK_BLUE),
    (7, 7, SHADER_CHECK_RED, True, SHADER_CHECK_RED),
    (7, 7, SHADER_CHECK_GREEN, False, SHADER_CHECK_RED),
    (6, 7, SHADER_CHECK_GREEN, False, SHADER_CHECK_RED),
    (0x80000007, 7, SHADER_CHECK_GREEN, False, SHADER_CHECK_RED),
    (8, 100, SHADER_CHECK_GREEN, False, SHADER_CHECK_RED),
    (8, 7, SHADER_CHECK_GREEN, True, SHADER_CHECK_GREEN),
    (0, 0, SHADER_CHECK_BLUE, True, SHADER_CHECK_BLUE),
    (1, 0, SHADER_CHECK_RED, True, SHADER_CHECK_RED),
)

SHADER_TIMING_FIELD_OF_VIEW = 70.0
SHADER_TIMING_ASPECT_RATIO = 16 / 9
SHADER_TIMING_NEAR = 0.05
SHADER_TIMING_FAR = 1000.0

SHADER_TIMING_DESCRIPTOR_MARKER = 0xA1

EDGE_STREAMS_MOTION_VECTORS = (
    MOTION_MINIMUM_X_MAXIMUM_Y,
    MOTION_MAXIMUM_X_MINIMUM_Y,
    MOTION_MINIMUM_X_MINIMUM_Y,
    MOTION_MAXIMUM_X_MAXIMUM_Y,
)

CHARTS_LABEL_PLACEMENTS = {"vp9": ((7, -1), "left"), MCV2_CODEC: ((0, -13), "center")}
CHARTS_DEFAULT_LABEL_PLACEMENT = ((-7, -1), "right")

WALK_CURSOR_MASK = (1 << WALK_CURSOR_BITS) - 1

CODEC_CURVES_CODECS = {
    "x264": {
        "container": "mkv",
        "arguments": ["-c:v", "libx264", "-preset", "veryslow", "-crf", "{q}", "-pix_fmt", "yuv420p"],
    },
    "vp9": {
        "container": "webm",
        "arguments": [
            "-c:v",
            "libvpx-vp9",
            "-crf",
            "{q}",
            "-b:v",
            "0",
            "-deadline",
            "good",
            "-cpu-used",
            "0",
            "-row-mt",
            "1",
            "-pix_fmt",
            "yuv420p",
        ],
    },
    "av1": {
        "container": "mkv",
        "arguments": [
            "-c:v",
            "libaom-av1",
            "-crf",
            "{q}",
            "-b:v",
            "0",
            "-cpu-used",
            "6",
            "-row-mt",
            "1",
            "-pix_fmt",
            "yuv420p",
        ],
    },
}

CODEC_CURVES_VERSION_PATTERNS = {
    "x264": re.compile(rb"x264 - core \d+ r\d+ \w+"),
    "vp9": re.compile(r"\[libvpx-vp9 @ [^\]]+\] (v\d[^\s]*)"),
    "av1": re.compile(r"\[libaom-av1 @ [^\]]+\] (\d+\.\d+\.\d+[^\s]*)"),
}

COUNTER_VIDEO_BITS = 20
COUNTER_VIDEO_BLOCK = 32
COUNTER_VIDEO_SYNCHRONIZATION_BITS = (1, 0, 1, 0)

EDGE_STREAMS_DEFAULT_SEED = 20261008

FIXTURES_PREFIX_LIMIT = 1_000_000

DIFFERENTIAL_DEFAULT_CORPUS = (
    REPOSITORY_ROOT / CONFORMANCE_RESOURCES_PATH
)

SPIRV_HELP = (
    "run compiled Mcv2Tools shader-compile; use the test runtime classpath from "
    "mcav-bukkit/build/mcv2-tools-classpath.txt"
)
shader_check_pack = REPOSITORY_ROOT / "mcav-bukkit/src/main/resources/mcav/mcv2/pack"
shader_check_screen = (DEFAULT_WIDTH, DEFAULT_HEIGHT)
SHADER_CHECK_STREAM_ID = 7
shader_check_screen_index = 0
shader_check_first_slot = 0
SHADER_CHECK_IDLE_SLOTS = 8
SHADER_CHECK_VERTEX = """#version 330
#extension GL_ARB_separate_shader_objects : require
layout(location = 0) out vec2 texCoord;
void main() {
    vec2 uv = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
    gl_Position = vec4(uv * vec2(2, 2) + vec2(-1, -1), 0, 1);
    texCoord = uv;
}
"""

SHADER_CHECK_BLIT = """#version 330
#extension GL_ARB_separate_shader_objects : require
uniform sampler2D InSampler;
layout(location = 0) out vec4 fragColor;
void main() {
    fragColor = texelFetch(InSampler, ivec2(gl_FragCoord.xy), 0);
}
"""

STRIP_CHECK_HEADER_SYMBOLS = (PAGE_HEADER.size * BITS_PER_BYTE + SYMBOL_BITS - 1) // SYMBOL_BITS
STRIP_CHECK_SQUARE = 20
STRIP_CHECK_STEP = 24
STRIP_CHECK_GREEN, STRIP_CHECK_RED, STRIP_CHECK_BLUE = ((0, 255, 0), (255, 0, 0), (0, 0, 255))
STRIP_CHECK_DESCRIPTOR = [tuple(STRIP_MAGIC), (0xA1, 0, 0)]

STRIP_FIT_CHECK_VIDEO = (64, 64)
STRIP_FIT_CHECK_SLOTS = 8

LATENCY_SAMPLES = COUNTER_VIDEO_BITS + len(COUNTER_VIDEO_SYNCHRONIZATION_BITS)
LATENCY_SAMPLE_OFFSET = COUNTER_VIDEO_BLOCK // 2

CHARTS_DATA = Path(__file__).resolve().parents[1] / "resources/mcv2/data"
IMAGES = REPOSITORY_ROOT / DOCUMENTATION_IMAGES_PATH
CHARTS_SURFACE = "#ffffff"
CHARTS_INK = "#0b0b0b"
CHARTS_SECONDARY = "#52514e"
CHARTS_GRID = "#e4e3df"
CHARTS_FONT = ["Liberation Sans", "Arial", "Helvetica", "DejaVu Sans"]
CHARTS_CODECS = [
    ("mcv2", "MCV2", "#2a78d6", "o"),
    ("x264", "H.264", "#eb6834", "s"),
    ("vp9", "VP9", "#1baf7a", "^"),
    ("av1", "AV1", "#eda100", "D"),
]
CHARTS_SOURCES = [(PROXY_SOURCE, "Minecraft proxy, 1080p30"), (GAMEPLAY_SOURCE, "Minecraft gameplay, 1080p30")]
CHARTS_SOURCE_COLOURS = {PROXY_SOURCE: "#2a78d6", GAMEPLAY_SOURCE: "#eb6834"}
CHARTS_LEVELS = (70, 75, 80, 85, 90)
CHARTS_TICKS = (0.1, 0.2, 0.5, 1, 2, 5, 10, 20, 50)

SAMPLES_MODES = [
    (mcv2_reference.SKIP, "SKIP", "#e4e3df"),
    (mcv2_reference.MOTION, "MOTION", "#2a78d6"),
    (mcv2_reference.SOLID, "SOLID", "#1baf7a"),
    (mcv2_reference.PALETTE, "PALETTE", "#eb6834"),
    (mcv2_reference.PATTERN, "PATTERN", "#eda100"),
    (mcv2_reference.COMPACT, "COMPACT", "#8f5bd6"),
]


def main():
    commands = {
        "bd_rate": bd_rate_main,
        "codec_curves": codec_curves_main,
        "counter_video": counter_video_main,
        "capture_check": capture_check_main,
        "edge_streams": edge_streams_main,
        "fixtures": fixtures_main,
        "differential": differential_main,
        "rate_quality": rate_quality_main,
        "shader_check": shader_check_main,
        "shader_timing": shader_timing_main,
        "strip_check": strip_check_main,
        "strip_fit_check": strip_fit_check_main,
        "latency": latency_main,
        "charts": charts_main,
        "samples": samples_main,
    }
    if len(sys.argv) <= 1 or sys.argv[1] not in commands:
        raise SystemExit("Usage: mcv2_tools.py <" + "|".join(commands) + "> [arguments]")

    command = sys.argv.pop(1)
    sys.argv[0] = f"{sys.argv[0]} {command}"

    return commands[command]()


def bd_rate_main():
    parser = argparse.ArgumentParser(
        description=(
            "Compare cubic log-rate/quality fits over their shared quality range; positive BD-rate means the test "
            "needs more rate. Each JSON curve needs at least four points. Exit 1 on invalid curve data; 2 on "
            "invalid options."
        ),
    )
    parser.add_argument("reference")
    parser.add_argument("test")
    parser.add_argument("--metric", default=VMAF_MEAN_KEY)
    parser.add_argument("--rate", default="map_mbps")
    arguments = parser.parse_args()
    reference_points = bd_rate_points(arguments.reference, arguments.rate, arguments.metric)
    test = bd_rate_points(arguments.test, arguments.rate, arguments.metric)
    if len(reference_points) < BD_RATE_MINIMUM_POINTS or len(test) < BD_RATE_MINIMUM_POINTS:
        message: str = "Each curve needs at least four points!"
        sys.exit(message)

    delta, low, high = bd_rate(reference_points, test)
    print(
        json.dumps(
            {
                "bd_rate_percent": round(delta, BD_RATE_DECIMALS),
                "metric": arguments.metric,
                "rate": arguments.rate,
                "quality_low": round(low, QUALITY_DECIMALS),
                "quality_high": round(high, QUALITY_DECIMALS),
            }
        )
    )


def bd_rate_points(path, rate, metric):
    data = json.loads(Path(path).read_text())
    return sorted((float(point[rate]), float(point[metric])) for point in data)


def bd_rate(reference_points, test):
    reference_rates = numpy.log([point[0] for point in reference_points])
    reference_qualities = numpy.array([point[1] for point in reference_points])
    test_rates = numpy.log([point[0] for point in test])
    test_qualities = numpy.array([point[1] for point in test])
    low = max(reference_qualities.min(), test_qualities.min())
    high = min(reference_qualities.max(), test_qualities.max())
    if high <= low:
        message: str = "The curves share no quality range!"
        raise ValueError(message)

    reference_fit = numpy.polyfit(reference_qualities, reference_rates, BD_RATE_FIT_DEGREE)
    test_fit = numpy.polyfit(test_qualities, test_rates, BD_RATE_FIT_DEGREE)
    reference_area = numpy.polyval(numpy.polyint(reference_fit), high) - numpy.polyval(
        numpy.polyint(reference_fit), low
    )
    test_area = numpy.polyval(numpy.polyint(test_fit), high) - numpy.polyval(numpy.polyint(test_fit), low)
    return ((math.exp((test_area - reference_area) / (high - low)) - 1) * PERCENT, low, high)


def codec_curves_main():
    parser = argparse.ArgumentParser(
        description=(
            "Encode raw RGB24 at H.264, VP9 and AV1 CRFs, decode to RGB24, then score RGB PSNR and libvmaf after "
            "yuv420p conversion. Rates include container bytes only. Resume existing points; refuse a source name "
            "with different content. Exit 1 on an encode/score failure; 2 on conflicting source identity or "
            "invalid options."
        ),
    )
    parser.add_argument(FFMPEG_OPTION, required=True)
    parser.add_argument(SOURCE_OPTION, required=True)
    parser.add_argument("--name", required=True)
    parser.add_argument(WIDTH_OPTION, type=int, required=True)
    parser.add_argument(HEIGHT_OPTION, type=int, required=True)
    parser.add_argument(FRAMES_OPTION, type=int, required=True)
    parser.add_argument(FRAMES_PER_SECOND_OPTION, dest="frames_per_second", type=float, required=True)
    parser.add_argument(
        "--codecs", nargs="+", choices=sorted(CODEC_CURVES_CODECS), default=sorted(CODEC_CURVES_CODECS)
    )
    parser.add_argument("--qualities", nargs="+", type=int, required=True, help="the CRF values to encode at")
    parser.add_argument(OUT_OPTION, required=True)
    arguments = parser.parse_args()
    identity = {
        "width": arguments.width,
        "height": arguments.height,
        "frames": arguments.frames,
        "fps": arguments.frames_per_second,
        "sha256": codec_curves_sha256(arguments.source),
    }
    try:
        measured = codec_curves_resume(arguments.out, arguments.name, identity)
    except ValueError as exception:
        parser.error(str(exception))
    done = {(point[SOURCE_KEY], point[CODEC_KEY], point[QUALITY_KEY]) for point in measured[CODECS_KEY]}
    version = subprocess.run(
        [arguments.ffmpeg, VERSION_FLAG], capture_output=True, text=True
    ).stdout.splitlines()[0]
    measured["ffmpeg"] = version
    pending = [
        (codec, quality)
        for codec in arguments.codecs
        for quality in arguments.qualities
        if (arguments.name, codec, quality) not in done
    ]
    with tempfile.TemporaryDirectory() as folder:
        for codec, quality in pending:
            codec_curves_measure(arguments, measured, codec, quality, folder)


def codec_curves_sha256(path):
    digest = hashlib.sha256()
    with open(path, "rb") as source:
        for chunk in iter(lambda: source.read(HASH_CHUNK_BYTES), b""):
            digest.update(chunk)

    return digest.hexdigest()


def codec_curves_resume(out, name, identity):
    """The measurements already in the output, with the identity of this run's source recorded under its name.

    The curves are kept under "codecs", as in mcav-bukkit/src/test/resources/mcv2/data/codec_curves.json; the first runs wrote them under "points",
    which is read too. A name whose recorded source is other content is refused: its points would mix with this run's.
    """
    measured = json.loads(Path(out).read_text()) if os.path.exists(out) else {SOURCES_KEY: {}, CODECS_KEY: []}
    if LEGACY_CODECS_KEY in measured:
        if CODECS_KEY in measured:
            message: str = f"Dataset {out} holds both points and codecs; resolve the two datasets first!"
            raise ValueError(message)
        measured[CODECS_KEY] = measured.pop(LEGACY_CODECS_KEY)
    previous = measured[SOURCES_KEY].get(name)
    if previous is not None and any(previous.get(key) != value for key, value in identity.items()):
        message: str = f"Name {name} already has measurements of other content; use another --name!"
        raise ValueError(message)

    measured[SOURCES_KEY][name] = identity
    return measured


def codec_curves_measure(arguments, measured, codec, quality, folder):
    point = codec_curves_point(arguments, codec, quality, folder)
    measured[CODECS_KEY].append(point)
    print(json.dumps(point[CODEC_KEY]), quality, point[CONTAINER_RATE_KEY], point[VMAF_MEAN_KEY], flush=True)
    measured[CODECS_KEY].sort(key=lambda entry: (entry[SOURCE_KEY], entry[CODEC_KEY], entry[QUALITY_KEY]))
    codec_curves_save(arguments.out, measured)


def codec_curves_point(arguments, codec, quality, folder):
    settings = CODEC_CURVES_CODECS[codec]
    encoded = os.path.join(folder, f"{codec}-{quality}.{settings['container']}")
    decoded = os.path.join(folder, f"{codec}-{quality}.rgb")
    log = os.path.join(folder, f"{codec}-{quality}-vmaf.json")
    size = (arguments.width, arguments.height, arguments.frames_per_second)
    encode = [
        arguments.ffmpeg,
        HIDE_BANNER_FLAG,
        NO_STANDARD_INPUT_FLAG,
        VERBOSITY_FLAG,
        VERBOSE_LOG_LEVEL,
        OVERWRITE_FLAG,
        *codec_curves_raw(arguments.source, *size),
        FRAME_COUNT_FLAG,
        str(arguments.frames),
        NO_AUDIO_FLAG,
        *[part.replace(CODEC_CURVES_QUALITY_PLACEHOLDER, str(quality)) for part in settings["arguments"]],
        encoded,
    ]
    result, encode_seconds = codec_curves_run(encode)
    decode_command = [
        arguments.ffmpeg,
        HIDE_BANNER_FLAG,
        NO_STANDARD_INPUT_FLAG,
        VERBOSITY_FLAG,
        ERROR_LOG_LEVEL,
        OVERWRITE_FLAG,
        INPUT_FLAG,
        encoded,
        FRAME_RATE_MODE_FLAG,
        PASSTHROUGH_FRAME_RATE_MODE,
        PIXEL_FORMAT_FLAG,
        RGB_PIXEL_FORMAT,
        FORMAT_FLAG,
        RAW_VIDEO_FORMAT,
        decoded,
    ]
    codec_curves_run(decode_command)
    frame_bytes = arguments.width * arguments.height * CHANNELS
    decoded_frames, remainder = divmod(os.path.getsize(decoded), frame_bytes)
    if remainder or decoded_frames != arguments.frames:
        message: str = f"Codec {codec} at quality {quality} decoded {decoded_frames} frames instead of {arguments.frames} frames!"
        raise RuntimeError(message)

    score = [
        arguments.ffmpeg,
        HIDE_BANNER_FLAG,
        NO_STANDARD_INPUT_FLAG,
        VERBOSITY_FLAG,
        ERROR_LOG_LEVEL,
        OVERWRITE_FLAG,
        *codec_curves_raw(arguments.source, *size),
        *codec_curves_raw(decoded, *size),
        FILTER_GRAPH_FLAG,
        vmaf_filter(log),
        FORMAT_FLAG,
        NULL_FORMAT,
        STANDARD_STREAM,
    ]
    codec_curves_run(score)
    vmaf = json.loads(Path(log).read_text())
    frames = [frame[VMAF_METRICS_KEY][VMAF_SCORE_KEY] for frame in vmaf[VMAF_FRAMES_KEY]]
    if len(frames) != arguments.frames:
        message: str = f"Codec {codec} at quality {quality} got {len(frames)} libvmaf scores for {arguments.frames} frames!"
        raise RuntimeError(message)

    container_bytes = os.path.getsize(encoded)
    seconds = arguments.frames / arguments.frames_per_second
    version = CODEC_CURVES_VERSION_PATTERNS[codec]
    if codec == X264_CODEC:
        found = version.search(Path(encoded).read_bytes())
        library = found.group(0).decode() if found else None
    else:
        found = version.search(result.stderr)
        library = found.group(1) if found else None
    names = {
        arguments.source: "$SRC",
        encoded: "ENCODED",
        decoded: "DECODED",
        log: "VMAF.json",
        arguments.ffmpeg: "$FFMPEG",
    }
    return {
        SOURCE_KEY: arguments.name,
        CODEC_KEY: codec,
        "library": library,
        QUALITY_KEY: quality,
        "container_bytes": container_bytes,
        CONTAINER_RATE_KEY: round(container_bytes * BITS_PER_BYTE / seconds / BITS_PER_MEGABIT, MEASUREMENT_DECIMALS),
        VMAF_MEAN_KEY: round(vmaf["pooled_metrics"][VMAF_SCORE_KEY]["mean"], MEASUREMENT_DECIMALS),
        VMAF_MINIMUM_KEY: round(min(frames), MEASUREMENT_DECIMALS),
        "rgb_psnr": round(
            codec_curves_rgb_psnr(
                arguments.source, decoded, arguments.frames, arguments.width, arguments.height
            ),
            QUALITY_DECIMALS,
        ),
        "encode_seconds": round(encode_seconds, SECONDS_DECIMALS),
        "commands": {
            "encode": codec_curves_placeholders(encode, names),
            "decode": codec_curves_placeholders(decode_command, names),
            "score": codec_curves_placeholders(score, names),
        },
    }


def codec_curves_raw(path, width, height, frame_rate):
    return [
        FORMAT_FLAG,
        RAW_VIDEO_FORMAT,
        RAW_PIXEL_FORMAT_FLAG,
        RGB_PIXEL_FORMAT,
        VIDEO_SIZE_FLAG,
        f"{width}x{height}",
        FRAME_RATE_FLAG,
        str(frame_rate),
        INPUT_FLAG,
        path,
    ]


def codec_curves_run(command):
    started = time.perf_counter()
    result = subprocess.run(command, capture_output=True, text=True)
    if result.returncode != 0:
        message: str = f"Command {command[0]} failed!\n{result.stderr[-STDERR_TAIL_CHARACTERS:]}"
        raise RuntimeError(message)

    return (result, time.perf_counter() - started)


def vmaf_filter(log, threads=VMAF_THREADS):
    return (
        "[0:v]format=yuv420p[ref];[1:v]format=yuv420p[dis];"
        f"[dis][ref]libvmaf=n_threads={threads}:log_fmt=json:log_path={log}"
    )


def codec_curves_rgb_psnr(source, decoded, frames, width, height):
    shape = (frames, height, width, CHANNELS)
    reference_frames = numpy.memmap(source, dtype=numpy.uint8, mode="r", shape=shape)
    picture = numpy.memmap(decoded, dtype=numpy.uint8, mode="r", shape=shape)
    errors = []
    for first, second in zip(reference_frames, picture, strict=True):
        difference = first.astype(numpy.float64) - second.astype(numpy.float64)
        errors.append(float(numpy.mean(difference * difference)))
    mean_squared_error = float(numpy.mean(errors))
    return IDENTICAL_PSNR if mean_squared_error == 0 else float(DECIBELS_PER_BEL * numpy.log10(MAXIMUM_CHANNEL**2 / mean_squared_error))


def codec_curves_placeholders(command, replacements):
    text = []
    for part in command:
        for local, name in replacements.items():
            part = part.replace(local, name)
        text.append(part)

    return text


def codec_curves_save(path, measured):
    temporary = path + PARTIAL_SUFFIX
    with open(temporary, "w") as out:
        json.dump(measured, out, indent=JSON_INDENT)
        out.write(LINE_SEPARATOR)

    os.replace(temporary, path)


def counter_video_main():
    parser = argparse.ArgumentParser(
        description=(
            "Loop an RGB24 clip forward and backward, stamping its frame number into twenty bits plus four sync "
            "blocks for latency measurements. Write raw RGB or CRF-12 H.264. Exit 1 on input failure; 2 on "
            "invalid options. Encoded output exits with ffmpeg's status."
        ),
    )
    parser.add_argument("clip")
    parser.add_argument("width", type=int)
    parser.add_argument("height", type=int)
    parser.add_argument("frames_per_second", type=int)
    parser.add_argument("seconds", type=float)
    parser.add_argument("out")
    parser.add_argument(FFMPEG_OPTION, default=FFMPEG_COMMAND)
    arguments = parser.parse_args()
    width, height = (arguments.width, arguments.height)
    size = width * height * CHANNELS
    clip = numpy.memmap(arguments.clip, dtype=numpy.uint8, mode="r")
    count = clip.size // size
    if (
        count < 1
        or width < COUNTER_VIDEO_BLOCK * (COUNTER_VIDEO_BITS + len(COUNTER_VIDEO_SYNCHRONIZATION_BITS))
        or height < COUNTER_VIDEO_BLOCK
    ):
        minimum_width: int = COUNTER_VIDEO_BLOCK * (COUNTER_VIDEO_BITS + len(COUNTER_VIDEO_SYNCHRONIZATION_BITS))
        message: str = f"The clip must have a frame at least {minimum_width} pixels wide and {COUNTER_VIDEO_BLOCK} pixels high!"
        sys.exit(message)

    frames = int(arguments.seconds * arguments.frames_per_second)
    raw = arguments.out.endswith(RAW_SUFFIX)
    encoder = (
        None
        if raw
        else subprocess.Popen(
            [
                arguments.ffmpeg,
                NO_STANDARD_INPUT_FLAG,
                LOG_LEVEL_FLAG,
                ERROR_LOG_LEVEL,
                OVERWRITE_FLAG,
                FORMAT_FLAG,
                RAW_VIDEO_FORMAT,
                PIXEL_FORMAT_FLAG,
                RGB_PIXEL_FORMAT,
                FRAME_SIZE_FLAG,
                f"{width}x{height}",
                RATE_FLAG,
                str(arguments.frames_per_second),
                INPUT_FLAG,
                STANDARD_STREAM,
                VIDEO_CODEC_FLAG,
                X264_ENCODER,
                PRESET_FLAG,
                COUNTER_VIDEO_PRESET,
                CONSTANT_RATE_FACTOR_FLAG,
                COUNTER_VIDEO_CONSTANT_RATE_FACTOR,
                KEYFRAME_INTERVAL_FLAG,
                str(COUNTER_VIDEO_KEYFRAME_SECONDS * arguments.frames_per_second),
                PIXEL_FORMAT_FLAG,
                YUV_PIXEL_FORMAT,
                arguments.out,
            ],
            stdin=subprocess.PIPE,
        )
    )
    sink = open(arguments.out, "wb") if raw else encoder.stdin
    period = max(1, 2 * (count - 1))
    for number in range(frames):
        phase = number % period
        index = phase if phase < count else period - phase
        frame = numpy.array(clip[index * size : (index + 1) * size]).reshape(height, width, CHANNELS)
        counter_video_stamp(frame, number)
        sink.write(frame.tobytes())
    sink.close()
    sys.exit(SUCCESS_EXIT_CODE if raw else encoder.wait())


def counter_video_stamp(frame, number):
    values = [number >> bit & 1 for bit in range(COUNTER_VIDEO_BITS)] + list(COUNTER_VIDEO_SYNCHRONIZATION_BITS)
    for position, value in enumerate(values):
        frame[
            0:COUNTER_VIDEO_BLOCK, position * COUNTER_VIDEO_BLOCK : (position + 1) * COUNTER_VIDEO_BLOCK
        ] = MAXIMUM_CHANNEL if value else 0


def capture_check_main():
    parser = argparse.ArgumentParser(
        description=(
            "Match debug-view PNG captures below --top to decoded RGB24 reference pictures; report exact, "
            "ambiguous and nearest matches, PSNR, SSIM and optional VMAF. Identical reference pictures cannot "
            "distinguish frame occurrences. Exit 1 if any capture is not exact or any distinct picture is never "
            "seen exactly; 2 on invalid options."
        ),
    )
    parser.add_argument("reference")
    parser.add_argument("width", type=int)
    parser.add_argument("height", type=int)
    parser.add_argument("captures")
    parser.add_argument("--top", type=int, default=CAPTURE_CHECK_DEFAULT_TOP)
    parser.add_argument("--vmaf")
    arguments = parser.parse_args()
    from PIL import Image

    width, height = (arguments.width, arguments.height)
    reference_frames = numpy.fromfile(arguments.reference, numpy.uint8).reshape(-1, height, width, CHANNELS)
    paths = sorted(Path(arguments.captures).glob(SCREENSHOT_GLOB))
    screen_height, screen_width = numpy.asarray(Image.open(paths[0]).convert("RGB")).shape[:2]
    visible = min(height, screen_height - arguments.top)
    shown = min(width, screen_width)
    if visible < height or shown < width:
        print(
            "the screen shows %dx%d of the %dx%d picture; the rest is not compared"
            % (shown, visible, width, height)
        )
        reference_frames = numpy.ascontiguousarray(reference_frames[:, :visible, :shown])
        height, width = (visible, shown)
    index = {}
    for frame_index, frame in enumerate(reference_frames):
        index.setdefault(hashlib.sha256(frame.tobytes()).hexdigest(), []).append(frame_index)
    exact, near = ({}, [])
    pairs = []
    for path in paths:
        screen = numpy.asarray(Image.open(path).convert("RGB"))
        crop = numpy.ascontiguousarray(screen[arguments.top : arguments.top + height, :width])
        key = hashlib.sha256(crop.tobytes()).hexdigest()
        if key in index:
            exact.setdefault(key, path.name)
            pairs.append((index[key][0], crop))
            continue
        scores = [capture_check_psnr(crop, frame) for frame in reference_frames]
        best = int(numpy.argmax(scores))
        near.append((path.name, best, scores[best]))
        pairs.append((best, crop))
    unique_frames_seen = sum(1 for key in exact if len(index[key]) == 1)
    ambiguous_frames = sum(len(occurrences) for occurrences in index.values() if len(occurrences) > 1)
    print("captures:", len(pairs), "- distinct pictures seen exactly:", len(exact), "of", len(index))
    if ambiguous_frames:
        print(
            " ",
            ambiguous_frames,
            "reference frame occurrences are indistinguishable; their individual display is unknown",
        )
    for name, best, score in near:
        print("  %s is not exact: closest frame %d, PSNR %.2f dB" % (name, best, score))
    missing = sorted(occurrences[0] for key, occurrences in index.items() if key not in exact)
    if missing:
        print("  pictures never seen exactly (first reference frame):", missing)
    captured = [crop for _, crop in pairs]
    matched = [reference_frames[frame_index] for frame_index, _ in pairs]
    psnrs = [capture_check_psnr(capture, expected) for capture, expected in zip(captured, matched)]
    finite = [value for value in psnrs if value != math.inf]
    summary = {
        "captures": len(pairs),
        "exact_captures": len(pairs) - len(near),
        "frames_seen_exactly": unique_frames_seen,
        "frames": len(reference_frames),
        "pictures_seen_exactly": len(exact),
        "distinct_pictures": len(index),
        "ambiguous_reference_frames": ambiguous_frames,
        "psnr_min_db": min(finite) if finite else "inf",
        "ssim_mean": float(
            numpy.mean(
                [capture_check_ssim(capture, expected) for capture, expected in zip(captured, matched)]
            )
        ),
    }
    if arguments.vmaf:
        summary[VMAF_MEAN_KEY], summary[VMAF_MINIMUM_KEY] = capture_check_vmaf(
            arguments.vmaf, matched, captured, width, height
        )
    print(json.dumps(summary))
    sys.exit(SUCCESS_EXIT_CODE if not near and (not missing) else FAILURE_EXIT_CODE)


def capture_check_psnr(captured, expected):
    mean_squared_error = numpy.mean((captured.astype(numpy.float64) - expected.astype(numpy.float64)) ** 2)
    return math.inf if mean_squared_error == 0 else float(DECIBELS_PER_BEL * numpy.log10(MAXIMUM_CHANNEL**2 / mean_squared_error))


def capture_check_ssim(captured, expected):

    def luma(picture):
        picture = picture.astype(numpy.float64)
        return LUMA_RED_WEIGHT * picture[..., 0] + LUMA_GREEN_WEIGHT * picture[..., 1] + LUMA_BLUE_WEIGHT * picture[..., 2]

    captured_luma, expected_luma = (luma(captured), luma(expected))
    height, width = (captured_luma.shape[0] // SSIM_BLOCK * SSIM_BLOCK, captured_luma.shape[1] // SSIM_BLOCK * SSIM_BLOCK)
    captured_luma = captured_luma[:height, :width].reshape(height // SSIM_BLOCK, SSIM_BLOCK, width // SSIM_BLOCK, SSIM_BLOCK).swapaxes(1, 2)
    expected_luma = expected_luma[:height, :width].reshape(height // SSIM_BLOCK, SSIM_BLOCK, width // SSIM_BLOCK, SSIM_BLOCK).swapaxes(1, 2)
    captured_mean, expected_mean = (captured_luma.mean(axis=(2, 3)), expected_luma.mean(axis=(2, 3)))
    captured_variance, expected_variance = (captured_luma.var(axis=(2, 3)), expected_luma.var(axis=(2, 3)))
    covariance = (
        (captured_luma - captured_mean[..., None, None]) * (expected_luma - expected_mean[..., None, None])
    ).mean(axis=(2, 3))
    luminance_constant, contrast_constant = ((SSIM_LUMINANCE_FACTOR * MAXIMUM_CHANNEL) ** 2, (SSIM_CONTRAST_FACTOR * MAXIMUM_CHANNEL) ** 2)
    return float(
        numpy.mean(
            (2 * captured_mean * expected_mean + luminance_constant)
            * (2 * covariance + contrast_constant)
            / (
                (captured_mean**2 + expected_mean**2 + luminance_constant)
                * (captured_variance + expected_variance + contrast_constant)
            )
        )
    )


def capture_check_vmaf(ffmpeg, reference_frames, captured, width, height):
    with tempfile.TemporaryDirectory() as folder:
        reference_picture, distorted_picture, log = (
            Path(folder, REFERENCE_PICTURES_NAME),
            Path(folder, DISTORTED_PICTURES_NAME),
            Path(folder, VMAF_LOG_NAME),
        )
        reference_picture.write_bytes(b"".join(frame.tobytes() for frame in reference_frames))
        distorted_picture.write_bytes(b"".join(frame.tobytes() for frame in captured))
        graph = vmaf_filter(log)
        subprocess.run(
            [
                ffmpeg,
                NO_STANDARD_INPUT_FLAG,
                VERBOSITY_FLAG,
                ERROR_LOG_LEVEL,
                OVERWRITE_FLAG,
                *codec_curves_raw(reference_picture, width, height, CAPTURE_CHECK_FRAME_RATE),
                *codec_curves_raw(distorted_picture, width, height, CAPTURE_CHECK_FRAME_RATE),
                FILTER_GRAPH_FLAG,
                graph,
                FORMAT_FLAG,
                NULL_FORMAT,
                STANDARD_STREAM,
            ],
            check=True,
        )
        frames = json.loads(log.read_text())[VMAF_FRAMES_KEY]
        scores = [frame[VMAF_METRICS_KEY][VMAF_SCORE_KEY] for frame in frames]
        return (float(numpy.mean(scores)), float(numpy.min(scores)))


def edge_streams_main():
    parser = argparse.ArgumentParser(
        description=(
            "Build deterministic v3 block-tree archives, RGB digests and a spec section 9 rejection catalog using "
            "the independent serializer. Exit 1 if a generated fixture fails validation; 2 on invalid options."
        ),
    )
    parser.add_argument("output", type=Path)
    parser.add_argument("seed", nargs="?", type=int, default=EDGE_STREAMS_DEFAULT_SEED)
    arguments = parser.parse_args()
    edge_streams_generate(arguments.output, arguments.seed)


def edge_streams_generate(output, seed=EDGE_STREAMS_DEFAULT_SEED):
    output.mkdir(parents=True, exist_ok=True)
    streams = edge_streams_build_streams(seed)
    for old in output.glob(EDGE_STREAM_GLOB):
        if old.name not in streams:
            old.unlink()
    digests = {}
    for name, frames in streams.items():
        decoder = Decoder()
        digests[name] = [hashlib.sha256(decoder.accept(data).tobytes()).hexdigest() for data in frames]
        output.joinpath(name).write_bytes(archive_bytes(frames))
    output.joinpath(DIGESTS_NAME).write_text(json.dumps(digests, indent=JSON_INDENT) + LINE_SEPARATOR)
    from rejection_cases import rejected_frames

    rejected = rejected_frames()
    for name, case in rejected.items():
        edge_streams_check_rejected(name, case)
    output.joinpath(REJECTED_NAME).write_text(json.dumps(rejected, indent=JSON_INDENT) + LINE_SEPARATOR)
    print(
        json.dumps(
            {"streams": len(streams), "frames": sum(map(len, streams.values())), "rejected": len(rejected)}
        )
    )


def edge_streams_build_streams(seed=EDGE_STREAMS_DEFAULT_SEED):
    randomizer = random.Random(seed)
    streams = {
        "edge-modes.mcs": edge_streams_mode_stream(randomizer),
        "edge-patterns.mcs": edge_streams_pattern_stream(randomizer),
    }
    for name, width, height in EDGE_STREAMS_RANDOM_CASES:
        streams[f"edge-{name}.mcs"] = edge_streams_random_stream(randomizer, width, height)
    streams[
        "edge-absent.mcs"] = [pack_frame(EDGE_STREAMS_SMALL_WIDTH, EDGE_STREAMS_SMALL_HEIGHT, 0, 0, {}),
        pack_frame(EDGE_STREAMS_SMALL_WIDTH, EDGE_STREAMS_SMALL_HEIGHT, 1, 0, {}),
    ]
    motion = [
        pack_frame(
            EDGE_STREAMS_SMALL_WIDTH,
            EDGE_STREAMS_SMALL_HEIGHT,
            0,
            0,
            {
                0: Node(mcv2_reference.SOLID, record=b"\xff\x80\x00"),
                1: Node(mcv2_reference.SOLID, record=b"\x04\x32\x96"),
            },
        )
    ]
    for index, vector in enumerate(EDGE_STREAMS_MOTION_VECTORS, 1):
        motion.append(
            pack_frame(
                EDGE_STREAMS_SMALL_WIDTH,
                EDGE_STREAMS_SMALL_HEIGHT,
                index,
                index - 1,
                {
                    0: Node(mcv2_reference.MOTION, record=vector),
                    1: edge_streams_repeated(
                        Node(mcv2_reference.COMPACT, EDGE_STREAMS_MOTION_QUANTIZER, vector + bytes(GRID_BYTES)), SMALLEST_LEAF_SIZE
                    ),
                },
            )
        )
    streams["edge-motion.mcs"] = motion
    long_roots = {
        index: Node(
            mcv2_reference.SPLIT,
            children=tuple(
                Node(
                    mcv2_reference.SPLIT,
                    children=tuple(
                        Node(
                            mcv2_reference.COMPACT,
                            (index + entry_index) % (mcv2_reference.MAXIMUM_QUANTIZER + 1),
                            edge_streams_compact_record(randomizer),
                        )
                        for entry_index in range(SPLIT_CHILDREN)
                    ),
                )
                for _ in range(SPLIT_CHILDREN)
            ),
        )
        for index in range(EDGE_STREAMS_LONG_WALK_ROOTS)
    }
    streams[
        "edge-long-walk.mcs"] = [pack_frame(EDGE_STREAMS_LONG_WALK_WIDTH, EDGE_STREAMS_LONG_WALK_HEIGHT, 0, 0, {}),
        pack_frame(EDGE_STREAMS_LONG_WALK_WIDTH, EDGE_STREAMS_LONG_WALK_HEIGHT, 1, 0, long_roots),
    ]
    roots = {
        index: edge_streams_repeated(Node(mcv2_reference.SKIP), SMALLEST_LEAF_SIZE)
        for index in range(EDGE_STREAMS_MAXIMUM_SPLITS_ROOTS)
    }
    streams["edge-max-splits.mcs"] = [pack_frame(MAXIMUM_DIMENSION, MAXIMUM_DIMENSION, 0, 0, roots)]
    roots = {
        index: Node(mcv2_reference.PALETTE, record=edge_streams_random_bytes(randomizer, SUPERBLOCK_PALETTE_BYTES))
        for index in range(EDGE_STREAMS_LENGTH_LIMIT_PALETTES)
    }
    roots[EDGE_STREAMS_LENGTH_LIMIT_PALETTES] = Node(
        mcv2_reference.PATTERN, record=edge_streams_pattern_record(randomizer, SUPERBLOCK_SIZE)
    )
    roots.update(
        {
            index: Node(mcv2_reference.SOLID, record=edge_streams_random_bytes(randomizer, SOLID_BYTES))
            for index in range(EDGE_STREAMS_LENGTH_LIMIT_PALETTES + 1, EDGE_STREAMS_LENGTH_LIMIT_ROOTS)
        }
    )
    streams["edge-length-limit.mcs"] = [pack_frame(EDGE_STREAMS_LENGTH_LIMIT_SIDE, EDGE_STREAMS_LENGTH_LIMIT_SIDE, 0, 0, roots)]
    streams["edge-wrap.mcs"] = [
        pack_frame(
            1,
            1,
            frame_id,
            frame_id if index == 0 else (frame_id - 1) & mcv2_reference.ID_MASK,
            {0: Node(mcv2_reference.SOLID, record=b"\x1b@\x80")} if index == 0 else {},
        )
        for index, frame_id in enumerate((mcv2_reference.ID_MASK - 1, mcv2_reference.ID_MASK, 0, 1))
    ]
    return streams


def edge_streams_mode_stream(randomizer):
    nodes = []
    for size in ASCENDING_LEAF_SIZES:
        nodes.extend(
            edge_streams_repeated(edge_streams_leaf(randomizer, size, False, mode), size)
            for mode in range(mcv2_reference.COMPACT)
        )
        for quantizer in range(mcv2_reference.MAXIMUM_QUANTIZER + 1):
            nodes.extend(edge_streams_compact_nodes(randomizer, size, quantizer))
    superblocks_per_row = EDGE_STREAMS_MODES_WIDTH // SUPERBLOCK_SIZE
    height = (len(nodes) + superblocks_per_row - 1) // superblocks_per_row * SUPERBLOCK_SIZE
    key_roots = {
        index: edge_streams_repeated(edge_streams_leaf(randomizer, size, True, mode), size)
        for index, (size, mode) in enumerate(
            (size, mode)
            for size in ASCENDING_LEAF_SIZES
            for mode in (
                mcv2_reference.SKIP,
                mcv2_reference.SOLID,
                mcv2_reference.PALETTE,
                mcv2_reference.PATTERN,
            )
        )
    }
    return [
        pack_frame(EDGE_STREAMS_MODES_WIDTH, height, 0, 0, key_roots),
        pack_frame(EDGE_STREAMS_MODES_WIDTH, height, 1, 0, dict(enumerate(nodes))),
    ]


def edge_streams_repeated(node, size):
    while size < SUPERBLOCK_SIZE:
        node = Node(mcv2_reference.SPLIT, children=(node,) * SPLIT_CHILDREN)
        size *= QUADRANTS_PER_SIDE

    return node


def edge_streams_leaf(randomizer, size, keyframe, mode=None):
    modes = (
        (mcv2_reference.SKIP, mcv2_reference.SOLID, mcv2_reference.PALETTE, mcv2_reference.PATTERN)
        if keyframe
        else tuple(range(mcv2_reference.SPLIT))
    )
    mode = randomizer.choice(modes) if mode is None else mode
    if mode == mcv2_reference.SKIP:
        return Node(mode)

    if mode == mcv2_reference.MOTION:
        return Node(mode, record=edge_streams_random_bytes(randomizer, MOTION_BYTES))

    if mode == mcv2_reference.SOLID:
        return Node(mode, record=edge_streams_random_bytes(randomizer, SOLID_BYTES))

    if mode == mcv2_reference.PALETTE:
        return Node(mode, record=edge_streams_random_bytes(randomizer, COLORS_BYTES + size * size // BITS_PER_BYTE))

    if mode == mcv2_reference.PATTERN:
        return Node(mode, record=edge_streams_pattern_record(randomizer, size))

    return Node(
        mcv2_reference.COMPACT,
        randomizer.randrange(mcv2_reference.MAXIMUM_QUANTIZER + 1),
        edge_streams_compact_record(randomizer),
    )


def edge_streams_random_bytes(randomizer, count):
    return bytes(randomizer.randrange(BYTE_VALUES) for _ in range(count))


def edge_streams_pattern_record(randomizer, size):
    return (
        edge_streams_random_bytes(randomizer, COLORS_BYTES)
        + bytes([randomizer.randrange(PATTERN_ORIENTATIONS)])
        + edge_streams_random_bytes(randomizer, size // BITS_PER_BYTE)
    )


def edge_streams_compact_record(randomizer):
    return edge_streams_random_bytes(randomizer, mcv2_reference.COMPACT_BYTES)


def edge_streams_compact_nodes(randomizer, size, quantizer):
    records = (
        (b"\x00\x00", edge_streams_random_bytes(randomizer, GRID_BYTES)),
        (MOTION_MINIMUM_X_MAXIMUM_Y, bytes([MAXIMUM_NIBBLE_PAIR]) * GRID_BYTES),
        (MOTION_MAXIMUM_X_MINIMUM_Y, bytes([MINIMUM_NIBBLE_PAIR]) * GRID_BYTES),
        (edge_streams_random_bytes(randomizer, MOTION_BYTES), edge_streams_random_bytes(randomizer, GRID_BYTES)),
    )
    return [edge_streams_repeated(Node(mcv2_reference.COMPACT, quantizer, vector + luma), size) for vector, luma in records]


def edge_streams_pattern_stream(randomizer):
    leaves = [
        (size, orientation, axis)
        for size in ASCENDING_LEAF_SIZES
        for orientation in (0, 1)
        for axis in edge_streams_pattern_axes(randomizer, size)
    ]
    roots = {}
    for index, (size, orientation, axis) in enumerate(leaves):
        endpoints = EDGE_STREAMS_PATTERN_ENDPOINTS[index % len(EDGE_STREAMS_PATTERN_ENDPOINTS)]
        record = endpoints + bytes([orientation]) + axis
        roots[index] = edge_streams_repeated(Node(mcv2_reference.PATTERN, record=record), size)

    swapped = {
        index: Node(mcv2_reference.SKIP)
        if index % EDGE_STREAMS_SKIP_PERIOD == 0
        else edge_streams_repeated(
            Node(mcv2_reference.PATTERN, record=edge_streams_pattern_record(randomizer, SMALLEST_LEAF_SIZE)), SMALLEST_LEAF_SIZE
        )
        for index in roots
    }
    return [
        pack_frame(EDGE_STREAMS_PATTERN_WIDTH, EDGE_STREAMS_PATTERN_HEIGHT, 0, 0, roots),
        pack_frame(EDGE_STREAMS_PATTERN_WIDTH, EDGE_STREAMS_PATTERN_HEIGHT, 1, 0, swapped),
    ]


def edge_streams_pattern_axes(randomizer, size):
    axis_bytes = size // BITS_PER_BYTE
    return (
        bytes(axis_bytes),
        bytes([EDGE_STREAMS_FULL_AXIS]) * axis_bytes,
        bytes([EDGE_STREAMS_MIXED_AXIS]) * axis_bytes,
        edge_streams_random_bytes(randomizer, axis_bytes),
    )


def edge_streams_random_stream(randomizer, width=None, height=None, frame_count=EDGE_STREAMS_FRAMES):
    width = randomizer.randint(MINIMUM_DIMENSION, EDGE_STREAMS_MAXIMUM_WIDTH) if width is None else width
    height = randomizer.randint(MINIMUM_DIMENSION, EDGE_STREAMS_MAXIMUM_HEIGHT) if height is None else height
    count = (width + SUPERBLOCK_SIZE - 1) // SUPERBLOCK_SIZE * ((height + SUPERBLOCK_SIZE - 1) // SUPERBLOCK_SIZE)
    stream = []
    for frame_id in range(frame_count):
        keyframe = frame_id == 0 or randomizer.random() < EDGE_STREAMS_KEYFRAME_CHANCE
        roots = {
            index: edge_streams_tree(randomizer, SUPERBLOCK_SIZE, keyframe)
            for index in range(count)
            if randomizer.random() > EDGE_STREAMS_ABSENT_CHANCE
        }
        stream.append(pack_frame(width, height, frame_id, frame_id if keyframe else frame_id - 1, roots))

    return stream


def edge_streams_tree(randomizer, size, keyframe, split_chance=EDGE_STREAMS_SPLIT_CHANCE):
    if size > SMALLEST_LEAF_SIZE and randomizer.random() < split_chance:
        return Node(
            mcv2_reference.SPLIT,
            children=tuple(
                edge_streams_tree(randomizer, size // QUADRANTS_PER_SIDE, keyframe, split_chance) for _ in range(SPLIT_CHILDREN)
            ),
        )

    return edge_streams_leaf(randomizer, size, keyframe)


def archive_bytes(chunks):
    return b"".join(struct.pack(LENGTH_FORMAT, len(chunk)) + chunk for chunk in chunks)


def edge_streams_check_rejected(name, case):
    try:
        parse_frame(bytes.fromhex(case["frame"]))
    except ValueError as exception:
        if case["reason"] not in str(exception):
            message: str = f"Fixture {name} must be rejected for {case['reason']!r}, but it was rejected for {str(exception)!r} instead!"
            raise ValueError(message) from exception
    else:
        message: str = f"Rejected fixture {name} was accepted!"
        raise ValueError(message)


def fixtures_main():
    parser = argparse.ArgumentParser(
        description=(
            "Regenerate independent v3 RGB digests, transport pages and edge streams. Validate Java "
            "conformance/golden archives without rewriting them; leave non-v3 streams untouched. Archives repeat "
            "a little-endian u32 length and that many frame bytes. Exit 1 on validation failure; 2 on invalid "
            "options."
        ),
    )
    parser.add_argument("root", type=Path)
    parser.add_argument(
        "what", nargs="?", default=ALL_FIXTURES, choices=("conformance", "edge", "pages", "encoder", ALL_FIXTURES)
    )
    arguments = parser.parse_args()
    steps = dict(
        conformance=fixtures_conformance, edge=fixtures_edge, pages=fixtures_pages, encoder=fixtures_encoder
    )
    for step in steps if arguments.what == ALL_FIXTURES else [arguments.what]:
        steps[step](arguments.root)


def fixtures_conformance(root):
    output = root / CONFORMANCE_DIRECTORY
    path = output / DIGESTS_NAME
    table = json.loads(path.read_text()) if path.exists() else {}
    updated = False
    for stream in sorted(output.glob(STREAM_GLOB)):
        result = fixtures_version_3_stream(stream)
        if result is None:
            continue
        data, kept = result
        if len(data) > FIXTURES_PREFIX_LIMIT or not parse_frame(kept[0]).keyframe:
            message: str = f"Conformance stream {stream} must start with a keyframe and fit within {FIXTURES_PREFIX_LIMIT} bytes!"
            raise ValueError(message)
        table[stream.name] = dict(frames=len(kept), bytes=len(data), sha256_per_frame=fixtures_digests(data))
        updated = True
        print(f"Checked {len(kept)} v3 frames of {stream.name}.", file=sys.stderr)
    if updated:
        fixtures_write_json(path, table)


def fixtures_version_3_stream(path):
    data = path.read_bytes()
    kept = list(archive_frames(data))
    if not kept:
        message: str = f"Archive {path} is empty!"
        raise ValueError(message)

    if not kept[0].startswith(VERSION_3_PREFIX):
        print(f"Skipping stream {path}, which is not MCV2 version 3.", file=sys.stderr)
        return None

    return (data, kept)


def archive_frames(data):
    offset = 0
    while offset < len(data):
        if offset + LENGTH_BYTES > len(data):
            message: str = "The archive has a truncated frame length!"
            raise ValueError(message)
        length = struct.unpack_from(LENGTH_FORMAT, data, offset)[0]
        offset += LENGTH_BYTES
        if offset + length > len(data):
            message: str = "The archive has a truncated frame!"
            raise ValueError(message)
        yield data[offset : offset + length]
        offset += length


def fixtures_digests(data):
    decoder = Decoder()
    return [hashlib.sha256(decoder.accept(frame).tobytes()).hexdigest() for frame in archive_frames(data)]


def fixtures_write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=JSON_INDENT) + LINE_SEPARATOR)


def fixtures_edge(root):
    edge_streams_generate(root / EDGE_DIRECTORY)


def fixtures_pages(root):
    cases = []
    for stream in sorted((root / CONFORMANCE_DIRECTORY).glob(STREAM_GLOB)):
        result = fixtures_version_3_stream(stream)
        if result is not None:
            cases.extend(
                (str(stream.relative_to(root)), index, frame) for index, frame in enumerate(result[1])
            )
    if cases:
        source = FIXTURES_PAGES_COMMITTED_SOURCE
        cases = [cases[index % len(cases)] for index in range(FIXTURES_PAGES_CASES)]
    else:
        source = "edge streams (no committed v3 conformance streams)"
        cases = [
            (
                f"edge/{name}.mcs",
                index,
                list(read_archive(root / EDGE_DIRECTORY / f"{name}.mcs"))[index],
            )
            for name, index in FIXTURES_PAGES_EDGE_CASES
        ]
    for name, index in FIXTURES_PAGES_EXTRA_CASES:
        path = root / EDGE_DIRECTORY / f"{name}.mcs"
        if path.exists():
            cases.append((f"edge/{name}.mcs", index, list(read_archive(path))[index]))
    if len(cases) > FIXTURES_PAGES_CASES and source == FIXTURES_PAGES_COMMITTED_SOURCE:
        source = "committed v3 conformance and edge streams"
    entries = []
    for stream, index, frame in cases:
        symbols = make_pages(frame, FIXTURES_PAGES_STREAM_ID)
        entries.append(
            dict(
                stream=stream,
                frame=index,
                pages=[hashlib.sha256(page).hexdigest() for page in symbols],
                lengths=[len(page) for page in symbols],
                wire=wire_bytes(symbols),
                wire_full=wire_bytes(symbols, MapCharge.FULL_MAPS),
            )
        )
    fixtures_write_json(
        root / CONFORMANCE_PAGES_PATH, dict(source=source, stream_id=FIXTURES_PAGES_STREAM_ID, symbol_bits=SYMBOL_BITS, frames=entries)
    )


def read_archive(path):
    return archive_frames(Path(path).read_bytes())


def fixtures_encoder(root):
    checked = 0
    for stream in sorted((root / ENCODER_DIRECTORY).glob(STREAM_GLOB)):
        result = fixtures_version_3_stream(stream)
        if result is None:
            continue
        decoded = fixtures_digests(result[0])
        checked += 1
        print(f"Decoded {len(decoded)} golden v3 frames of {stream.name}.", file=sys.stderr)
    print(f"Checked {checked} committed v3 golden encoder streams; the files are unchanged.", file=sys.stderr)


def differential_main():
    parser = argparse.ArgumentParser(
        description=(
            "Require identical Python/Java v3 acceptance and RGB digests for random trees, conformance streams "
            "and mutations. Only ValueError counts as a reference rejection. Exit 1 on disagreement or another "
            "reference exception; 2 on a failed Java process or invalid options."
        ),
    )
    parser.add_argument("classpath")
    parser.add_argument("--streams", type=int, default=DIFFERENTIAL_DEFAULT_STREAMS, help="random-tree archives")
    parser.add_argument(
        "--conformance", type=int, default=DIFFERENTIAL_DEFAULT_CONFORMANCE, help="committed archives, cycling when necessary"
    )
    parser.add_argument("--corpus", type=Path, default=DIFFERENTIAL_DEFAULT_CORPUS)
    parser.add_argument("--mutants", type=int, default=1, help="mutated copies of every archive")
    parser.add_argument("--seed", type=int, default=DIFFERENTIAL_DEFAULT_SEED)
    parser.add_argument(OUT_OPTION, type=Path, default=Path(DIFFERENTIAL_OUTPUT_PATH))
    parser.add_argument(JAVA_OPTION, default=JAVA_COMMAND)
    arguments = parser.parse_args()
    if (
        min(arguments.streams, arguments.conformance, arguments.mutants) < 0
        or arguments.streams + arguments.conformance == 0
    ):
        message: str = "Archive counts must be nonnegative, and at least one archive is required!"
        parser.error(message)
    sys.exit(differential_run(arguments))


def differential_run(arguments):
    archives = differential_build_archives(arguments)
    output = arguments.out
    (output / ARCHIVES_NAME).mkdir(parents=True, exist_ok=True)
    paths = {}
    for name, chunks in archives.items():
        paths[name] = output / ARCHIVES_NAME / f"{name}.mcs"
        paths[name].write_bytes(archive_bytes(chunks))
    expected = {name: differential_reference_tokens(chunks) for name, chunks in archives.items()}
    java = subprocess.run(
        [
            arguments.java,
            CLASSPATH_FLAG,
            arguments.classpath,
            MCV2_TOOLS_CLASS,
            DIGESTS_COMMAND,
        ]
        + [str(path) for path in paths.values()],
        capture_output=True,
        text=True,
    )
    if java.returncode:
        print(java.stderr, file=sys.stderr)
        return USAGE_EXIT_CODE

    actual = {}
    for line in java.stdout.splitlines():
        path, *tokens = line.split(WORD_SEPARATOR)
        name = Path(path).stem
        if name in actual:
            message: str = f"Java printed the output of {name} twice!"
            raise ValueError(message)
        actual[name] = tokens
    counts, disagreements = differential_compare(expected, actual)
    summary = dict(seed=arguments.seed, counts=counts, disagreements=disagreements)
    (output / SUMMARY_NAME).write_text(json.dumps(summary, indent=JSON_INDENT) + LINE_SEPARATOR)
    print(json.dumps(counts))

    return FAILURE_EXIT_CODE if disagreements else SUCCESS_EXIT_CODE


def differential_build_archives(arguments):
    randomizer = random.Random(arguments.seed)
    archives = {
        f"tree-{index:04d}": edge_streams_random_stream(randomizer) for index in range(arguments.streams)
    }
    corpus = sorted(arguments.corpus.glob(STREAM_GLOB))
    if arguments.conformance and (not corpus):
        message: str = f"Corpus {arguments.corpus} has no committed conformance archives!"
        raise ValueError(message)

    for index in range(arguments.conformance):
        path = corpus[index % len(corpus)]
        chunks = list(read_archive(path))
        if not chunks:
            message: str = f"Conformance archive {path} is empty!"
            raise ValueError(message)
        archives[f"conformance-{index:04d}-{path.stem}"] = chunks
    for name, chunks in list(archives.items()):
        for index in range(arguments.mutants):
            archives[f"{name}-mutant-{index}"] = differential_mutant(chunks, randomizer)

    return archives


def differential_mutant(original, randomizer):
    mutated = [bytearray(frame) for frame in original]
    nonempty = [index for index, frame in enumerate(mutated) if frame]
    if not nonempty:
        message: str = "A mutation needs a nonempty frame!"
        raise ValueError(message)

    if randomizer.random() < DIFFERENTIAL_MUTATION_CHANCE:
        victim = randomizer.choice(nonempty)
        mutated[victim] = mutated[victim][: randomizer.randrange(len(mutated[victim]))]
    else:
        for _ in range(randomizer.randint(1, DIFFERENTIAL_MAXIMUM_FLIPS)):
            victim = mutated[randomizer.choice(nonempty)]
            limit = min(len(victim), DIFFERENTIAL_HEAD_BYTES) if randomizer.random() < DIFFERENTIAL_HEAD_CHANCE else len(victim)
            victim[randomizer.randrange(limit)] ^= randomizer.randint(1, BYTE_VALUES - 1)
    if mutated == original:
        mutated[nonempty[0]][0] ^= 1

    return [bytes(frame) for frame in mutated]


def differential_reference_tokens(chunks):
    """Only ValueError is a rejection; unexpected reference exceptions must fail the comparison."""
    decoder, tokens = (Decoder(), [])
    for frame in chunks:
        try:
            tokens.append(hashlib.sha256(decoder.accept(frame).tobytes()).hexdigest())
        except ValueError:
            tokens.append(REJECT_TOKEN)

    return tokens


def differential_compare(expected, actual):
    counts = dict(archives=len(expected), frames=0, decoded=0, refused=0, disagreements=0)
    disagreements = []
    for name, expected_tokens in expected.items():
        java = actual.get(name, [])
        if len(java) != len(expected_tokens):
            disagreements.append(
                dict(archive=name, reason="frame count", mcav=len(java), reference=len(expected_tokens))
            )
        disagreements.extend(differential_compare_frames(name, expected_tokens, java, counts))
    for name in actual.keys() - expected.keys():
        disagreements.append(dict(archive=name, reason="unexpected archive from Java"))
    counts["disagreements"] = len(disagreements)
    return (counts, disagreements)


def differential_compare_frames(name, expected_tokens, java, counts):
    disagreements = []
    for index, token in enumerate(expected_tokens):
        counts["frames"] += 1
        other = java[index] if index < len(java) else None
        if other != token:
            disagreements.append(dict(archive=name, frame=index, mcav=other, reference=token))
        else:
            counts["refused" if token == REJECT_TOKEN else "decoded"] += 1

    return disagreements


def rate_quality_main():
    parser = argparse.ArgumentParser(
        description=(
            "Run the Java benchmark at each lambda on a raw RGB24 source, score its decoded pictures with "
            "libvmaf, and write rates, PSNR, timings and VMAF mean/minimum as a JSON curve. Arguments after -- "
            "pass through to the encoder. Exit 1 on benchmark/scoring failure; 2 on invalid options."
        ),
    )
    parser.add_argument("--classpath", required=True)
    parser.add_argument("--main", default=MCV2_TOOLS_CLASS)
    parser.add_argument(SOURCE_OPTION, required=True)
    parser.add_argument(WIDTH_OPTION, type=int, default=DEFAULT_WIDTH)
    parser.add_argument(HEIGHT_OPTION, type=int, default=DEFAULT_HEIGHT)
    parser.add_argument(FRAMES_OPTION, type=int, default=RATE_QUALITY_DEFAULT_FRAMES)
    parser.add_argument(FRAMES_PER_SECOND_OPTION, dest="frames_per_second", type=float, default=RATE_QUALITY_DEFAULT_FRAMES_PER_SECOND)
    parser.add_argument("--lambdas", required=True)
    parser.add_argument(FFMPEG_OPTION, required=True)
    parser.add_argument(JAVA_OPTION, default=JAVA_COMMAND)
    parser.add_argument(OUT_OPTION, required=True)
    parser.add_argument("encoder", nargs="*")
    arguments = parser.parse_args()
    points = []
    with tempfile.TemporaryDirectory() as folder:
        decoded = os.path.join(folder, DECODED_PICTURES_NAME)
        for value in arguments.lambdas.split(LIST_SEPARATOR):
            command = [
                arguments.java,
                NATIVE_ACCESS_FLAG,
                JAVA_HEAP_FLAG,
                CLASSPATH_FLAG,
                arguments.classpath,
                arguments.main,
                *([BENCH_COMMAND] if arguments.main.endswith("Mcv2Tools") else []),
                f"source={arguments.source}",
                f"width={arguments.width}",
                f"height={arguments.height}",
                f"frames={arguments.frames}",
                f"fps={arguments.frames_per_second}",
                f"lambda={value}",
                f"decoded={decoded}",
                *arguments.encoder,
            ]
            result = subprocess.run(command, check=True, capture_output=True, text=True)
            point = json.loads(result.stdout.strip().splitlines()[-1])
            point["lambda"] = float(value)
            point["encoder"] = WORD_SEPARATOR.join(arguments.encoder)
            point[VMAF_MEAN_KEY], point[VMAF_MINIMUM_KEY] = rate_quality_vmaf(
                arguments.ffmpeg,
                arguments.source,
                decoded,
                arguments.width,
                arguments.height,
                arguments.frames,
                arguments.frames_per_second,
            )
            points.append(point)
            print(json.dumps(point), file=sys.stderr)
    with open(arguments.out, "w") as out:
        json.dump(points, out, indent=JSON_INDENT)
        out.write(LINE_SEPARATOR)


def rate_quality_vmaf(ffmpeg, source, decoded, width, height, frames, frame_rate):
    with tempfile.TemporaryDirectory() as folder:
        log = os.path.join(folder, VMAF_LOG_NAME)
        graph = vmaf_filter(log, os.cpu_count())
        subprocess.run(
            [
                ffmpeg,
                NO_STANDARD_INPUT_FLAG,
                VERBOSITY_FLAG,
                ERROR_LOG_LEVEL,
                OVERWRITE_FLAG,
                *codec_curves_raw(source, width, height, frame_rate),
                *codec_curves_raw(decoded, width, height, frame_rate),
                FRAME_COUNT_FLAG,
                str(frames),
                FILTER_GRAPH_FLAG,
                graph,
                FORMAT_FLAG,
                NULL_FORMAT,
                STANDARD_STREAM,
            ],
            check=True,
        )
        scores = [frame[VMAF_METRICS_KEY][VMAF_SCORE_KEY] for frame in json.loads(Path(log).read_text())[VMAF_FRAMES_KEY]]
        return (sum(scores) / len(scores), min(scores))


def shader_check_main():
    parser = argparse.ArgumentParser(
        description=(
            "Run the pack post chain outside Minecraft and require byte-exact reference pictures and decode "
            "decisions. --drop exercises missing frames; --restart-check exercises keyframe restart ordering. "
            "Frames with more pages than slots cannot be decoded, as Mcv2Channel.send refuses them. Exit 1 on a "
            "failed check; 2 on invalid options."
        ),
    )
    parser.add_argument("streams", nargs="+")
    parser.add_argument(SLOTS_OPTION, type=int, default=DEFAULT_SLOTS)
    parser.add_argument("--drop", type=int, default=0)
    parser.add_argument(BACKEND_OPTION, choices=(EGL_BACKEND, GLX_BACKEND), default=None)
    parser.add_argument(PACK_OPTION, type=Path, help="another pack source folder with its sibling chain.json")
    parser.add_argument(SPIRV_OPTION, metavar=SPIRV_METAVAR, help=SPIRV_HELP)
    parser.add_argument(
        "--second-screen", action="store_true", help="play on the second screen of a two-screen pack"
    )
    parser.add_argument(
        "--restart-check", action="store_true", help="also check keyframe restart and P-frame ordering"
    )
    arguments = parser.parse_args()
    if arguments.second_screen:
        global shader_check_screen_index, shader_check_first_slot
        shader_check_screen_index, shader_check_first_slot = (1, SHADER_CHECK_IDLE_SLOTS)
    if arguments.pack:
        global shader_check_pack
        shader_check_pack = arguments.pack
    import moderngl

    context = moderngl.create_standalone_context(
        require=GL_VERSION, **{"backend": EGL_BACKEND} if arguments.backend == EGL_BACKEND else {}
    )
    print(context.info[GL_RENDERER_KEY])
    failures = 0
    for stream in arguments.streams:
        chain = None
        last_id = None
        shown = None
        decoded = skipped = wrong = 0
        for index, frame in enumerate(read_archive(stream)):
            width, height = struct.unpack_from(DIMENSIONS_FORMAT, frame, WIDTH_OFFSET)
            frame_id, reference_id = struct.unpack_from(FRAME_IDS_FORMAT, frame, FRAME_ID_OFFSET)
            keyframe = frame_id == reference_id
            if chain is None:
                chain = ShaderChain(context, width, height, arguments.slots)
                if arguments.spirv:
                    chain.compiled = shader_check_compile_via_spirv(chain.includes, arguments.spirv)
            if arguments.drop and index % arguments.drop == arguments.drop - 1:
                continue
            pages = make_pages(frame, SHADER_CHECK_STREAM_ID, SYMBOL_BITS)
            # Mcv2Channel.send refuses a frame with more pages than slots, so the client never receives it.
            delta = None if last_id is None else (frame_id - last_id) & ID_MASK
            newer = delta is None or (delta > 0 and (keyframe or delta < ID_HALF_RANGE))
            decodable = (
                newer
                and (keyframe or reference_id == last_id)
                and ((width, height) == (chain.width, chain.height))
                and (len(pages) <= arguments.slots)
            )
            chain.show(pages[: arguments.slots])
            chain_decoded, picture = chain.frame()
            if chain_decoded != decodable:
                wrong += 1
                print(
                    "  frame %d: the chain %s it, the client model %s"
                    % (index, "decoded" if chain_decoded else "skipped", "can" if decodable else "cannot")
                )
                continue
            if chain_decoded:
                decoded += 1
                expected = decode(frame, None if keyframe else shown, last_id)
                shown = expected
                last_id = frame_id
                if not numpy.array_equal(picture, expected):
                    wrong += 1
                    difference = numpy.abs(picture.astype(int) - expected.astype(int))
                    print(
                        "  frame %d differs: %d pixels, max %d"
                        % (index, int((difference.max(axis=2) > 0).sum()), difference.max())
                    )
            else:
                skipped += 1
                if shown is not None and (not numpy.array_equal(picture, shown)):
                    wrong += 1
                    print("  frame %d was skipped but the picture changed" % index)
        print("%s: %d frames decoded, %d skipped, %d wrong" % (Path(stream).name, decoded, skipped, wrong))
        failures += wrong
    if arguments.restart_check:
        failures += shader_check_check_restart(context, arguments.slots, arguments.spirv)
    sys.exit(FAILURE_EXIT_CODE if failures else SUCCESS_EXIT_CODE)


class ShaderChain:
    def __init__(self, context, width, height, slots):
        self.context = context
        self.width, self.height, self.slots = (width, height, slots)
        self.includes = shader_check_generated(width, height, slots)
        self.programs = {}
        self.framebuffers = {}
        self.arrays = {}
        chain = shader_check_post_chain(width, height, slots)
        self.passes = chain[PASSES_KEY]
        make = lambda width, height: context.texture((width, height), RGBA_CHANNELS, dtype="f1")
        self.main = make(*shader_check_screen)
        self.depth = context.depth_texture(shader_check_screen)
        self.depth.compare_func = ""
        framebuffer = context.framebuffer(depth_attachment=self.depth)
        framebuffer.clear(depth=0.0)
        framebuffer.release()
        self.targets = {"minecraft:main": self.main, "minecraft:entity_outline": make(*shader_check_screen)}
        self.persistent = []
        for name, definition in chain[TARGETS_KEY].items():
            self.targets[name] = make(
                definition.get("width", shader_check_screen[0]), definition.get("height", shader_check_screen[1])
            )
            if definition.get("persistent"):
                self.persistent.append(name)
        for texture in self.targets.values():
            texture.filter = (context.NEAREST, context.NEAREST)
            texture.write(bytes(texture.width * texture.height * RGBA_CHANNELS))
        self.blit = context.program(vertex_shader=SHADER_CHECK_VERTEX, fragment_shader=SHADER_CHECK_BLIT)
        self.compiled = None

    def target(self, name):
        target_name = (
            name if RESOURCE_NAMESPACE_SEPARATOR in name else SHADER_CHECK_TARGET_PATTERN % (name, shader_check_screen_index)
        )
        return self.targets[target_name]

    def program(self, name, vertex=SCREENQUAD_PROGRAM):
        key = (name, vertex)
        if key not in self.programs:
            if self.compiled is not None:
                source = (self.compiled / (name + FRAGMENT_SHADER_SUFFIX)).read_text()
            else:
                source = shader_check_desktop(
                    shader_check_resolve(
                        (shader_check_pack / SHADER_POST_PATH / (name + FRAGMENT_SHADER_SUFFIX)).read_text(),
                        self.includes,
                    )
                )
            if vertex == SCREENQUAD_PROGRAM:
                vertex_source = SHADER_CHECK_VERTEX
            elif self.compiled is not None:
                vertex_source = (self.compiled / (vertex.split(RESOURCE_PATH_SEPARATOR)[-1] + VERTEX_SHADER_SUFFIX)).read_text()
            else:
                vertex_source = shader_check_desktop(
                    shader_check_resolve(
                        (
                            shader_check_pack / SHADER_POST_PATH / (vertex.split(RESOURCE_PATH_SEPARATOR)[-1] + VERTEX_SHADER_SUFFIX)
                        ).read_text(),
                        self.includes,
                    )
                )
            self.programs[key] = self.context.program(vertex_shader=vertex_source, fragment_shader=source)

        return self.programs[key]

    def draw(self, program, inputs, output):
        for unit, (sampler, texture) in enumerate(inputs.items()):
            texture.use(unit)
            if sampler + SAMPLER_SUFFIX in program:
                program[sampler + SAMPLER_SUFFIX].value = unit
        if output not in self.framebuffers:
            self.framebuffers[output] = self.context.framebuffer(color_attachments=[self.targets[output]])
        self.framebuffers[output].use()
        if id(program) not in self.arrays:
            self.arrays[id(program)] = self.context.vertex_array(program, [])
        self.arrays[id(program)].render(mode=self.context.TRIANGLES, vertices=TRIANGLE_VERTICES)

    def steps(self):
        """The passes this harness runs, in order: (name, program, inputs, output). Minecraft's own outline passes
        (sobel, box blurs) only touch its outline target and are left out."""
        steps = []
        for step in self.passes:
            shader = step["fragment_shader"]
            inputs = {}
            for entry in step.get(INPUTS_KEY, []):
                texture = self.depth if entry.get("use_depth_buffer") else self.targets[entry[TARGET_KEY]]
                inputs[entry["sampler_name"]] = texture
            if shader == "minecraft:post/blit":
                name = "blit %s -> %s" % (
                    step[INPUTS_KEY][0][TARGET_KEY].split(RESOURCE_NAMESPACE_SEPARATOR)[-1],
                    step[OUTPUT_KEY].split(RESOURCE_NAMESPACE_SEPARATOR)[-1],
                )
                steps.append((name, self.blit, inputs, step[OUTPUT_KEY]))
            elif shader.startswith("mcav:post/"):
                program = self.program(
                    shader.split(RESOURCE_PATH_SEPARATOR)[-1], step.get("vertex_shader", SCREENQUAD_PROGRAM)
                )
                steps.append((shader.split(RESOURCE_PATH_SEPARATOR)[-1], program, inputs, step[OUTPUT_KEY]))

        return steps

    def reset(self):
        for name in self.persistent:
            texture = self.targets[name]
            texture.write(bytes(texture.width * texture.height * RGBA_CHANNELS))

    def show(self, pages):
        screen = numpy.zeros((shader_check_screen[1], shader_check_screen[0], RGBA_CHANNELS), numpy.uint8)
        rows = (PAGE_PIXELS + shader_check_screen[0] - 1) // shader_check_screen[0]
        for page in pages:
            symbols = numpy.zeros(PAGE_SYMBOLS, numpy.uint32)
            symbols[: len(page)] = numpy.frombuffer(page, numpy.uint8)
            bits = numpy.bitwise_or.reduce([symbols[index::SYMBOLS_PER_PIXEL] << shift for index, shift in enumerate(SYMBOL_SHIFTS)])
            packed = numpy.stack(
                [
                    bits & MAXIMUM_CHANNEL,
                    bits >> BITS_PER_BYTE & MAXIMUM_CHANNEL,
                    bits >> 2 * BITS_PER_BYTE,
                    numpy.full_like(bits, MAXIMUM_CHANNEL),
                ],
                axis=1,
            ).astype(numpy.uint8)
            strip = numpy.zeros((rows * shader_check_screen[0], RGBA_CHANNELS), numpy.uint8)
            strip[:PAGE_PIXELS] = packed
            slot = shader_check_first_slot + shader_check_page_number(page)
            for row in range(rows):
                screen[shader_check_screen[1] - 1 - (slot * rows + row)] = strip[
                    row * shader_check_screen[0] : (row + 1) * shader_check_screen[0]
                ]
        self.main.write(screen.tobytes())

    def frame(self):
        for _, program, inputs, output in self.steps():
            self.draw(program, inputs, output)
        status = numpy.frombuffer(self.target(STATUS_TARGET).read(), numpy.uint8)
        picture = numpy.frombuffer(self.target(PREVIOUS_TARGET).read(), numpy.uint8).reshape(
            self.height, self.width, RGBA_CHANNELS
        )[:, :, :CHANNELS]
        return (bool(status[0]), picture)


def shader_check_generated(width, height, slots):
    return {
        "mcav:mcv2_config.glsl": LINE_SEPARATOR.join(
            shader_check_screens_configuration(slots)
            + ["const bool MCV2_DEBUG_VIEW = false;", "const ivec3 MCV2_OUTLINE_COLOR = ivec3(0, 0, 0);", ""]
        ),
        "mcav:mcv2_screen.glsl": LINE_SEPARATOR.join(
            [
                "const int MCV2_SCREEN_INDEX = %d;" % shader_check_screen_index,
                "const int MCV2_PAGE_SLOTS = %d;" % slots,
                "const int MCV2_FIRST_SLOT = %d;" % shader_check_first_slot,
                "const uint MCV2_STREAM_ID = %du;" % SHADER_CHECK_STREAM_ID,
                "const int MCV2_VIDEO_WIDTH = %d;" % width,
                "const int MCV2_VIDEO_HEIGHT = %d;" % height,
                "const int MCV2_BYTES_WIDTH = 128;",
                "const int MCV2_BYTES_HEIGHT = %d;"
                % shader_check_placeholders(width, height, slots)[BYTES_HEIGHT_PLACEHOLDER],
                "const int MCV2_CELLS_WIDTH = %d;" % shader_check_cells_width(width),
                "const int MCV2_CELLS_HEIGHT = %d;" % shader_check_cells_height(height),
                "const int MCV2_DEBUG_TOP = 0;",
                "",
            ]
        ),
    }


def shader_check_screens_configuration(slots):
    if shader_check_screen_index == 0:
        return [
            "const int MCV2_SCREENS = 1;",
            TOTAL_SLOTS_DECLARATION % slots,
            "const uint MCV2_SCREEN_STREAMS[1] = uint[1](%du);" % SHADER_CHECK_STREAM_ID,
            "const int MCV2_SCREEN_SLOTS[1] = int[1](%d);" % slots,
            "const int MCV2_SCREEN_FIRST_SLOTS[1] = int[1](0);",
        ]

    return [
        "const int MCV2_SCREENS = 2;",
        TOTAL_SLOTS_DECLARATION % (SHADER_CHECK_IDLE_SLOTS + slots),
        "const uint MCV2_SCREEN_STREAMS[2] = uint[2](1u, %du);" % SHADER_CHECK_STREAM_ID,
        "const int MCV2_SCREEN_SLOTS[2] = int[2](%d, %d);" % (SHADER_CHECK_IDLE_SLOTS, slots),
        "const int MCV2_SCREEN_FIRST_SLOTS[2] = int[2](0, %d);" % SHADER_CHECK_IDLE_SLOTS,
    ]


def shader_check_placeholders(width, height, slots):
    return {
        "VIDEO_WIDTH": width,
        "VIDEO_HEIGHT": height,
        "BYTES_WIDTH": SHADER_CHECK_BYTES_WIDTH,
        BYTES_HEIGHT_PLACEHOLDER: (slots * PAGE_CAPACITY // RGBA_CHANNELS + SHADER_CHECK_BYTES_WIDTH - 1) // SHADER_CHECK_BYTES_WIDTH,
        "PAGES_WIDTH": SHADER_CHECK_PAGE_TEXELS * slots,
        "CRC_WIDTH": SHADER_CHECK_CRC_TEXELS * slots,
        "CELLS_WIDTH": shader_check_cells_width(width),
        "CELLS_HEIGHT": shader_check_cells_height(height) + 1,
    }


def shader_check_cells_width(width):
    return (width + SHADER_CHECK_CELL_SIZE - 1) // SHADER_CHECK_CELL_SIZE


def shader_check_cells_height(height):
    return (height + SHADER_CHECK_CELL_SIZE - 1) // SHADER_CHECK_CELL_SIZE


def shader_check_post_chain(width, height, slots):
    template_path = shader_check_pack.parent / CHAIN_NAME
    text = template_path.read_text().replace("@S@", str(shader_check_screen_index))
    for name, value in shader_check_placeholders(width, height, slots).items():
        text = text.replace("@%s@" % name, str(value))
    template = json.loads(text)
    targets = dict(template["screen_targets"])
    targets.update(template[TARGETS_KEY])
    return {TARGETS_KEY: targets, PASSES_KEY: template["decode"] + template["draw"] + template["tail"]}


def shader_check_desktop(source):
    """The pack's GLSL for a desktop OpenGL 3.3 compiler: Minecraft 26.3 compiles it for Vulkan, whose vertex index is
    gl_VertexIndex."""
    return source.replace("gl_VertexIndex", "gl_VertexID")


def shader_check_resolve(source, includes, seen=None):
    seen = set() if seen is None else seen

    def include(match):
        name = match.group(1)
        if name in seen:
            return ""

        seen.add(name)
        if name in includes:
            text = includes[name]
        else:
            namespace, path = name.split(RESOURCE_NAMESPACE_SEPARATOR)
            text = (shader_check_pack / ASSETS_NAME / namespace / SHADER_INCLUDE_PATH / path).read_text()

        return shader_check_resolve(text, includes, seen)

    return re.sub("^#include <([^>]+)>", include, source, flags=re.M)


def shader_check_page_number(page):
    value = 0
    for bit in range(SHADER_CHECK_PAGE_NUMBER_BITS):
        symbol_bit = SHADER_CHECK_PAGE_NUMBER_FIRST_BIT + bit
        value |= (page[symbol_bit // SYMBOL_BITS] >> symbol_bit % SYMBOL_BITS & 1) << bit

    return value


def shader_check_compile_via_spirv(includes, classpath):
    work = Path(tempfile.mkdtemp(prefix=SPIRV_DIRECTORY_PREFIX))
    generated = work / "generated"
    generated.mkdir()
    for name, text in includes.items():
        (generated / name.split(RESOURCE_NAMESPACE_SEPARATOR)[1]).write_text(text)
    tool = MCV2_TOOLS_CLASS
    command = [
        JAVA_COMMAND,
        NATIVE_ACCESS_FLAG,
        CLASSPATH_FLAG,
        classpath,
        tool,
        SHADER_COMPILE_COMMAND,
        str(shader_check_pack),
        str(generated),
        str(work / COMPILED_SHADERS_NAME),
        POST_ONLY_FLAG,
    ]
    subprocess.run(command, check=True, stdout=subprocess.DEVNULL)

    return work / COMPILED_SHADERS_NAME


def shader_check_check_restart(context, slots, classpath=None):
    """Known pictures and decisions: a restarted keyframe can move backwards, P frames cannot."""

    chain = ShaderChain(context, SUPERBLOCK_SIZE, SUPERBLOCK_SIZE, slots)
    if classpath:
        chain.compiled = shader_check_compile_via_spirv(chain.includes, classpath)
    failures = committed = 0
    held_id = None
    for index, (frame_id, reference_id, color, should_decode, expected_color) in enumerate(SHADER_CHECK_RESTART_CASES):
        data = pack_frame(SUPERBLOCK_SIZE, SUPERBLOCK_SIZE, frame_id, reference_id, {0: Node(SOLID, record=bytes(color))})
        chain.show(make_pages(data, SHADER_CHECK_STREAM_ID, SYMBOL_BITS))
        chain_decoded, picture = chain.frame()
        if should_decode:
            held_id = frame_id
            committed += 1
        state = struct.unpack("<4I", chain.target("state").read())
        expected = numpy.full((SUPERBLOCK_SIZE, SUPERBLOCK_SIZE, CHANNELS), expected_color, numpy.uint8)
        if (
            chain_decoded != should_decode
            or not numpy.array_equal(picture, expected)
            or state[1] != held_id
            or (state[3] != committed)
        ):
            failures += 1
            print(
                "  restart case %d (id %d): decoded %s, expected %s; held id %d, expected %d; commits %d, expected %d"
                % (index, frame_id, chain_decoded, should_decode, state[1], held_id, state[3], committed)
            )
    print("keyframe restart: %d checks, %d wrong" % (len(SHADER_CHECK_RESTART_CASES), failures))

    return failures


def shader_timing_main():
    global shader_check_pack
    parser = argparse.ArgumentParser(
        description=(
            "Time GPU passes for new and repeated pages; the first round warms up and is excluded. Exit 1 if a "
            "frame does not decode on arrival, decodes again from the same pages, or gives different pixels in a "
            "later round; 2 on invalid options. --reference supplies an archived v2 mcvideo package."
        ),
    )
    parser.add_argument("streams", nargs="+")
    parser.add_argument(BACKEND_OPTION, choices=(EGL_BACKEND, GLX_BACKEND), default=None)
    parser.add_argument(SLOTS_OPTION, type=int, default=DEFAULT_SLOTS)
    parser.add_argument("--rounds", type=int, default=SHADER_TIMING_DEFAULT_ROUNDS)
    parser.add_argument("--repeats", type=int, default=SHADER_TIMING_DEFAULT_REPEATS)
    parser.add_argument(JSON_OPTION, type=Path)
    parser.add_argument(PACK_OPTION, type=Path, help="another pack source folder, with its sibling chain.json")
    parser.add_argument("--reference", type=Path, help="reference package root for an archived v2 baseline")
    parser.add_argument(SPIRV_OPTION, metavar=SPIRV_METAVAR, help=SPIRV_HELP)
    arguments = parser.parse_args()
    if arguments.rounds < SHADER_TIMING_MINIMUM_ROUNDS or arguments.repeats < 1 or arguments.slots < 1:
        message: str = "The run needs at least two rounds, one repeat and one page slot!"
        parser.error(message)
    if arguments.pack:
        shader_check_pack = arguments.pack
    import moderngl

    if arguments.reference:
        sys.path.insert(0, str(arguments.reference))
        from mcvideo.transport import make_pages as page_maker
    else:
        page_maker = make_pages
    options = {"backend": arguments.backend} if arguments.backend == EGL_BACKEND else {}
    context = moderngl.create_standalone_context(require=GL_VERSION, **options)
    renderer = context.info[GL_RENDERER_KEY]
    print(renderer, context.info[GL_VERSION_KEY])
    report = dict(
        renderer=renderer,
        version=context.info[GL_VERSION_KEY],
        rounds=arguments.rounds,
        repeats=arguments.repeats,
        streams={},
    )
    failures = 0
    for stream in arguments.streams:
        frames = list(read_archive(stream))
        width, height = struct.unpack_from(DIMENSIONS_FORMAT, frames[0], WIDTH_OFFSET)
        chain = TimedShaderChain(context, width, height, arguments.slots)
        pages = [page_maker(frame, SHADER_CHECK_STREAM_ID, SYMBOL_BITS) for frame in frames]
        keyframes = [
            frame[FRAME_ID_OFFSET:REFERENCE_ID_OFFSET] == frame[REFERENCE_ID_OFFSET:HEADER_BYTES]
            if frame[VERSION_OFFSET] == VERSION
            else bool(frame[VERSION_2_FLAGS_OFFSET] & 1)
            for frame in frames
        ]
        if arguments.spirv:
            chain.compiled = shader_check_compile_via_spirv(chain.includes, arguments.spirv)
        first_pictures = None
        new_keyframes, new_predicted_frames, idle = ([], [], [])
        mismatches = 0
        for round_index in range(arguments.rounds):
            chain.reset()
            pictures = []
            for index, page_list in enumerate(pages):
                chain.show(page_list)
                decoded, times = chain.timed_frame(arguments.repeats)
                names = tuple(times)
                times[TOTAL_TIMING_NAME] = sum(times[name] for name in names)
                chain.show(page_list)
                shown_again, again = chain.timed_frame(1)
                again["total"] = sum(again[name] for name in names)
                pictures.append(chain.target(PREVIOUS_TARGET).read())
                if not decoded or shown_again:
                    failures += 1
                    print("  frame %d: decoded %s, decoded again %s" % (index, decoded, shown_again))
                if round_index > 0:
                    (new_keyframes if keyframes[index] else new_predicted_frames).append(times)
                    idle.append(again)
            if first_pictures is None:
                first_pictures = pictures
            else:
                mismatches += sum(
                    1 for first_picture, picture in zip(first_pictures, pictures) if first_picture != picture
                )
        result = dict(
            width=width,
            height=height,
            frames=len(frames),
            mismatches=mismatches,
            new_keyframe=shader_timing_summarize(new_keyframes, names),
            new_p=shader_timing_summarize(new_predicted_frames, names),
            idle=shader_timing_summarize(idle, names),
        )
        report["streams"][Path(stream).name] = result
        failures += mismatches
        print(
            "%s (%dx%d, %d frames, %d rounds counted, %d picture mismatches between rounds)"
            % (Path(stream).name, width, height, len(frames), arguments.rounds - 1, mismatches)
        )
        print(
            "  %-24s %22s %22s %22s"
            % ("pass (ms)", "new P frame mean/p95", "new keyframe mean/p95", "no new video mean/p95")
        )
        for name in names + (TOTAL_TIMING_NAME,):
            cells = [result[kind][name] for kind in ("new_p", "new_keyframe", "idle")]
            print(
                "  %-24s %22s %22s %22s"
                % (name, *("%9.3f / %9.3f" % (cell["mean"], cell["p95"]) for cell in cells))
            )
    if arguments.json:
        arguments.json.write_text(json.dumps(report, indent=REPORT_INDENT))
    sys.exit(FAILURE_EXIT_CODE if failures else SUCCESS_EXIT_CODE)


class TimedShaderChain(ShaderChain):
    def __init__(self, context, width, height, slots):
        super().__init__(context, width, height, slots)
        self.queries = {}

    def show(self, pages):
        super().show(pages)
        rows = (PAGE_PIXELS + shader_check_screen[0] - 1) // shader_check_screen[0]
        row = (
            shader_check_screen[1]
            - 1
            - (shader_check_first_slot + self.slots) * rows
            - shader_check_screen_index
        )
        self.main.write(
            shader_timing_descriptor_row(shader_check_screen[0]).tobytes(),
            viewport=(0, row, shader_check_screen[0], 1),
        )

    def warm(self):
        """Keeps the GPU busy for a few milliseconds, untimed, as a client drawing its world would: after the harness's
        own uploads and read-backs the GPU would otherwise start the chain at an idle clock."""
        for _ in range(SHADER_TIMING_WARM_UP_FRAMES):
            self.draw(self.blit, {"In": self.main}, "mcav:mcv2_screen")

    def timed_frame(self, repeats):
        self.warm()
        times = {}
        for name, program, inputs, output in self.steps():
            if name.startswith("blit "):
                self.draw(program, inputs, output)
                continue
            if name == "mcv2_copy":
                name += " -> " + output.split(RESOURCE_NAMESPACE_SEPARATOR)[-1].removeprefix("mcv2_")
            if name not in self.queries:
                self.queries[name] = self.context.query(time=True)
            query = self.queries[name]
            count = repeats if name == "mcv2_decode" else 1
            with query:
                self.draw_repeatedly(count, program, inputs, output)
            times[name] = query.elapsed / count / NANOSECONDS_PER_MILLISECOND
        status = numpy.frombuffer(self.target(STATUS_TARGET).read(), numpy.uint8)
        return (bool(status[0]), times)

    def draw_repeatedly(self, count, program, inputs, output):
        for _ in range(count):
            self.draw(program, inputs, output)


def shader_timing_descriptor_row(width):
    floats = [*SHADER_TIMING_TOP_LEFT, SHADER_TIMING_CELLS[0], *SHADER_TIMING_RIGHT, SHADER_TIMING_CELLS[1], *SHADER_TIMING_DOWN, 0.0]
    perspective = shader_timing_perspective(
        SHADER_TIMING_FIELD_OF_VIEW, SHADER_TIMING_ASPECT_RATIO, SHADER_TIMING_NEAR, SHADER_TIMING_FAR
    )
    floats += list(perspective.reshape(-1))
    row = numpy.zeros((width, RGBA_CHANNELS), numpy.uint8)
    row[:, ALPHA_CHANNEL] = MAXIMUM_CHANNEL
    row[0, :CHANNELS] = tuple(STRIP_MAGIC)
    row[1, 0] = SHADER_TIMING_DESCRIPTOR_MARKER
    for index, value in enumerate(floats):
        packed = struct.pack(FLOAT_FORMAT, value)
        pixel = SHADER_TIMING_FIRST_FLOAT_PIXEL + index * SHADER_TIMING_PIXELS_PER_FLOAT
        row[pixel, :CHANNELS] = tuple(packed[:CHANNELS])
        row[pixel + 1, 0] = packed[CHANNELS]

    return row


def shader_timing_perspective(field_of_view_y, aspect, near, far):
    """An OpenGL projection matrix, column-major like GLSL's mat4 constructor order."""
    focal_length = 1.0 / numpy.tan(numpy.radians(field_of_view_y) / 2)
    matrix = numpy.zeros((4, 4), numpy.float32)
    matrix[0, 0] = focal_length / aspect
    matrix[1, 1] = focal_length
    matrix[2, 2] = (far + near) / (near - far)
    matrix[2, 3] = -1.0
    matrix[3, 2] = 2 * far * near / (near - far)
    return matrix


def shader_timing_summarize(samples, names):
    out = {}
    for name in names + (TOTAL_TIMING_NAME,):
        values = numpy.array([sample[name] for sample in samples]) if samples else numpy.zeros(1)
        out[name] = dict(mean=float(values.mean()), p95=float(numpy.percentile(values, TAIL_PERCENTILE)), n=len(samples))

    return out


def strip_check_main():
    parser = argparse.ArgumentParser(
        description=(
            "Validate six-bit transport pages, anchor descriptors, decision squares and monotonic decoded-frame "
            "counters in debug-view PNG screenshots. Exit 1 if any check fails; 2 if no PNG screenshots exist or "
            "options are invalid."
        ),
    )
    parser.add_argument("captures", type=Path)
    parser.add_argument(SLOTS_OPTION, type=int, required=True)
    parser.add_argument("--video-width", type=int, required=True)
    parser.add_argument(
        "--screens", type=int, default=1, help="number of screens sharing the strip (default: 1)"
    )
    parser.add_argument(
        "--screen", type=int, default=0,
        help="zero-based screen index for its anchor descriptor (default: 0)",
    )
    parser.add_argument(
        "--first-slot", type=int, default=0, help="first global page slot for this screen (default: 0)"
    )
    parser.add_argument(
        "--total-slots", type=int, help="total page slots across all screens (defaults to --slots)"
    )
    parser.add_argument(
        "--debug-top", type=int, default=0,
        help="rows between the anchor descriptors and debug view (default: 0)",
    )
    arguments = parser.parse_args()
    from PIL import Image

    total_slots = arguments.total_slots if arguments.total_slots is not None else arguments.slots
    captures = sorted(arguments.captures.glob(SCREENSHOT_GLOB))
    if not captures:
        message: str = f"Folder {arguments.captures} holds no PNG screenshots!"
        parser.error(message)
    valid, invalid, frames = (Counter(), Counter(), set())
    descriptors, decisions, failures, counts = (0, Counter(), [], [])
    squared = 0
    for capture in captures:
        screen = numpy.asarray(Image.open(capture).convert("RGB"))
        width = screen.shape[1]
        rows = (PAGE_PIXELS + width - 1) // width
        pages = {}
        for slot in range(arguments.slots):
            try:
                page = strip_check_read_strip_page(
                    strip_check_page_symbols(screen, arguments.first_slot + slot, rows)
                )
                pages[slot] = True
                valid[slot] += 1
                frames.add((page.frame_id, page.number))
            except ValueError as exception:
                pages[slot] = False
                invalid[slot, str(exception)] += 1
        descriptor_row = total_slots * rows + arguments.screen
        if [
            tuple(int(channel) for channel in screen[descriptor_row, column]) for column in range(len(STRIP_CHECK_DESCRIPTOR))
        ] == STRIP_CHECK_DESCRIPTOR:
            descriptors += 1
        else:
            failures.append((capture.name, "descriptor"))
        top = total_slots * rows + arguments.screens + arguments.debug_top
        if arguments.video_width + STRIP_CHECK_MARGIN + (arguments.slots + STRIP_CHECK_EXTRA_SQUARES) * STRIP_CHECK_STEP > width:
            continue
        squared += 1
        colours = []
        for square in range(arguments.slots + STRIP_CHECK_EXTRA_SQUARES):
            left = arguments.video_width + STRIP_CHECK_MARGIN + square * STRIP_CHECK_STEP
            region = screen[top : top + STRIP_CHECK_SQUARE, left : left + STRIP_CHECK_SQUARE].reshape(-1, CHANNELS)
            if numpy.any(region != region[0]):
                failures.append((capture.name, "square %d is not one colour" % square))
            colours.append(tuple(int(channel) for channel in region[0]))
        for slot in range(arguments.slots):
            if colours[slot] != (STRIP_CHECK_GREEN if pages[slot] else STRIP_CHECK_RED):
                failures.append(
                    (capture.name, "slot %d square %s, page valid %s" % (slot, colours[slot], pages[slot]))
                )
        decision = {
            STRIP_CHECK_GREEN: "decoded",
            STRIP_CHECK_BLUE: "nothing new",
            STRIP_CHECK_RED: "cannot decode",
        }.get(colours[arguments.slots], "other")
        decisions[decision] += 1
        if decision in ("cannot decode", "other"):
            failures.append((capture.name, "decision %s" % (colours[arguments.slots],)))
        count = 0
        for index, colour in enumerate(colours[arguments.slots + 1 :]):
            if len(set(colour)) != 1:
                failures.append((capture.name, "counter byte %d not grey: %s" % (index, colour)))
            count |= colour[0] << BITS_PER_BYTE * index
        if counts and count < counts[-1]:
            failures.append((capture.name, "counter went down from %d to %d" % (counts[-1], count)))
        counts.append(count)
    summary = {
        "captures": len(captures),
        "valid_pages_per_slot": {slot: valid[slot] for slot in sorted(valid)},
        "slots_without_a_page": {"%d: %s" % key: value for key, value in sorted(invalid.items())},
        "distinct_pages": len(frames),
        "descriptor_rows": descriptors,
        "captures_with_squares": squared,
        "decisions": dict(decisions),
        "counter": [counts[0], counts[-1]] if counts else None,
        "failures": len(failures),
    }
    for failure in failures[:STRIP_CHECK_PRINTED_FAILURES]:
        print("%s: %s" % failure)
    print(json.dumps(summary))
    sys.exit(FAILURE_EXIT_CODE if failures else SUCCESS_EXIT_CODE)


def strip_check_read_strip_page(symbols):
    if len(symbols) < STRIP_CHECK_HEADER_SYMBOLS:
        message: str = "The strip page header is truncated!"
        raise ValueError(message)

    bits = (
        (symbols[:STRIP_CHECK_HEADER_SYMBOLS, None] >> numpy.arange(SYMBOL_BITS) & 1)
        .astype(numpy.uint8)
        .ravel()
    )
    fields = PAGE_HEADER.unpack(numpy.packbits(bits[: PAGE_HEADER.size * BITS_PER_BYTE], bitorder=BIT_ORDER).tobytes())
    number, total = (fields[PAGE_NUMBER_FIELD], fields[PAGE_TOTAL_FIELD])
    size = min(page_capacity(), max(total - number * page_capacity(), 0))
    length = ((PAGE_HEADER.size + size) * BITS_PER_BYTE + SYMBOL_BITS - 1) // SYMBOL_BITS
    return read_page(symbols[:length].tobytes())


def strip_check_page_symbols(screen, slot, rows):
    pixels = (
        screen[slot * rows : (slot + 1) * rows].reshape(-1, CHANNELS)[:PAGE_PIXELS].astype(numpy.uint32)
    )
    bits = pixels[:, 0] | pixels[:, 1] << BITS_PER_BYTE | pixels[:, 2] << 2 * BITS_PER_BYTE
    return numpy.stack([bits >> shift & SYMBOL_MASK for shift in SYMBOL_SHIFTS], axis=1).astype(numpy.uint8).ravel()


def strip_fit_check_main():
    parser = argparse.ArgumentParser(
        description=(
            "Require the pack to preserve the scene when the transport strip cannot fit, and to cover a fitting "
            "strip with the scene row below it, in the first screen's pass only. Exit 1 if any check fails; "
            "2 on invalid options."
        ),
    )
    parser.add_argument(BACKEND_OPTION, choices=(EGL_BACKEND, GLX_BACKEND), default=None)
    parser.add_argument(SPIRV_OPTION, metavar=SPIRV_METAVAR, help=SPIRV_HELP)
    arguments = parser.parse_args()
    import moderngl

    context = moderngl.create_standalone_context(
        require=GL_VERSION, **{"backend": EGL_BACKEND} if arguments.backend == EGL_BACKEND else {}
    )
    print(context.info[GL_RENDERER_KEY])
    failures = []
    for screen in STRIP_FIT_CHECK_UNFIT_SCREENS:
        assert strip_fit_check_strip_rows(screen[0]) >= screen[1], screen
        scene, after, decoded = strip_fit_check_frame(context, screen, arguments.spirv)
        changed = int(numpy.count_nonzero(numpy.any(after != scene, axis=2)))
        if changed:
            failures.append(
                "%dx%d, where the strip does not fit: %d pixels of the scene changed" % (screen + (changed,))
            )
        if decoded:
            failures.append("%dx%d, where the strip does not fit: a frame was decoded" % screen)
    screen = STRIP_FIT_CHECK_FIT_SCREEN
    rows = strip_fit_check_strip_rows(screen[0])
    scene, after, _ = strip_fit_check_frame(context, screen, arguments.spirv)
    below = screen[1] - 1 - rows
    if not numpy.array_equal(after[: below + 1], scene[: below + 1]):
        failures.append("%dx%d: the scene below the strip changed" % screen)
    if not numpy.array_equal(
        after[below + 1 :], numpy.broadcast_to(scene[below], (rows,) + scene[below].shape)
    ):
        failures.append("%dx%d: the strip is not covered with the scene row below it" % screen)
    # Later screens' passes read what earlier screens drew, so covering the strip again smears their pictures.
    scene, after, _ = strip_fit_check_frame(context, screen, arguments.spirv, screen_index=1)
    changed = int(numpy.count_nonzero(numpy.any(after != scene, axis=2)))
    if changed:
        failures.append(
            "%dx%d, second screen: the screen pass changed %d pixels the first screen's pass drew" % (screen + (changed,))
        )
    print(json.dumps({"checks": STRIP_FIT_CHECK_TOTAL_CHECKS, "failures": failures}))

    return FAILURE_EXIT_CODE if failures else SUCCESS_EXIT_CODE


def strip_fit_check_strip_rows(width):
    return STRIP_FIT_CHECK_SLOTS * ((PAGE_PIXELS + width - 1) // width) + 1


def strip_fit_check_frame(context, screen, spirv=None, screen_index=0):
    """Use the same random scene for every size, so strip coverage can be compared exactly."""
    global shader_check_screen, shader_check_screen_index, shader_check_first_slot
    shader_check_screen = screen
    shader_check_screen_index = screen_index
    shader_check_first_slot = SHADER_CHECK_IDLE_SLOTS if screen_index else 0
    chain = ShaderChain(context, STRIP_FIT_CHECK_VIDEO[0], STRIP_FIT_CHECK_VIDEO[1], STRIP_FIT_CHECK_SLOTS)
    if spirv:
        chain.compiled = shader_check_compile_via_spirv(chain.includes, spirv)
    scene = numpy.random.default_rng(STRIP_FIT_CHECK_SEED).integers(
        0, BYTE_VALUES, (screen[1], screen[0], RGBA_CHANNELS), dtype=numpy.uint8
    )
    scene[..., ALPHA_CHANNEL] = MAXIMUM_CHANNEL
    chain.main.write(scene.tobytes())
    decoded, _ = chain.frame()
    after = numpy.frombuffer(chain.main.read(), numpy.uint8).reshape(screen[1], screen[0], RGBA_CHANNELS)
    return (scene, after, decoded)


def latency_main():
    parser = argparse.ArgumentParser(
        description=(
            "Match server JFR events to timestamped counter-video captures on the same clock. --stream N uses "
            "send events and frame ids modulo the archived stream length instead of encoder events. Exit 1 on "
            "input/analysis failure; 2 on invalid options."
        ),
    )
    parser.add_argument("recording")
    parser.add_argument("capture")
    parser.add_argument(JSON_OPTION, type=Path)
    parser.add_argument("--jfr", default=JFR_COMMAND)
    parser.add_argument(
        "--stream", type=int, default=0, help="the frames of a pre-encoded stream played by /mcav mcv2 stream"
    )
    arguments = parser.parse_args()
    frames = (
        latency_sends(arguments.recording, arguments.jfr, arguments.stream)
        if arguments.stream
        else latency_events(arguments.recording, arguments.jfr)
    )
    shots = latency_captures(arguments.capture)
    numbered = [event for event in frames if event["number"] is not None and math.isfinite(event["arrived"])]
    by_number = {}
    for event in numbered:
        by_number.setdefault(event["number"], []).append(event)
    first_seen = {}
    ambiguous = set()
    previous = None
    for timestamp, number in shots:
        if number is not None and number != previous:
            candidates = [
                event
                for event in by_number.get(number, [])
                if event["sent_to"] > 0
                and math.isfinite(event["sent"])
                and (event["arrived"] <= event["sent"] <= timestamp)
            ]
            if candidates:
                latest = max(candidates, key=lambda event: event["sent"])
                occurrence = (number, latest["arrived"])
                first_seen[occurrence] = timestamp
                if len(candidates) > 1:
                    ambiguous.add(occurrence)
        previous = number if number is not None else previous
    latencies = [timestamp - arrival for (number, arrival), timestamp in first_seen.items()]
    arrived = {event["number"]: event for event in numbered}
    server = [event["sent"] - event["arrived"] for event in frames]
    duration = (shots[-1][0] - shots[0][0]) / MILLISECONDS_PER_SECOND if len(shots) > 1 else math.nan
    source = max(arrived) - min(arrived) + 1 if arrived else 0
    report = dict(
        source_frames=source,
        encoded=len(frames),
        keyframes=sum(1 for event in frames if event["keyframe"]),
        sent_to_viewers=sum(event["sent_to"] for event in frames),
        held_back_for_backlog=sum(event["behind"] for event in frames),
        held_back_for_reference=sum(event["waiting"] for event in frames),
        not_sent_too_large=sum(1 for event in frames if event["colors"] < 0),
        backlog_max=max((event["backlog"] for event in frames), default=0),
        backlog_p95=latency_percentile([event["backlog"] for event in frames], TAIL_PERCENTILE),
        captured=len(shots),
        capture_seconds=duration,
        displayed=len(first_seen),
        ambiguous_displayed=len(ambiguous),
        displayed_fps=len(first_seen) / duration if duration == duration and duration > 0 else math.nan,
        server_ms_mean=float(numpy.mean(server)) if server else math.nan,
        server_ms_p95=latency_percentile(server, TAIL_PERCENTILE),
        glass_to_glass_ms_mean=float(numpy.mean(latencies)) if latencies else math.nan,
        glass_to_glass_ms_p50=latency_percentile(latencies, MEDIAN_PERCENTILE),
        glass_to_glass_ms_p95=latency_percentile(latencies, TAIL_PERCENTILE),
        glass_to_glass_ms_max=max(latencies, default=math.nan),
    )
    for key, value in report.items():
        print("%-26s %s" % (key, round(value, LATENCY_PRINTED_DIGITS) if isinstance(value, float) else value))
    if arguments.json:
        arguments.json.write_text(json.dumps(report, indent=REPORT_INDENT))


def latency_sends(path, jfr, span):
    output = subprocess.run(
        [jfr, JFR_PRINT_COMMAND, JFR_JSON_FLAG, JFR_EVENTS_FLAG, SEND_EVENT, str(path)],
        check=True,
        capture_output=True,
        text=True,
    ).stdout
    found = []
    for event in json.loads(output)[JFR_RECORDING_KEY][JFR_EVENTS_KEY]:
        values = event[JFR_VALUES_KEY]
        sent = latency_milliseconds(values[SENT_FIELD]) if values[COLORS_FIELD] >= 0 else math.nan
        found.append(
            dict(
                frame=values[FRAME_ID_FIELD],
                keyframe=values[KEYFRAME_FIELD],
                bytes=values[BYTES_FIELD],
                colors=values[COLORS_FIELD],
                arrived=sent,
                sent=sent,
                sent_to=values[SENT_TO_FIELD],
                behind=values[BEHIND_FIELD],
                waiting=values[WAITING_FIELD],
                backlog=values[BACKLOG_FIELD],
                number=values[FRAME_ID_FIELD] % span,
            )
        )

    return sorted(found, key=lambda event: event["frame"])


def latency_milliseconds(value):
    if isinstance(value, (int, float)):
        return float(value)

    return datetime.fromisoformat(value.replace("Z", "+00:00")).timestamp() * MILLISECONDS_PER_SECOND


def latency_events(path, jfr):
    output = subprocess.run(
        [jfr, JFR_PRINT_COMMAND, JFR_JSON_FLAG, JFR_EVENTS_FLAG, FRAME_EVENT, str(path)],
        check=True,
        capture_output=True,
        text=True,
    ).stdout
    found = []
    for event in json.loads(output)[JFR_RECORDING_KEY][JFR_EVENTS_KEY]:
        values = event[JFR_VALUES_KEY]
        fingerprint = values.get(FINGERPRINT_FIELD) or ""
        luma = list(bytes.fromhex(fingerprint))
        found.append(
            dict(
                frame=values[FRAME_ID_FIELD],
                keyframe=values[KEYFRAME_FIELD],
                bytes=values[BYTES_FIELD],
                colors=values[COLORS_FIELD],
                arrived=latency_milliseconds(values[ARRIVED_FIELD]),
                sent=latency_milliseconds(values[SENT_FIELD]),
                sent_to=values[SENT_TO_FIELD],
                behind=values[BEHIND_FIELD],
                waiting=values[WAITING_FIELD],
                backlog=values[BACKLOG_FIELD],
                number=counter_video_read(luma),
            )
        )

    return sorted(found, key=lambda event: event["frame"])


def counter_video_read(luma):
    bits = [1 if value >= COUNTER_VIDEO_THRESHOLD else 0 for value in luma]
    if (
        len(bits) < COUNTER_VIDEO_BITS + len(COUNTER_VIDEO_SYNCHRONIZATION_BITS)
        or tuple(bits[COUNTER_VIDEO_BITS : COUNTER_VIDEO_BITS + len(COUNTER_VIDEO_SYNCHRONIZATION_BITS)])
        != COUNTER_VIDEO_SYNCHRONIZATION_BITS
    ):
        return None

    return sum(bit << position for position, bit in enumerate(bits[:COUNTER_VIDEO_BITS]))


def latency_captures(path):
    """(wall-clock ms, number or None) for every captured frame, decoded one frame at a time: a 10-minute capture is
    2.6 GB of pixels."""
    probe = subprocess.run(
        [
            FFPROBE_COMMAND,
            VERBOSITY_FLAG,
            ERROR_LOG_LEVEL,
            SELECT_STREAMS_FLAG,
            FIRST_VIDEO_STREAM,
            SHOW_ENTRIES_FLAG,
            LATENCY_PROBE_ENTRIES,
            OUTPUT_FORMAT_FLAG,
            JSON_FORMAT,
            str(path),
        ],
        check=True,
        capture_output=True,
        text=True,
    ).stdout
    metadata = json.loads(probe)
    width, height = (metadata[FFPROBE_STREAMS_KEY][0]["width"], metadata[FFPROBE_STREAMS_KEY][0]["height"])
    times = [float(frame["pts_time"]) * MILLISECONDS_PER_SECOND for frame in metadata["frames"]]
    row = min(LATENCY_SAMPLE_OFFSET, height - 1)
    columns = [
        LATENCY_SAMPLE_OFFSET + COUNTER_VIDEO_BLOCK * block
        for block in range(LATENCY_SAMPLES)
        if LATENCY_SAMPLE_OFFSET + COUNTER_VIDEO_BLOCK * block < width
    ]
    size = width * height * CHANNELS
    result = []
    with subprocess.Popen(
        [
            FFMPEG_COMMAND,
            NO_STANDARD_INPUT_FLAG,
            VERBOSITY_FLAG,
            ERROR_LOG_LEVEL,
            INPUT_FLAG,
            str(path),
            FORMAT_FLAG,
            RAW_VIDEO_FORMAT,
            PIXEL_FORMAT_FLAG,
            RGB_PIXEL_FORMAT,
            STANDARD_STREAM,
        ],
        stdout=subprocess.PIPE,
    ) as decoder:
        # Drain every frame so ffmpeg cannot block on a full pipe when timestamps run out.
        frames = enumerate(iter(lambda: decoder.stdout.read(size), b""))
        complete = ((index, raw) for index, raw in frames if index < len(times) and len(raw) >= size)
        for index, raw in complete:
            pixels = numpy.frombuffer(raw, numpy.uint8).reshape(height, width, CHANNELS)[row, columns].astype(int)
            luma = ((pixels[:, 0] + LATENCY_LUMA_GREEN_WEIGHT * pixels[:, 1] + pixels[:, 2]) // LATENCY_LUMA_WEIGHT_TOTAL).tolist()
            result.append((times[index], counter_video_read(luma)))
    if decoder.returncode != 0:
        raise subprocess.CalledProcessError(decoder.returncode, decoder.args)

    return result


def latency_percentile(values, percent):
    return float(numpy.percentile(values, percent)) if values else math.nan


def charts_main():
    parser = argparse.ArgumentParser(
        description=(
            "Render the codec and ablation charts from committed measurements; --tables also prints the article "
            "tables. Exact pixels require the documented matplotlib/font versions. Exit 1 on data/render failure; "
            "2 on invalid options."
        ),
    )
    parser.add_argument("--tables", action="store_true", help="print the tables of mcav-docs/mcv2.md")
    arguments = parser.parse_args()
    logging.getLogger(FONT_MANAGER_LOGGER).setLevel(logging.ERROR)
    charts_style()
    IMAGES.mkdir(parents=True, exist_ok=True)
    data = json.loads((CHARTS_DATA / CODEC_CURVES_DATA_NAME).read_text())
    charts_draw_codecs(data)
    ablation_file = CHARTS_DATA / ABLATION_DATA_NAME
    ablation = json.loads(ablation_file.read_text()) if ablation_file.exists() else {}
    rows = charts_ablation_rates(ablation)
    if rows:
        charts_draw_features(rows)
    if arguments.tables:
        charts_print_tables(data, rows, ablation.get("removed", []))


def charts_style():
    pyplot = figure_pyplot()
    pyplot.rcParams.update(
        {
            "font.family": CHARTS_FONT,
            "font.size": CHARTS_FONT_SIZE,
            "axes.edgecolor": CHARTS_SECONDARY,
            "axes.labelcolor": CHARTS_INK,
            "xtick.color": CHARTS_SECONDARY,
            "ytick.color": CHARTS_SECONDARY,
            "text.color": CHARTS_INK,
            "axes.facecolor": CHARTS_SURFACE,
            "figure.facecolor": CHARTS_SURFACE,
        }
    )


def figure_pyplot():
    import matplotlib

    matplotlib.use("Agg")
    import matplotlib.pyplot as pyplot

    return pyplot


def charts_draw_codecs(data):
    from matplotlib.ticker import FixedLocator, FuncFormatter, NullLocator

    pyplot = figure_pyplot()
    figure, axes = pyplot.subplots(len(CHARTS_SOURCES), 1, figsize=CHARTS_CODECS_FIGURE_SIZE, sharex=True)
    for axis, (source, title) in zip(axes, CHARTS_SOURCES):
        for codec, name, colour, marker in CHARTS_CODECS:
            rates, scores = zip(*charts_curve(data, source, codec))
            axis.plot(
                rates,
                scores,
                color=colour,
                marker=marker,
                markersize=CHARTS_MARKER_SIZE,
                linewidth=CHARTS_CURVE_LINE_WIDTH,
                label=name,
                markeredgecolor=CHARTS_SURFACE,
                markeredgewidth=CHARTS_MARKER_EDGE_WIDTH,
                zorder=CHARTS_FRONT_LAYER if codec == MCV2_CODEC else CHARTS_BACK_LAYER,
            )
            offset, align = CHARTS_LABEL_PLACEMENTS.get(codec, CHARTS_DEFAULT_LABEL_PLACEMENT)
            axis.annotate(
                name,
                (rates[0], scores[0]),
                textcoords="offset points",
                xytext=offset,
                fontsize=CHARTS_SMALL_FONT_SIZE,
                color=CHARTS_SECONDARY,
                ha=align,
                va="center",
            )
        axis.set_xscale("log")
        axis.xaxis.set_major_locator(FixedLocator(CHARTS_TICKS))
        axis.xaxis.set_major_formatter(FuncFormatter(lambda value, _: f"{value:g}"))
        axis.xaxis.set_minor_locator(NullLocator())
        axis.set_title(title, fontsize=CHARTS_TITLE_FONT_SIZE, color=CHARTS_INK, loc="left")
        axis.set_xlabel("Rate on the wire, Mbit/s (log scale)", color=CHARTS_SECONDARY)
        axis.grid(True, color=CHARTS_GRID, linewidth=CHARTS_GRID_LINE_WIDTH, zorder=CHARTS_GRID_LAYER)
        axis.set_ylim(CHARTS_LOWEST_SCORE, CHARTS_HIGHEST_SCORE)
        axis.margins(x=CHARTS_RATE_MARGIN)
        for side in ("top", "right"):
            axis.spines[side].set_visible(False)
    for axis in axes:
        axis.set_ylabel("VMAF (mean)", color=CHARTS_SECONDARY)
        axis.xaxis.set_tick_params(labelbottom=True)
    handles, labels = axes[0].get_legend_handles_labels()
    figure.legend(handles, labels, loc="lower center", ncol=len(labels), frameon=False, fontsize=CHARTS_LEGEND_FONT_SIZE)
    figure.tight_layout(rect=(0, CHARTS_LEGEND_HEIGHT, 1, 1))
    figure.savefig(IMAGES / CODECS_FIGURE_NAME, dpi=CHARTS_DOTS_PER_INCH, facecolor=CHARTS_SURFACE)
    pyplot.close(figure)


def charts_curve(data, source, codec):
    if codec == MCV2_CODEC:
        points = [
            (point[ZLIB_RATE_KEY], point[VMAF_MEAN_KEY]) for point in data[MCV2_CODEC] if point[SOURCE_KEY] == source
        ]
    else:
        points = [
            (point[CONTAINER_RATE_KEY], point[VMAF_MEAN_KEY])
            for point in data[CODECS_KEY]
            if point[SOURCE_KEY] == source and point[CODEC_KEY] == codec
        ]

    return sorted(points)


def charts_ablation_rates(ablation):
    results = []
    for feature in ablation.get("features", []):
        row = {FEATURE_ID_KEY: feature[FEATURE_ID_KEY], FEATURE_NAME_KEY: feature[FEATURE_NAME_KEY]}
        for source, _ in CHARTS_SOURCES:
            baseline = [(point[ZLIB_RATE_KEY], point[VMAF_MEAN_KEY]) for point in ablation["baseline"][source]]
            test = [(point[ZLIB_RATE_KEY], point[VMAF_MEAN_KEY]) for point in feature["points"][source]]
            row[source] = bd_rate(baseline, test)
        results.append(row)

    return results


def charts_draw_features(rows):
    pyplot = figure_pyplot()
    rows = sorted(rows, key=lambda row: row[GAMEPLAY_SOURCE][0] + row[PROXY_SOURCE][0])
    values = [row[source][0] for row in rows for source, _ in CHARTS_SOURCES]
    ordered = sorted(values, reverse=True)
    limit = (
        max(ordered[1] * CHARTS_CLIPPED_HEADROOM, CHARTS_MINIMUM_CLIPPED_LIMIT)
        if len(ordered) > 1 and ordered[0] > CHARTS_OUTLIER_RATIO * ordered[1]
        else None
    )
    height = CHARTS_BAR_HEIGHT
    figure, axis = pyplot.subplots(figsize=(CHARTS_FEATURES_WIDTH, CHARTS_FEATURES_ROW_HEIGHT * len(rows) + CHARTS_FEATURES_BASE_HEIGHT))
    for index, (source, title) in enumerate(CHARTS_SOURCES):
        positions = [row_index + (0.5 - index) * (height + CHARTS_BAR_GAP) for row_index in range(len(rows))]
        shown = [min(row[source][0], limit) if limit else row[source][0] for row in rows]
        axis.barh(positions, shown, height=height, color=CHARTS_SOURCE_COLOURS[source], label=title, zorder=CHARTS_FRONT_LAYER)
        clipped = [(position, row, value) for position, row, value in zip(positions, rows, shown) if limit and row[source][0] > limit]
        for position, row, value in clipped:
            axis.annotate(
                f"{row[source][0]:+.0f}% →",
                (value, position),
                textcoords="offset points",
                xytext=CHARTS_BAR_LABEL_OFFSET,
                ha="right",
                va="center",
                fontsize=CHARTS_BAR_LABEL_FONT_SIZE,
                color=CHARTS_SURFACE,
                zorder=CHARTS_LABEL_LAYER,
            )
    axis.set_yticks(range(len(rows)))
    axis.set_yticklabels([row[FEATURE_NAME_KEY] for row in rows], color=CHARTS_INK)
    axis.axvline(0, color=CHARTS_SECONDARY, linewidth=CHARTS_AXIS_LINE_WIDTH, zorder=CHARTS_BACK_LAYER)
    axis.set_xlabel("Extra rate on the wire with the feature turned off (BD-rate, %)", color=CHARTS_SECONDARY)
    axis.grid(True, axis="x", color=CHARTS_GRID, linewidth=CHARTS_GRID_LINE_WIDTH, zorder=CHARTS_GRID_LAYER)
    if limit:
        axis.set_xlim(right=limit * CHARTS_CLIPPED_MARGIN)
    for side in ("top", "right"):
        axis.spines[side].set_visible(False)
    axis.legend(loc="lower right", frameon=False, fontsize=CHARTS_LEGEND_FONT_SIZE)
    figure.tight_layout()
    figure.savefig(IMAGES / FEATURES_FIGURE_NAME, dpi=CHARTS_DOTS_PER_INCH, facecolor=CHARTS_SURFACE)
    pyplot.close(figure)


def charts_print_tables(data, rows, removed):
    print("Rate on the wire (Mbit/s) for the same VMAF mean, log-linear between measured points:\n")
    print("| VMAF mean | " + MARKDOWN_CELL_SEPARATOR.join(name for _, name, _, _ in CHARTS_CODECS) + MARKDOWN_ROW_END)
    print("|---:|" + MARKDOWN_RIGHT_ALIGNED_RULE * len(CHARTS_CODECS))
    for source, title in CHARTS_SOURCES:
        print(f"| **{title}** |" + MARKDOWN_ROW_END * len(CHARTS_CODECS))
        for level in CHARTS_LEVELS:
            cells = [charts_rate_cell(data, source, codec, level) for codec, _, _, _ in CHARTS_CODECS]
            print(f"| {level} | " + MARKDOWN_CELL_SEPARATOR.join(cells) + MARKDOWN_ROW_END)
    print(
        "\nBD-rate of MCV2 against each codec (positive: MCV2 needs more), over the VMAF range both cover:\n"
    )
    for source, title in CHARTS_SOURCES:
        mcv2 = charts_curve(data, source, MCV2_CODEC)
        cells = []
        for codec, name, _, _ in CHARTS_CODECS[1:]:
            percent, low, high = bd_rate(charts_curve(data, source, codec), mcv2)
            cells.append(f"{name} {percent:+.1f}% (VMAF {low:.1f}-{high:.1f})")
        print(f"- {title}: " + "; ".join(cells))
    if rows:
        print("\nExtra rate on the wire without each feature (BD-rate against the full encoder):\n")
        print("| Feature turned off | " + MARKDOWN_CELL_SEPARATOR.join(title for _, title in CHARTS_SOURCES) + MARKDOWN_ROW_END)
        print(MARKDOWN_HEADER_RULE + MARKDOWN_RIGHT_ALIGNED_RULE * len(CHARTS_SOURCES))
        for row in sorted(rows, key=lambda row: -(row[GAMEPLAY_SOURCE][0] + row[PROXY_SOURCE][0])):
            cells = [
                f"{charts_signed(row[source][0])} ({row[source][1]:.0f}-{row[source][2]:.0f})"
                for source, _ in CHARTS_SOURCES
            ]
            print(f"| {row[FEATURE_NAME_KEY]} | " + MARKDOWN_CELL_SEPARATOR.join(cells) + MARKDOWN_ROW_END)
    if removed:
        print("\nWhat each feature MCV2 no longer has was worth (BD-rate when it was measured):\n")
        print("| Feature | " + MARKDOWN_CELL_SEPARATOR.join(title for _, title in CHARTS_SOURCES) + " | Mean |")
        print(MARKDOWN_HEADER_RULE + MARKDOWN_RIGHT_ALIGNED_RULE * (len(CHARTS_SOURCES) + 1))
        for row in sorted(removed, key=lambda row: -row[BD_RATE_KEY]["mean"]):
            cells = [charts_signed(row[BD_RATE_KEY][source]) for source, _ in CHARTS_SOURCES]
            print(f"| {row[FEATURE_NAME_KEY]} | " + MARKDOWN_CELL_SEPARATOR.join(cells) + f" | {charts_signed(row['bd_rate']['mean'])} |")


def charts_rate_cell(data, source, codec, level):
    rate = charts_rate_at(charts_curve(data, source, codec), level)
    return "-" if rate is None else f"{rate:.2f}"


def charts_rate_at(points, level):
    by_quality = sorted(points, key=lambda point: point[1])
    for (low_rate, low), (high_rate, high) in zip(by_quality, by_quality[1:]):
        if low <= level <= high and high > low:
            share = (level - low) / (high - low)
            return math.exp(math.log(low_rate) + share * (math.log(high_rate) - math.log(low_rate)))

    return None


def charts_signed(value):
    text = f"{value:+.1f}%"
    return "0.0%" if text in ("+0.0%", "-0.0%") else text


def samples_main():
    parser = argparse.ArgumentParser(
        description=(
            "Decode archives independently to draw cropped leaf trees and source/decoded palette or pattern "
            "leaves, or print frame bytes. --size describes the raw source dimensions. Exit 1 on input/render "
            "failure; 2 on invalid options."
        ),
    )
    parser.add_argument(
        "--size", type=lambda text: tuple(int(value) for value in text.split("x")), default=(DEFAULT_WIDTH, DEFAULT_HEIGHT)
    )
    commands = parser.add_subparsers(dest="command", required=True)
    tree = commands.add_parser(TREE_COMMAND)
    tree.add_argument("archive")
    tree.add_argument(FRAMES_OPTION, required=True)
    tree.add_argument(CROP_OPTION, required=True)
    tree.add_argument(OUT_OPTION, default=TREE_FIGURE_NAME)
    leaves = commands.add_parser(LEAVES_COMMAND)
    leaves.add_argument("archive")
    leaves.add_argument("source")
    leaves.add_argument(FRAME_OPTION, type=int, default=0)
    leaves.add_argument(CROP_OPTION, required=True)
    dump = commands.add_parser(BYTES_COMMAND)
    dump.add_argument("archive")
    dump.add_argument(FRAME_OPTION, type=int, default=0)
    arguments = parser.parse_args()
    if arguments.command != BYTES_COMMAND:
        logging.getLogger(FONT_MANAGER_LOGGER).setLevel(logging.ERROR)
        charts_style()
        IMAGES.mkdir(parents=True, exist_ok=True)
    {TREE_COMMAND: samples_draw_tree, LEAVES_COMMAND: samples_draw_leaves, BYTES_COMMAND: samples_print_bytes}[
        arguments.command
    ](arguments)


def samples_draw_tree(arguments):
    from matplotlib.patches import Patch, Rectangle

    pyplot = figure_pyplot()
    numbers = [int(value) for value in arguments.frames.split(LIST_SEPARATOR)]
    frames = samples_decoded(arguments.archive, set(numbers))
    pixel_x, pixel_y, width, height = crop = samples_crop_of(arguments.crop)
    colours = {mode: colour for mode, _, colour in SAMPLES_MODES}
    figure, axes = pyplot.subplots(
        SAMPLES_FRAME_ROWS, len(numbers), figsize=(SAMPLES_COLUMN_WIDTH * len(numbers), SAMPLES_FIGURE_HEIGHT), squeeze=False
    )
    for column, number in enumerate(numbers):
        frame, picture = frames[number]
        kind = "keyframe" if frame.keyframe else "P frame"
        top, bottom = (axes[0][column], axes[1][column])
        top.imshow(picture[pixel_y : pixel_y + height, pixel_x : pixel_x + width], interpolation="nearest")
        bottom.imshow(numpy.full((height, width, CHANNELS), MAXIMUM_CHANNEL, numpy.uint8))
        inside = [leaf for leaf in frame.leaves if samples_inside(leaf, crop)]
        for leaf in inside:
            corner = (leaf.pixel_x - pixel_x - SAMPLES_HALF_PIXEL, leaf.pixel_y - pixel_y - SAMPLES_HALF_PIXEL)
            top.add_patch(
                Rectangle(corner, leaf.size, leaf.size, fill=False, edgecolor=SAMPLES_LEAF_OUTLINE, linewidth=CHARTS_FINE_LINE_WIDTH)
            )
            bottom.add_patch(
                Rectangle(
                    corner,
                    leaf.size,
                    leaf.size,
                    facecolor=colours[leaf.mode],
                    edgecolor=CHARTS_SURFACE,
                    linewidth=CHARTS_FINE_LINE_WIDTH,
                )
            )
        for axis in (top, bottom):
            axis.set_xlim(-SAMPLES_HALF_PIXEL, width - SAMPLES_HALF_PIXEL)
            axis.set_ylim(height - SAMPLES_HALF_PIXEL, -SAMPLES_HALF_PIXEL)
        samples_finish(top, f"Frame {number} ({kind}): {frame.total:,} bytes, {len(frame.leaves):,} leaves")
        samples_finish(bottom, f"The leaves of frame {number} by mode")
    handles = [
        Patch(facecolor=colour, edgecolor=CHARTS_SECONDARY, linewidth=CHARTS_FINE_LINE_WIDTH, label=name)
        for _, name, colour in SAMPLES_MODES
    ]
    columns = min(len(SAMPLES_MODES), SAMPLES_LEGEND_COLUMNS_PER_FRAME * len(numbers))
    figure.legend(handles=handles, loc="lower center", ncol=columns, frameon=False, fontsize=CHARTS_SMALL_FONT_SIZE)
    figure.tight_layout(rect=(0, SAMPLES_LEGEND_ROW_HEIGHT * len(SAMPLES_MODES) / columns, 1, 1), h_pad=SAMPLES_ROW_PADDING)
    figure.savefig(IMAGES / arguments.out, dpi=CHARTS_DOTS_PER_INCH, facecolor=CHARTS_SURFACE)
    pyplot.close(figure)
    for number in numbers:
        frame = frames[number][0]
        sizes = {
            size: sum(1 for leaf in frame.leaves if leaf.size == size) for size in mcv2_reference.LEAF_SIZES
        }
        print(
            f"frame {number}: keyframe={frame.keyframe} bytes={frame.total} leaves={len(frame.leaves)} by size {sizes} by mode "
            + str(
                {
                    name: sum(1 for leaf in frame.leaves if leaf.mode == mode)
                    for mode, name, _ in SAMPLES_MODES
                }
            )
        )


def samples_decoded(path, wanted):
    decoder, found = (Decoder(), {})
    for index, data in enumerate(read_archive(path)):
        picture = decoder.accept(data)
        if index in wanted:
            found[index] = (parse_frame(data), picture.copy())
        if len(found) == len(wanted):
            break

    return found


def samples_crop_of(text):
    pixel_x, pixel_y, width, height = (int(value) for value in text.split(LIST_SEPARATOR))
    return (pixel_x, pixel_y, width, height)


def samples_inside(leaf, crop):
    pixel_x, pixel_y, width, height = crop
    return (
        leaf.pixel_x < pixel_x + width
        and leaf.pixel_x + leaf.size > pixel_x
        and (leaf.pixel_y < pixel_y + height)
        and (leaf.pixel_y + leaf.size > pixel_y)
    )


def samples_finish(axis, title):
    axis.set_xticks([])
    axis.set_yticks([])
    for spine in axis.spines.values():
        spine.set_visible(False)
    axis.set_title(title, fontsize=CHARTS_FONT_SIZE, color=CHARTS_INK, loc="left")


def samples_draw_leaves(arguments):
    pyplot = figure_pyplot()
    frame, picture = samples_decoded(arguments.archive, {arguments.frame})[arguments.frame]
    width, height = arguments.size
    source = samples_source_frame(arguments.source, arguments.frame, width, height)
    crop = samples_crop_of(arguments.crop)
    leaves = [
        samples_biggest(frame, mcv2_reference.PALETTE, crop),
        samples_biggest(frame, mcv2_reference.PATTERN, crop),
    ]
    figure, axes = pyplot.subplots(len(leaves), SAMPLES_LEAF_PANELS, figsize=SAMPLES_LEAVES_FIGURE_SIZE)
    for row, leaf in zip(axes, leaves):
        name = "PALETTE" if leaf.mode == mcv2_reference.PALETTE else "PATTERN"
        size = leaf.size
        block = source[leaf.pixel_y : leaf.pixel_y + size, leaf.pixel_x : leaf.pixel_x + size]
        colours = [tuple(leaf.record[0:CHANNELS]), tuple(leaf.record[CHANNELS:COLORS_BYTES])]
        bits = samples_bits_of(leaf)
        row[0].imshow(block, interpolation="nearest")
        samples_finish(row[0], f"{name} {size}x{size} at ({leaf.pixel_x}, {leaf.pixel_y}): source")
        swatch = numpy.array([[colours[0]], [colours[1]]], numpy.uint8)
        row[1].imshow(swatch, interpolation="nearest", aspect="auto")
        for index, colour in enumerate(colours):
            light = sum(colour) > SAMPLES_LIGHT_THRESHOLD
            row[1].text(
                0,
                index,
                f"colour {index}\n{colour[0]}, {colour[1]}, {colour[2]}",
                ha="center",
                va="center",
                fontsize=CHARTS_SMALL_FONT_SIZE,
                color=CHARTS_INK if light else CHARTS_SURFACE,
            )
        samples_finish(row[1], "its two colours")
        row[2].imshow(bits, cmap="gray_r", vmin=0, vmax=SAMPLES_BITS_COLOUR_LIMIT, interpolation="nearest")
        for edge in range(size + 1):
            row[2].axhline(edge - SAMPLES_HALF_PIXEL, color=CHARTS_GRID, linewidth=CHARTS_HAIRLINE_WIDTH)
            row[2].axvline(edge - SAMPLES_HALF_PIXEL, color=CHARTS_GRID, linewidth=CHARTS_HAIRLINE_WIDTH)
        if leaf.mode == mcv2_reference.PALETTE:
            what = f"{size * size} bits, one per pixel"
        else:
            what = f"{size} bits, one per {('column' if leaf.record[PATTERN_ORIENTATION_INDEX] == VERTICAL_STRIPES else 'row')}"
        samples_finish(row[2], f"bits (grey is 1): {what}")
        row[3].imshow(
            picture[leaf.pixel_y : leaf.pixel_y + size, leaf.pixel_x : leaf.pixel_x + size],
            interpolation="nearest",
        )
        samples_finish(row[3], f"decoded: {len(leaf.record)} bytes")
        print(
            f"{name} {size}x{size} at ({leaf.pixel_x}, {leaf.pixel_y}) colours {colours} record {leaf.record.hex()}"
        )
    figure.tight_layout()
    figure.savefig(IMAGES / LEAVES_FIGURE_NAME, dpi=CHARTS_DOTS_PER_INCH, facecolor=CHARTS_SURFACE)
    pyplot.close(figure)


def samples_source_frame(path, index, width, height):
    size = width * height * CHANNELS
    return numpy.fromfile(path, numpy.uint8, size, offset=index * size).reshape(height, width, CHANNELS)


def samples_biggest(frame, mode, crop):
    candidates = [leaf for leaf in frame.leaves if leaf.mode == mode and samples_inside(leaf, crop)]
    candidates = candidates or [leaf for leaf in frame.leaves if leaf.mode == mode]
    return max(candidates, key=lambda leaf: (leaf.size, -leaf.pixel_y, -leaf.pixel_x)) if candidates else None


def samples_bits_of(leaf):
    record = leaf.record
    if leaf.mode == mcv2_reference.PALETTE:
        bits = numpy.unpackbits(numpy.frombuffer(record[COLORS_BYTES:], numpy.uint8), bitorder=BIT_ORDER)
        return bits.reshape(leaf.size, leaf.size)

    axis = numpy.unpackbits(numpy.frombuffer(record[PATTERN_HEADER_BYTES:], numpy.uint8), bitorder=BIT_ORDER)
    return numpy.broadcast_to(
        axis[None, :] if record[PATTERN_ORIENTATION_INDEX] == VERTICAL_STRIPES else axis[:, None], (leaf.size, leaf.size)
    )


def samples_print_bytes(arguments):
    data = list(read_archive(arguments.archive))[arguments.frame]
    frame = parse_frame(data)
    groups = len(frame.masks)
    parts = [("header", 0, mcv2_reference.HEADER_BYTES)]
    offset = mcv2_reference.HEADER_BYTES
    for name, length in (
        ("presence masks", WORD_BYTES * groups),
        ("directory", WORD_BYTES * len(frame.directory)),
        ("level counts", LEVEL_COUNTS.size),
        ("descriptors", len(frame.descriptors)),
        ("walk checkpoints", WORD_BYTES * len(frame.walk)),
    ):
        parts.append((name, offset, offset + length))
        offset += length
    parts.append(("records", offset, frame.total))
    for name, start, end in parts:
        print(f"{name} ({end - start} bytes, offsets {start}-{end - 1}):")
        for line in range(start, end, SAMPLES_HEXDUMP_WIDTH):
            print(f"  {line:5d}  " + WORD_SEPARATOR.join(f"{value:02x}" for value in data[line : min(line + SAMPLES_HEXDUMP_WIDTH, end)]))
    print(
        f"width {frame.width} height {frame.height} keyframe {frame.keyframe} frame id {frame.frame_id} reference id {frame.reference_id} payload start {frame.payload_start} length {frame.total}"
    )
    print(
        f"masks {[hex(mask) for mask in frame.masks]} directory {list(frame.directory)} levels {frame.level_counts} walk {[(value & WALK_CURSOR_MASK, value >> WALK_CURSOR_BITS) for value in frame.walk]}"
    )
    names = {mode: name for mode, name, _ in SAMPLES_MODES}
    names[mcv2_reference.SPLIT] = "SPLIT"
    for index, descriptor in enumerate(frame.descriptors):
        print(f"descriptor {index}: 0x{descriptor:02x} = {names[descriptor & MODE_MASK]} q={descriptor >> QUANTIZER_SHIFT}")
    for leaf in frame.leaves:
        if leaf.offset is not None:
            print(
                f"leaf {leaf.size}x{leaf.size} at ({leaf.pixel_x}, {leaf.pixel_y}) {names[leaf.mode]} q={leaf.quantizer} offset {leaf.offset}: {leaf.record.hex()}"
            )


if __name__ == "__main__":
    sys.exit(main())
