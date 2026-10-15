/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.brandonli.mcav.bukkit.media.mcv2;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import me.brandonli.mcav.bukkit.testing.UtilityClassAssertions;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.level.material.MapColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class Mcv2PackTest {

  @TempDir
  private Path directory;

  @Test
  void shouldBeAUtilityClass() throws ReflectiveOperationException {
    UtilityClassAssertions.assertNotInstantiable(Mcv2Pack.class);
  }

  private static Map<String, String> read(final Path zip) throws IOException {
    final Map<String, String> entries = new HashMap<>();
    try (final InputStream input = Files.newInputStream(zip); final ZipInputStream stream = new ZipInputStream(input)) {
      for (ZipEntry entry = stream.getNextEntry(); entry != null; entry = stream.getNextEntry()) {
        entries.put(entry.getName(), new String(stream.readAllBytes(), StandardCharsets.UTF_8));
      }
    }
    return entries;
  }

  @Test
  void shouldWriteThePackOfAScreen() throws IOException {
    final Mcv2Configuration configuration = Mcv2ConfigurationTest.complete()
      .video(320, 180)
      .pageSlots(2)
      .streamId(9)
      .outlineColor(NamedTextColor.GOLD)
      .build();
    final Path zip = this.directory.resolve("pack.zip");
    Mcv2Pack.write(configuration, true, zip);
    final Map<String, String> entries = read(zip);
    for (final String name : new String[] {
      "pack.mcmeta",
      "mcav_mcv2.json",
      "assets/minecraft/post_effect/entity_outline.json",
      "assets/minecraft/shaders/core/text.vsh",
      "assets/minecraft/shaders/core/text.fsh",
      "assets/mcav/shaders/include/mcv2.glsl",
      "assets/mcav/shaders/include/mcv2_config.glsl",
      "assets/mcav/shaders/include/mcv2_screen_0.glsl",
      "assets/mcav/shaders/include/mcv2_alphabet.glsl",
      "assets/mcav/shaders/post/s0/mcv2_crc.fsh",
      "assets/mcav/shaders/post/s0/mcv2_resolve.fsh",
      "assets/mcav/shaders/post/s0/mcv2_decode.vsh",
      "assets/mcav/shaders/post/s0/mcv2_decode.fsh",
      "assets/mcav/shaders/post/s0/mcv2_view.fsh",
      "assets/mcav/shaders/post/s0/mcv2_screen.vsh",
      "assets/mcav/shaders/post/s0/mcv2_screen.fsh",
      "assets/mcav/shaders/post/mcv2_state.fsh",
      "assets/mcav/shaders/post/mcv2_copy.fsh",
    }) {
      final boolean written = entries.containsKey(name);
      assertTrue(written, name);
    }

    // six shared files, the screen's ten passes and its include, the chain, the shared includes, the manifest, the
    // metadata
    final int entryCount = entries.size();
    assertEquals(22, entryCount);

    final JsonObject metadata = JsonParser.parseString(entries.get("pack.mcmeta")).getAsJsonObject().getAsJsonObject("pack");
    final int packFormat = metadata.get("pack_format").getAsInt();
    assertEquals(Mcv2Pack.PACK_FORMAT, packFormat);
    final String description = metadata.get("description").getAsString();
    assertEquals("mcav MCV2 decoder, 1 screen: 320x180", description);

    final String config = entries.get("assets/mcav/shaders/include/mcv2_config.glsl");
    final boolean declaresOneScreen = config.contains("const int MCV2_SCREENS = 1;");
    assertTrue(declaresOneScreen, config);
    final boolean declaresTwoSlots = config.contains("const int MCV2_TOTAL_SLOTS = 2;");
    assertTrue(declaresTwoSlots, config);
    final boolean declaresTheStream = config.contains("const uint MCV2_SCREEN_STREAMS[1] = uint[1](9u);");
    assertTrue(declaresTheStream, config);
    final boolean declaresTheScreenSlots = config.contains("const int MCV2_SCREEN_SLOTS[1] = int[1](2);");
    assertTrue(declaresTheScreenSlots, config);
    final boolean declaresTheFirstSlotOfEachScreen = config.contains("const int MCV2_SCREEN_FIRST_SLOTS[1] = int[1](0);");
    assertTrue(declaresTheFirstSlotOfEachScreen, config);
    final boolean showsTheDebugView = config.contains("const bool MCV2_DEBUG_VIEW = true;");
    assertTrue(showsTheDebugView, config);
    final boolean declaresTheOutlineColor = config.contains("const ivec3 MCV2_OUTLINE_COLOR = ivec3(255, 170, 0);");
    assertTrue(declaresTheOutlineColor, config);

    final String screen = entries.get("assets/mcav/shaders/include/mcv2_screen_0.glsl");
    final boolean declaresTheScreenIndex = screen.contains("const int MCV2_SCREEN_INDEX = 0;");
    assertTrue(declaresTheScreenIndex, screen);
    final boolean declaresThePageSlots = screen.contains("const int MCV2_PAGE_SLOTS = 2;");
    assertTrue(declaresThePageSlots, screen);
    final boolean declaresTheFirstSlot = screen.contains("const int MCV2_FIRST_SLOT = 0;");
    assertTrue(declaresTheFirstSlot, screen);
    final boolean declaresTheStreamId = screen.contains("const uint MCV2_STREAM_ID = 9u;");
    assertTrue(declaresTheStreamId, screen);
    final boolean declaresTheVideoWidth = screen.contains("const int MCV2_VIDEO_WIDTH = 320;");
    assertTrue(declaresTheVideoWidth, screen);
    final boolean declaresTheVideoHeight = screen.contains("const int MCV2_VIDEO_HEIGHT = 180;");
    assertTrue(declaresTheVideoHeight, screen);
    final boolean declaresTheDebugTop = screen.contains("const int MCV2_DEBUG_TOP = 0;");
    assertTrue(declaresTheDebugTop, screen);
    final boolean declaresTheBytesHeight = screen.contains("const int MCV2_BYTES_HEIGHT = 48;");
    assertTrue(declaresTheBytesHeight, screen);
    // one cell per 8x8 pixels: 40 columns, 23 rows of cells (180 / 8 rounded up)
    final boolean declaresTheCellsWidth = screen.contains("const int MCV2_CELLS_WIDTH = 40;");
    assertTrue(declaresTheCellsWidth, screen);
    final boolean declaresTheCellsHeight = screen.contains("const int MCV2_CELLS_HEIGHT = 23;");
    assertTrue(declaresTheCellsHeight, screen);

    final String pass = entries.get("assets/mcav/shaders/post/s0/mcv2_crc.fsh");
    final boolean includesTheScreen = pass.contains("#include <mcav:mcv2_screen_0.glsl>");
    assertTrue(includesTheScreen, pass);
    final boolean includesTheSharedScreen = pass.contains("mcv2_screen.glsl");
    assertFalse(includesTheSharedScreen, pass);

    final String chain = entries.get("assets/minecraft/post_effect/entity_outline.json");
    final boolean hasAnUnfilledToken = chain.contains("@");
    assertFalse(hasAnUnfilledToken, "every token is filled in");

    final JsonObject parsed = JsonParser.parseString(chain).getAsJsonObject();
    final JsonObject targets = parsed.getAsJsonObject("targets");
    final int previousWidth = targets.getAsJsonObject("mcav:mcv2_previous_0").get("width").getAsInt();
    assertEquals(320, previousWidth);
    final int previousHeight = targets.getAsJsonObject("mcav:mcv2_previous_0").get("height").getAsInt();
    assertEquals(180, previousHeight);
    final boolean persistent = targets.getAsJsonObject("mcav:mcv2_previous_0").get("persistent").getAsBoolean();
    assertTrue(persistent);
    final boolean hasAKeyTarget = targets.has("mcav:mcv2_key_0");
    assertFalse(hasAKeyTarget);
    final boolean hasANextKeyTarget = targets.has("mcav:mcv2_key_next_0");
    assertFalse(hasANextKeyTarget);
    final boolean writesTheBooks = entries.containsKey("assets/mcav/shaders/include/mcv2_books.glsl");
    assertFalse(writesTheBooks);
    final boolean writesTheKeyframePass = entries.containsKey("assets/mcav/shaders/post/mcv2_keyframe.fsh");
    assertFalse(writesTheKeyframePass);
    final int pagesWidth = targets.getAsJsonObject("mcav:mcv2_pages_0").get("width").getAsInt();
    assertEquals(8, pagesWidth);
    // two pages of 12,256 bytes, four to a texel, 128 texels to a row
    final int bytesHeight = targets.getAsJsonObject("mcav:mcv2_bytes_0").get("height").getAsInt();
    assertEquals(48, bytesHeight);
    // 64 CRC chunks per page slot
    final int crcWidth = targets.getAsJsonObject("mcav:mcv2_crc_0").get("width").getAsInt();
    assertEquals(128, crcWidth);
    // the cells and, after them, the frame row
    final int cellsWidth = targets.getAsJsonObject("mcav:mcv2_cells_0").get("width").getAsInt();
    assertEquals(40, cellsWidth);
    final int cellsHeight = targets.getAsJsonObject("mcav:mcv2_cells_0").get("height").getAsInt();
    assertEquals(24, cellsHeight);
    final boolean hasTheScreenAndSwapTargets = targets.has("mcav:mcv2_screen") && targets.has("swap");
    assertTrue(hasTheScreenAndSwapTargets);

    // ten passes decode, two draw, six are the outline's
    final JsonArray passes = parsed.getAsJsonArray("passes");
    final int passCount = passes.size();
    assertEquals(18, passCount);
    final String firstDecodeShader = passes.get(0).getAsJsonObject().get("fragment_shader").getAsString();
    assertEquals("mcav:post/s0/mcv2_bytes", firstDecodeShader);
    final String screenShader = passes.get(10).getAsJsonObject().get("fragment_shader").getAsString();
    assertEquals("mcav:post/s0/mcv2_screen", screenShader);
    final String outlineShader = passes.get(12).getAsJsonObject().get("fragment_shader").getAsString();
    assertEquals("mcav:post/mcv2_outline", outlineShader);

    final String licenseHeader = Files.readString(Path.of("../HEADER"), StandardCharsets.UTF_8);
    for (final String name : entries.keySet()) {
      if (name.startsWith("assets/mcav/shaders/post/")) {
        final String stub = entries.get(name);
        final boolean startsWithTheHeader = stub.startsWith(
          licenseHeader + "\n#version 330\n#extension GL_ARB_separate_shader_objects : require\n#define MCV2_PASS_"
        );
        assertTrue(startsWithTheHeader, name);
        final boolean endsWithTheInclude = stub.endsWith("#include <mcav:mcv2.glsl>\n");
        assertTrue(endsWithTheInclude, name);
        final long expectedLineCount = name.contains("/s0/") ? 24L : 23L;
        final long lineCount = stub.lines().count();
        assertEquals(expectedLineCount, lineCount, name);
        final boolean declaresAFunction = stub.contains("void ");
        assertFalse(declaresAFunction, name);
      }
    }

    final JsonObject manifest = JsonParser.parseString(entries.get("mcav_mcv2.json")).getAsJsonObject();
    final String codec = manifest.get("codec").getAsString();
    assertEquals("MCV2", codec);
    final String codecCommit = manifest.get("gpu_codec_commit").getAsString();
    assertEquals(Mcv2Pack.CODEC_COMMIT, codecCommit);
    final int version = manifest.get("version").getAsInt();
    assertEquals(3, version);
    final int manifestSize = manifest.size();
    assertEquals(4, manifestSize);

    final JsonObject described = manifest.getAsJsonArray("screens").get(0).getAsJsonObject();
    final int describedSize = described.size();
    assertEquals(4, describedSize, "the encoder's profile is not the pack's business");
    final int videoWidth = described.get("video_width").getAsInt();
    assertEquals(320, videoWidth);
    final int videoHeight = described.get("video_height").getAsInt();
    assertEquals(180, videoHeight);
    final int pageSlots = described.get("page_slots").getAsInt();
    assertEquals(2, pageSlots);
    final long streamId = described.get("stream_id").getAsLong();
    assertEquals(9, streamId);

    // every channel of the outline colour, the red one too
    final Mcv2Configuration purple = Mcv2ConfigurationTest.complete().outlineColor(NamedTextColor.DARK_PURPLE).build();
    final boolean declaresThePurpleOutline = Mcv2Pack.packConstants(List.of(purple), false).contains(
      "const ivec3 MCV2_OUTLINE_COLOR = ivec3(170, 0, 170);"
    );
    assertTrue(declaresThePurpleOutline);
  }

  @Test
  void shouldWriteOnePackForSeveralScreens() throws IOException {
    final Mcv2Configuration small = Mcv2ConfigurationTest.complete().video(320, 180).pageSlots(2).streamId(9).build();
    final Mcv2Configuration large = Mcv2ConfigurationTest.complete().video(640, 360).pageSlots(8).streamId(3).build();
    final Path zip = this.directory.resolve("screens.zip");
    Mcv2Pack.write(List.of(small, large), false, zip);
    final Map<String, String> entries = read(zip);
    final int entryCount = entries.size();
    assertEquals(33, entryCount);

    final String config = entries.get("assets/mcav/shaders/include/mcv2_config.glsl");
    final boolean declaresTwoScreens = config.contains("const int MCV2_SCREENS = 2;");
    assertTrue(declaresTwoScreens, config);
    final boolean declaresTenSlots = config.contains("const int MCV2_TOTAL_SLOTS = 10;");
    assertTrue(declaresTenSlots, config);
    final boolean declaresTheStreams = config.contains("const uint MCV2_SCREEN_STREAMS[2] = uint[2](9u, 3u);");
    assertTrue(declaresTheStreams, config);
    final boolean declaresTheScreenSlots = config.contains("const int MCV2_SCREEN_SLOTS[2] = int[2](2, 8);");
    assertTrue(declaresTheScreenSlots, config);
    final boolean declaresTheFirstSlotOfEachScreen = config.contains("const int MCV2_SCREEN_FIRST_SLOTS[2] = int[2](0, 2);");
    assertTrue(declaresTheFirstSlotOfEachScreen, config);

    final String second = entries.get("assets/mcav/shaders/include/mcv2_screen_1.glsl");
    final boolean declaresTheScreenIndex = second.contains("const int MCV2_SCREEN_INDEX = 1;");
    assertTrue(declaresTheScreenIndex, second);
    final boolean declaresTheFirstSlot = second.contains("const int MCV2_FIRST_SLOT = 2;");
    assertTrue(declaresTheFirstSlot, second);
    final boolean declaresTheVideoWidth = second.contains("const int MCV2_VIDEO_WIDTH = 640;");
    assertTrue(declaresTheVideoWidth, second);
    // the debug view draws the second screen's picture under the first one's
    final boolean declaresTheDebugTop = second.contains("const int MCV2_DEBUG_TOP = 188;");
    assertTrue(declaresTheDebugTop, second);
    final boolean includesTheSecondScreen = entries
      .get("assets/mcav/shaders/post/s1/mcv2_decode.fsh")
      .contains("#include <mcav:mcv2_screen_1.glsl>");
    assertTrue(includesTheSecondScreen);

    final JsonObject chain = JsonParser.parseString(entries.get("assets/minecraft/post_effect/entity_outline.json")).getAsJsonObject();
    final int previousWidth = chain.getAsJsonObject("targets").getAsJsonObject("mcav:mcv2_previous_1").get("width").getAsInt();
    assertEquals(640, previousWidth);

    // each screen decodes before any draws, so no strip is covered before its pages were read
    final JsonArray passes = chain.getAsJsonArray("passes");
    final int passCount = passes.size();
    assertEquals(30, passCount);
    final String secondDecodeShader = passes.get(10).getAsJsonObject().get("fragment_shader").getAsString();
    assertEquals("mcav:post/s1/mcv2_bytes", secondDecodeShader);
    final String firstScreenShader = passes.get(20).getAsJsonObject().get("fragment_shader").getAsString();
    assertEquals("mcav:post/s0/mcv2_screen", firstScreenShader);
    final String secondScreenShader = passes.get(22).getAsJsonObject().get("fragment_shader").getAsString();
    assertEquals("mcav:post/s1/mcv2_screen", secondScreenShader);

    final String description = describe(entries);
    assertEquals("mcav MCV2 decoder, 2 screens: 320x180, 640x360", description);
  }

  private static String describe(final Map<String, String> entries) {
    return JsonParser.parseString(entries.get("pack.mcmeta")).getAsJsonObject().getAsJsonObject("pack").get("description").getAsString();
  }

  @Test
  void shouldRefuseScreensThatCannotShareAPack() {
    final Path zip = this.directory.resolve("refused.zip");
    final Mcv2Configuration first = Mcv2ConfigurationTest.complete().streamId(1).build();
    final Mcv2Configuration sameStream = Mcv2ConfigurationTest.complete().streamId(1).build();
    final Mcv2Configuration otherColour = Mcv2ConfigurationTest.complete().streamId(2).outlineColor(NamedTextColor.GOLD).build();
    final List<Mcv2Configuration> tooMany = new ArrayList<>();
    for (int stream = 0; stream <= Mcv2Pack.MAX_SCREENS; stream++) {
      tooMany.add(Mcv2ConfigurationTest.complete().streamId(stream).build());
    }
    assertThrows(IllegalArgumentException.class, () -> Mcv2Pack.write(List.of(), false, zip));
    assertThrows(IllegalArgumentException.class, () -> Mcv2Pack.write(tooMany, false, zip));
    assertThrows(IllegalArgumentException.class, () -> Mcv2Pack.write(List.of(first, sameStream), false, zip));
    assertThrows(IllegalArgumentException.class, () -> Mcv2Pack.write(List.of(first, otherColour), false, zip));

    final boolean written = Files.exists(zip);
    assertFalse(written);
  }

  @Test
  void shouldRefuseAScreenPassWithoutTheScreenInclude() {
    final byte[] pass = "void main() {}".getBytes(StandardCharsets.UTF_8);
    assertThrows(IllegalStateException.class, () -> Mcv2Pack.screenCopy(pass, 0));
  }

  @Test
  void shouldSizeTheCellsOnePerEightPixels() {
    final Mcv2Configuration narrow = Mcv2ConfigurationTest.complete().video(16, 9).build();
    final int narrowCellsWidth = Mcv2Pack.cellsWidth(narrow);
    assertEquals(2, narrowCellsWidth);
    final int narrowCellsHeight = Mcv2Pack.cellsHeight(narrow);
    assertEquals(2, narrowCellsHeight);

    final Mcv2Configuration wide = Mcv2ConfigurationTest.complete().video(1920, 1080).build();
    final int wideCellsWidth = Mcv2Pack.cellsWidth(wide);
    assertEquals(240, wideCellsWidth);
    final int wideCellsHeight = Mcv2Pack.cellsHeight(wide);
    assertEquals(135, wideCellsHeight);
  }

  @Test
  void shouldGenerateTheAlphabetFromTheMapPalette() {
    final int[] palette = Mcv2Pack.palette();
    final int colorCount = palette.length;
    assertEquals(64, colorCount);
    for (int symbol = 0; symbol < 64; symbol++) {
      final int expectedColor = MapColor.getColorFromPackedId(symbol + 4) & 0xFFFFFF;
      final int paletteColor = palette[symbol];
      assertEquals(expectedColor, paletteColor);
    }

    final int[] symbols = new int[1 << 24];
    Arrays.fill(symbols, -1);
    for (int symbol = 0; symbol < 64; symbol++) {
      symbols[palette[symbol]] = symbol;
    }
    final String alphabet = Mcv2Pack.alphabet(palette);
    final Matcher hash = Pattern.compile("MCV2_SYMBOL_HASH = (\\d+)u;\nconst uint MCV2_SYMBOL_SHIFT = (\\d+)u;").matcher(alphabet);
    final boolean declaresTheHash = hash.find();
    assertTrue(declaresTheHash);

    final int multiplier = Integer.parseInt(hash.group(1));
    final int shift = Integer.parseInt(hash.group(2));
    final int[] entries = Pattern.compile("(\\d+)u")
      .matcher(alphabet.substring(alphabet.indexOf("MCV2_SYMBOLS")))
      .results()
      .mapToInt(entry -> Integer.parseUnsignedInt(entry.group(1)))
      .toArray();
    final int entryCount = entries.length;
    assertEquals(256, entryCount);

    // every colour a map pixel can have, read as mcv2.glsl's mcv2Symbol reads it
    int misread = 0;
    for (int color = 0; color < 1 << 24; color++) {
      final int entry = entries[(color * multiplier) >>> shift];
      if ((entry >>> 6 == color ? entry & 63 : -1) != symbols[color]) {
        misread++;
      }
    }
    assertEquals(0, misread);

    final int[] clash = palette.clone();
    clash[5] = clash[4];
    final IllegalStateException clashException = assertThrows(IllegalStateException.class, () -> Mcv2Pack.alphabet(clash));
    final String clashMessage = clashException.getMessage();
    assertEquals("Map colours 9 and another symbol are the same RGB", clashMessage);

    final int[] two = { 0x010203, 0x040506 };
    final String empty = " 4294967295u,";
    final String expectedAlphabet =
      "#ifndef MCAV_MCV2_ALPHABET_GLSL\n#define MCAV_MCV2_ALPHABET_GLSL\n\n" +
      "const uint MCV2_SYMBOL_HASH = 65u;\nconst uint MCV2_SYMBOL_SHIFT = 24u;\nconst uint MCV2_SYMBOLS[256] = uint[256](\n" +
      "    4227264u, 16859521u," +
      empty.repeat(6) +
      "\n" +
      ("   " + empty + empty.repeat(7) + "\n").repeat(30) +
      "   " +
      empty +
      empty.repeat(6) +
      " 4294967295u\n);\n\n#endif\n";
    final String twoColorAlphabet = Mcv2Pack.alphabet(two);
    assertEquals(expectedAlphabet, twoColorAlphabet);

    // 65 is the first odd multiplier that tells these two colours apart
    final String expectedTwoColorAlphabet = Mcv2Pack.alphabet(two);
    final String alphabetUpToSixtyFive = Mcv2Pack.alphabet(two, 65);
    assertEquals(expectedTwoColorAlphabet, alphabetUpToSixtyFive);
    final IllegalStateException exhaustedException = assertThrows(IllegalStateException.class, () -> Mcv2Pack.alphabet(two, 63));
    final String exhaustedMessage = exhaustedException.getMessage();
    assertEquals("No odd multiplier up to 63 gives the map colours distinct symbol entries", exhaustedMessage);
  }

  @Test
  void shouldReportAMissingOrUnreadableResource() {
    final IllegalStateException missingException = assertThrows(IllegalStateException.class, () -> Mcv2Pack.read(null, "none"));
    final String missingMessage = missingException.getMessage();
    assertEquals("Missing MCV2 pack resource none", missingMessage);

    final InputStream broken = new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException("broken");
      }

      @Override
      public int read(final byte[] buffer, final int offset, final int length) throws IOException {
        throw new IOException("broken");
      }
    };
    assertThrows(UncheckedIOException.class, () -> Mcv2Pack.read(broken, "broken"));

    final byte[] expectedBytes = new byte[] { 1 };
    final byte[] readBytes = Mcv2Pack.read(new ByteArrayInputStream(new byte[] { 1 }), "one");
    assertArrayEquals(expectedBytes, readBytes);
  }
}
