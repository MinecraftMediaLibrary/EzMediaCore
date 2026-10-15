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
package me.brandonli.mcav.vnc;

import static me.brandonli.mcav.vnc.RfbSession.BELL;
import static me.brandonli.mcav.vnc.RfbSession.BITS_PER_PIXEL_16;
import static me.brandonli.mcav.vnc.RfbSession.BITS_PER_PIXEL_32;
import static me.brandonli.mcav.vnc.RfbSession.BITS_PER_PIXEL_8;
import static me.brandonli.mcav.vnc.RfbSession.COPY_RECTANGLE_ENCODING;
import static me.brandonli.mcav.vnc.RfbSession.CURSOR_ENCODING;
import static me.brandonli.mcav.vnc.RfbSession.DESKTOP_NAME;
import static me.brandonli.mcav.vnc.RfbSession.DESKTOP_SIZE_ENCODING;
import static me.brandonli.mcav.vnc.RfbSession.HEXTILE_BACKGROUND;
import static me.brandonli.mcav.vnc.RfbSession.HEXTILE_COLOURED;
import static me.brandonli.mcav.vnc.RfbSession.HEXTILE_ENCODING;
import static me.brandonli.mcav.vnc.RfbSession.HEXTILE_FOREGROUND;
import static me.brandonli.mcav.vnc.RfbSession.HEXTILE_RAW;
import static me.brandonli.mcav.vnc.RfbSession.HEXTILE_SUBRECTANGLES;
import static me.brandonli.mcav.vnc.RfbSession.MINOR_VERSION_3_3;
import static me.brandonli.mcav.vnc.RfbSession.MINOR_VERSION_3_8;
import static me.brandonli.mcav.vnc.RfbSession.MS_LOGON;
import static me.brandonli.mcav.vnc.RfbSession.NONE;
import static me.brandonli.mcav.vnc.RfbSession.RAW_ENCODING;
import static me.brandonli.mcav.vnc.RfbSession.RRE_ENCODING;
import static me.brandonli.mcav.vnc.RfbSession.SERVER_CUT_TEXT;
import static me.brandonli.mcav.vnc.RfbSession.SET_COLOUR_MAP_ENTRIES;
import static me.brandonli.mcav.vnc.RfbSession.SHARE_DESKTOP;
import static me.brandonli.mcav.vnc.RfbSession.VNC;
import static me.brandonli.mcav.vnc.RfbSession.ZLIB_ENCODING;
import static me.brandonli.mcav.vnc.RfbSession.concat;
import static me.brandonli.mcav.vnc.RfbSession.cutText;
import static me.brandonli.mcav.vnc.RfbSession.rectangle;
import static me.brandonli.mcav.vnc.RfbSession.serverInitialization;
import static me.brandonli.mcav.vnc.RfbSession.setPixelFormat;
import static me.brandonli.mcav.vnc.RfbSession.unsigned16;
import static me.brandonli.mcav.vnc.RfbSession.unsigned32;
import static me.brandonli.mcav.vnc.RfbSession.update;
import static me.brandonli.mcav.vnc.RfbSession.version;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link RfbGuard}: every handshake the client supports passes, every message and encoding is framed so the
 * messages after it pass too, and every length or size above its bound, and every byte the protocol does not allow,
 * is refused before the client would read it.
 */
final class RfbGuardTest {

  private static final int SIDE = 64;

  private static RfbSession connected() throws IOException {
    final RfbSession session = new RfbSession();
    session.handshake(MINOR_VERSION_3_8, NONE, SIDE, SIDE);
    return session;
  }

  private static void assertRefused(final RfbSession session, final byte[] bytes, final String reason) {
    final IOException refused = assertThrows(IOException.class, () -> session.server(bytes));
    final String message = refused.getMessage();
    final boolean named = message.contains(reason);
    assertTrue(named, message);
  }

  /** The guard waits for a server message: a bell passes, and the byte after it is refused as a message type. */
  private static void assertAtAMessage(final RfbSession session) throws IOException {
    session.server(new byte[] { BELL });
    assertRefused(session, new byte[] { 9 }, "message type 9");
  }

  /** Data the guard only counts, of a byte that is no message type, so data read as a message is refused. */
  private static byte[] pixels(final int length) {
    final byte[] bytes = new byte[length];
    Arrays.fill(bytes, (byte) 0x55);
    return bytes;
  }

  @ParameterizedTest
  @ValueSource(ints = { 3, 7, 8 })
  void shouldPassTheHandshakeWithoutSecurityOfEveryVersion(final int minor) throws IOException {
    final RfbSession session = new RfbSession();
    assertDoesNotThrow(() -> session.handshake(minor, NONE, SIDE, SIDE));
    assertAtAMessage(session);
  }

  @ParameterizedTest
  @ValueSource(ints = { 3, 7, 8 })
  void shouldPassTheHandshakeWithVncAuthenticationOfEveryVersion(final int minor) throws IOException {
    final RfbSession session = new RfbSession();
    assertDoesNotThrow(() -> session.handshake(minor, VNC, SIDE, SIDE));
    assertAtAMessage(session);
  }

  @Test
  void shouldPassTheMsLogonHandshake() throws IOException {
    final RfbSession session = new RfbSession();
    assertDoesNotThrow(() -> session.handshake(MINOR_VERSION_3_8, MS_LOGON, SIDE, SIDE));
    assertAtAMessage(session);
  }

  @Test
  void shouldRefuseSecurityDataBeforeTheClientChoseAVersion() throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(MINOR_VERSION_3_8));
    assertRefused(session, new byte[] { 1, NONE }, "before the client chose a version");
  }

  @Test
  void shouldRefuseAChallengeBeforeTheClientChoseASecurityType() throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(MINOR_VERSION_3_8));
    session.client(version(MINOR_VERSION_3_8));
    session.server(new byte[] { 1, VNC });
    assertRefused(session, new byte[16], "before the client chose a security type");
  }

  @Test
  void shouldRefuseAVersionOtherThanThree() {
    final RfbSession session = new RfbSession();
    assertRefused(session, "RFB 004.001\n".getBytes(StandardCharsets.US_ASCII), "version");
  }

  @Test
  void shouldRefuseAVersionWithoutItsNewline() {
    final RfbSession session = new RfbSession();
    assertRefused(session, "RFB 003.008 ".getBytes(StandardCharsets.US_ASCII), "version");
  }

  @Test
  void shouldRefuseASecurityTypeTheClientDoesNotSupport() throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(3));
    session.client(version(3));
    assertRefused(session, unsigned32(19), "security type 19");
  }

  @Test
  void shouldRefuseTheInvalidSecurityTypeAsOneTheClientChose() throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(MINOR_VERSION_3_8));
    session.client(version(MINOR_VERSION_3_8));
    session.server(new byte[] { 1, NONE });
    session.client(new byte[] { 0 });
    assertRefused(session, unsigned32(0), "security type 0");
  }

  @ParameterizedTest
  @ValueSource(ints = { 3, 8 })
  void shouldAcceptAFailureReasonAndNothingAfterIt(final int minor) throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(minor));
    session.client(version(minor));
    final byte[] refusal = minor == MINOR_VERSION_3_3 ? unsigned32(0) : new byte[] { 0 };
    session.server(concat(refusal, unsigned32(4), new byte[] { 'n', 'o', 'p', 'e' }));
    assertRefused(session, new byte[] { 1 }, "after the connection ended");
  }

  @Test
  void shouldAcceptAnEmptyFailureReason() throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(MINOR_VERSION_3_8));
    session.client(version(MINOR_VERSION_3_8));
    session.server(concat(new byte[] { 0 }, unsigned32(0)));
    assertRefused(session, new byte[] { 1 }, "after the connection ended");
  }

  @Test
  void shouldRefuseAFailureReasonAboveItsBound() throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(MINOR_VERSION_3_8));
    session.client(version(MINOR_VERSION_3_8));
    assertRefused(session, concat(new byte[] { 0 }, unsigned32(RfbGuard.MAXIMUM_REASON + 1)), "failure reason");
  }

  @Test
  void shouldEndAfterAFailedAuthenticationOfVersion37() throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(7));
    session.client(version(7));
    session.server(new byte[] { 1, VNC });
    session.client(new byte[] { VNC });
    session.server(new byte[16]);
    session.client(new byte[16]);
    session.server(unsigned32(1));
    assertRefused(session, new byte[] { 0 }, "after the connection ended");
  }

  @Test
  void shouldReadTheReasonOfAFailedAuthenticationOfVersion38() throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(MINOR_VERSION_3_8));
    session.client(version(MINOR_VERSION_3_8));
    session.server(new byte[] { 1, VNC });
    session.client(new byte[] { VNC });
    session.server(new byte[16]);
    session.client(new byte[16]);
    session.server(concat(unsigned32(1), unsigned32(2), new byte[] { 'n', 'o' }));
    assertRefused(session, new byte[] { 0 }, "after the connection ended");
  }

  @ParameterizedTest
  @ValueSource(ints = { 0, RfbGuard.MAXIMUM_SIDE + 1 })
  void shouldRefuseAFramebufferWidthOutsideItsBounds(final int width) throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(3));
    session.client(version(3));
    session.server(unsigned32(NONE));
    session.client(new byte[] { SHARE_DESKTOP });
    assertRefused(session, serverInitialization(width, SIDE, BITS_PER_PIXEL_32, DESKTOP_NAME), "framebuffer");
  }

  @ParameterizedTest
  @ValueSource(ints = { 0, RfbGuard.MAXIMUM_SIDE + 1 })
  void shouldRefuseAFramebufferHeightOutsideItsBounds(final int height) throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(3));
    session.client(version(3));
    session.server(unsigned32(NONE));
    session.client(new byte[] { SHARE_DESKTOP });
    assertRefused(session, serverInitialization(SIDE, height, BITS_PER_PIXEL_32, DESKTOP_NAME), "framebuffer");
  }

  @Test
  void shouldRefusePixelsOfAnUnsupportedWidth() throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(3));
    session.client(version(3));
    session.server(unsigned32(NONE));
    session.client(new byte[] { SHARE_DESKTOP });
    assertRefused(session, serverInitialization(SIDE, SIDE, 24, DESKTOP_NAME), "24 bits per pixel");
  }

  @Test
  void shouldRefuseADesktopNameAboveItsBoundBeforeItsFirstByte() throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(3));
    session.client(version(3));
    session.server(unsigned32(NONE));
    session.client(new byte[] { SHARE_DESKTOP });
    final byte[] header = concat(unsigned16(SIDE), unsigned16(SIDE), RfbSession.pixelFormat(32), unsigned32(RfbGuard.MAXIMUM_NAME + 1L));
    assertRefused(session, header, "desktop name");
  }

  @Test
  void shouldAcceptClipboardTextWithinItsBound() throws IOException {
    final RfbSession session = connected();
    session.server(cutText(RfbGuard.MAXIMUM_TEXT));
    session.server(cutText(0));
    assertAtAMessage(session);
  }

  @Test
  void shouldRefuseClipboardTextAboveItsBound() throws IOException {
    final RfbSession session = connected();
    assertRefused(session, concat(new byte[] { SERVER_CUT_TEXT, 0, 0, 0 }, unsigned32(0xFFFFFFFFL)), "clipboard text");
  }

  @Test
  void shouldFrameColourMapEntries() throws IOException {
    final RfbSession session = connected();
    session.server(concat(new byte[] { SET_COLOUR_MAP_ENTRIES, 0 }, unsigned16(0), unsigned16(2), new byte[12]));
    assertAtAMessage(session);
  }

  @Test
  void shouldRefuseAnUnknownMessageType() throws IOException {
    final RfbSession session = connected();
    assertRefused(session, new byte[] { (byte) 255 }, "message type 255");
  }

  @Test
  void shouldFrameRawRectanglesInTheClientsPixelFormat() throws IOException {
    final RfbSession session = connected();
    session.server(concat(update(2), rectangle(0, 0, 3, 2, RAW_ENCODING), pixels(3 * 2 * 4), rectangle(1, 1, 0, 0, RAW_ENCODING)));
    assertAtAMessage(session);
  }

  @Test
  void shouldFrameRawRectanglesInTheServersPixelFormatUntilTheClientChoseOne() throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(3));
    session.client(version(3));
    session.server(unsigned32(NONE));
    session.client(new byte[] { SHARE_DESKTOP });
    session.server(serverInitialization(SIDE, SIDE, BITS_PER_PIXEL_16, DESKTOP_NAME));
    session.server(concat(update(1), rectangle(0, 0, 2, 2, RAW_ENCODING), pixels(2 * 2 * 2)));
    assertAtAMessage(session);
  }

  @Test
  void shouldIgnoreAPixelFormatOfAnUnsupportedWidthFromTheClient() throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(3));
    session.client(version(3));
    session.server(unsigned32(NONE));
    session.client(new byte[] { SHARE_DESKTOP });
    session.server(serverInitialization(SIDE, SIDE, BITS_PER_PIXEL_8, DESKTOP_NAME));
    session.client(setPixelFormat(24));
    session.server(concat(update(1), rectangle(0, 0, 2, 2, RAW_ENCODING), pixels(2 * 2)));
    assertAtAMessage(session);
  }

  @Test
  void shouldFrameCopyRectangles() throws IOException {
    final RfbSession session = connected();
    session.server(concat(update(1), rectangle(0, 0, 8, 8, COPY_RECTANGLE_ENCODING), unsigned16(8), unsigned16(8)));
    assertAtAMessage(session);
  }

  @Test
  void shouldFrameRreRectangles() throws IOException {
    final RfbSession session = connected();
    session.server(concat(update(1), rectangle(0, 0, 4, 4, RRE_ENCODING), unsigned32(2), pixels(4), pixels(2 * (4 + 8))));
    assertAtAMessage(session);
  }

  @Test
  void shouldRefuseMoreRreSubrectanglesThanPixels() throws IOException {
    final RfbSession session = connected();
    assertRefused(session, concat(update(1), rectangle(0, 0, 2, 2, RRE_ENCODING), unsigned32(5), new byte[4]), "RRE subrectangles");
  }

  @Test
  void shouldFrameEveryKindOfHextileTile() throws IOException {
    final RfbSession session = connected();
    final byte[] raw = concat(new byte[] { HEXTILE_RAW }, pixels(16 * 16 * 4));
    final byte[] colours = new byte[] { HEXTILE_BACKGROUND | HEXTILE_FOREGROUND, 0, 0, 0, 0, 0, 0, 0, 0 };
    final byte[] coloured = concat(new byte[] { HEXTILE_SUBRECTANGLES | HEXTILE_COLOURED, 1 }, pixels(4 + 2));
    final byte[] plain = concat(new byte[] { HEXTILE_BACKGROUND | HEXTILE_SUBRECTANGLES, 0, 0, 0, 0, 1 }, pixels(2));
    session.server(concat(update(1), rectangle(0, 0, 20, 17, HEXTILE_ENCODING), raw, colours, coloured, plain));
    session.server(concat(update(1), rectangle(0, 0, 16, 16, HEXTILE_ENCODING), new byte[] { 0 }));
    assertAtAMessage(session);
  }

  @Test
  void shouldFrameZlibRectangles() throws IOException {
    final RfbSession session = connected();
    session.server(concat(update(1), rectangle(0, 0, 4, 4, ZLIB_ENCODING), unsigned32(10), pixels(10)));
    session.server(concat(update(1), rectangle(0, 0, 4, 4, ZLIB_ENCODING), unsigned32(0)));
    assertAtAMessage(session);
  }

  @Test
  void shouldRefuseZlibDataFarLargerThanItsPixels() throws IOException {
    final RfbSession session = connected();
    final long limit = 4L * 4 * 4 + RfbGuard.ZLIB_SLACK;
    assertRefused(session, concat(update(1), rectangle(0, 0, 4, 4, ZLIB_ENCODING), unsigned32(limit + 1)), "zlib data");
  }

  @Test
  void shouldFollowANewDesktopSizeWithinItsBounds() throws IOException {
    final RfbSession session = connected();
    session.server(concat(update(1), rectangle(0, 0, SIDE * 2, SIDE, DESKTOP_SIZE_ENCODING)));
    session.server(concat(update(1), rectangle(SIDE, 0, SIDE, 1, COPY_RECTANGLE_ENCODING), unsigned32(0)));
    assertRefused(session, concat(update(1), rectangle(0, 0, RfbGuard.MAXIMUM_SIDE + 1, SIDE, DESKTOP_SIZE_ENCODING)), "framebuffer");
  }

  @Test
  void shouldFrameCursors() throws IOException {
    final RfbSession session = connected();
    session.server(concat(update(1), rectangle(0, 0, 9, 2, CURSOR_ENCODING), pixels(9 * 2 * 4 + 2 * 2)));
    session.server(concat(update(1), rectangle(0, 0, 0, 0, CURSOR_ENCODING)));
    assertAtAMessage(session);
  }

  @ParameterizedTest
  @ValueSource(ints = { 0, 1 })
  void shouldRefuseACursorAboveItsBound(final int tallInsteadOfWide) throws IOException {
    final RfbSession session = connected();
    final int wide = tallInsteadOfWide == 1 ? 1 : RfbGuard.MAXIMUM_SIDE + 1;
    final int tall = tallInsteadOfWide == 1 ? RfbGuard.MAXIMUM_SIDE + 1 : 1;
    assertRefused(session, concat(update(1), rectangle(0, 0, wide, tall, CURSOR_ENCODING)), "cursor");
  }

  @ParameterizedTest
  @ValueSource(ints = { 0, 1 })
  void shouldRefuseARectangleOutsideTheFramebuffer(final int belowInsteadOfRight) throws IOException {
    final RfbSession session = connected();
    final int left = belowInsteadOfRight == 1 ? 0 : SIDE - 1;
    final int top = belowInsteadOfRight == 1 ? SIDE - 1 : 0;
    assertRefused(session, concat(update(1), rectangle(left, top, 2, 2, RAW_ENCODING)), "outside the framebuffer");
  }

  @Test
  void shouldRefuseAnEncodingTheClientCannotDecode() throws IOException {
    final RfbSession session = connected();
    assertRefused(session, concat(update(1), rectangle(0, 0, 2, 2, 16)), "encoding 16");
  }

  @Test
  void shouldCheckBytesThatArriveOneAtATime() throws IOException {
    final RfbSession session = connected();
    final byte[] update = concat(update(1), rectangle(0, 0, 2, 2, RAW_ENCODING), pixels(2 * 2 * 4), cutText(3));
    for (final byte value : update) {
      session.server(new byte[] { value });
    }
    assertRefused(session, new byte[] { 9 }, "message type 9");
  }

  @Test
  void shouldFollowEveryMessageTheClientSends() throws IOException {
    final RfbSession session = connected();
    session.client(concat(new byte[] { 2, 0 }, unsigned16(2), unsigned32(0)));
    session.client(unsigned32(1));
    session.client(concat(new byte[] { 2, 0 }, unsigned16(0)));
    session.client(concat(new byte[] { 3, 0 }, unsigned16(0), unsigned16(0), unsigned16(SIDE), unsigned16(SIDE)));
    session.client(concat(new byte[] { 4, 1, 0, 0 }, unsigned32(65)));
    session.client(concat(new byte[] { 5, 0 }, unsigned16(1), unsigned16(1)));
    session.client(concat(new byte[] { 6, 0, 0, 0 }, unsigned32(2), new byte[] { 'h', 'i' }));
    session.client(concat(new byte[] { 6, 0, 0, 0 }, unsigned32(0)));
    session.client(setPixelFormat(BITS_PER_PIXEL_16));
    session.server(concat(update(1), rectangle(0, 0, 2, 2, RAW_ENCODING), pixels(2 * 2 * 2)));
    session.client(setPixelFormat(BITS_PER_PIXEL_8));
    session.server(concat(update(1), rectangle(0, 0, 2, 2, RAW_ENCODING), pixels(2 * 2)));
    assertAtAMessage(session);
  }

  @Test
  void shouldStopFollowingAClientThatSendsAnUnknownMessage() throws IOException {
    final RfbSession session = connected();
    session.client(new byte[] { 99, 1, 2, 3 });
    session.client(setPixelFormat(BITS_PER_PIXEL_8));
    session.server(concat(update(1), rectangle(0, 0, 1, 1, RAW_ENCODING), pixels(4)));
    assertAtAMessage(session);
  }

  @ParameterizedTest
  @ValueSource(strings = { "RFB 003.0x8\n", "RFB 003.0 8\n" })
  void shouldStopFollowingAClientVersionThatIsNotANumber(final String clientVersion) throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(MINOR_VERSION_3_8));
    session.client(clientVersion.getBytes(StandardCharsets.US_ASCII));
    assertRefused(session, new byte[] { 1 }, "before the client chose a version");
  }

  @ParameterizedTest
  @CsvSource({ "0, 1", "3, 1", "3, 2", "7, 1", "7, 2", "8, 1", "8, 2", "8, 113", "9, 1", "10, 1" })
  void shouldFollowTheClientThroughEveryHandshakeToItsPixelFormat(final int minor, final int security) throws IOException {
    final RfbSession session = new RfbSession();
    session.handshake(minor, security, SIDE, SIDE, BITS_PER_PIXEL_8);
    session.server(concat(update(1), rectangle(0, 0, 3, 2, RAW_ENCODING), pixels(3 * 2)));
    assertAtAMessage(session);
  }

  @Test
  void shouldFrameAListOfMoreSecurityTypesThanAFieldHolds() throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(MINOR_VERSION_3_8));
    session.client(version(MINOR_VERSION_3_8));
    final byte[] types = new byte[255];
    Arrays.fill(types, (byte) NONE);
    session.server(concat(new byte[] { (byte) types.length }, types));
    session.client(new byte[] { NONE });
    session.server(unsigned32(0));
    session.client(new byte[] { SHARE_DESKTOP });
    session.server(serverInitialization(SIDE, SIDE, BITS_PER_PIXEL_32, DESKTOP_NAME));
    assertAtAMessage(session);
  }

  @Test
  void shouldFollowTheClientPastEveryEncodingItLists() throws IOException {
    final RfbSession session = connected();
    session.client(concat(new byte[] { 2, 0 }, unsigned16(3), pixels(3 * 4)));
    session.client(setPixelFormat(BITS_PER_PIXEL_8));
    session.server(concat(update(1), rectangle(0, 0, 2, 2, RAW_ENCODING), pixels(2 * 2)));
    assertAtAMessage(session);
  }

  @Test
  void shouldFollowServerBytesThatStartPartwayIntoABuffer() throws IOException {
    final RfbGuard guard = new RfbGuard();
    final byte[] version = version(MINOR_VERSION_3_8);
    guard.acceptServer(version, 0, version.length);
    guard.acceptClient(version, 0, version.length);
    guard.acceptServer(new byte[] { 9, 9, 1, NONE }, 2, 2);
    guard.acceptClient(new byte[] { NONE }, 0, 1);
    guard.acceptServer(concat(new byte[] { 9 }, unsigned32(0)), 1, 4);
    guard.acceptClient(new byte[] { SHARE_DESKTOP }, 0, 1);
    final byte[] initialization = serverInitialization(SIDE, SIDE, BITS_PER_PIXEL_32, DESKTOP_NAME);
    guard.acceptServer(initialization, 0, initialization.length);
    guard.acceptServer(new byte[] { BELL }, 0, 1);
    final IOException refused = assertThrows(IOException.class, () -> guard.acceptServer(new byte[] { 9 }, 0, 1));
    final String message = refused.getMessage();
    final boolean named = message.contains("message type 9");
    assertTrue(named, message);
  }

  @Test
  void shouldAcceptAFailureReasonOfItsBound() throws IOException {
    final RfbSession session = new RfbSession();
    session.server(version(MINOR_VERSION_3_8));
    session.client(version(MINOR_VERSION_3_8));
    session.server(concat(new byte[] { 0 }, unsigned32(RfbGuard.MAXIMUM_REASON), pixels(RfbGuard.MAXIMUM_REASON)));
    assertRefused(session, new byte[] { 2 }, "after the connection ended");
  }

  @ParameterizedTest
  @ValueSource(ints = { 0, 1 })
  void shouldAcceptAFramebufferOfOnePixelAcrossAndTheLargestSide(final int tallInsteadOfWide) throws IOException {
    final int width = tallInsteadOfWide == 1 ? 1 : RfbGuard.MAXIMUM_SIDE;
    final int height = tallInsteadOfWide == 1 ? RfbGuard.MAXIMUM_SIDE : 1;
    final RfbSession session = new RfbSession();
    session.handshake(MINOR_VERSION_3_8, NONE, width, height);
    session.server(concat(update(1), rectangle(width - 1, height - 1, 1, 1, RAW_ENCODING), pixels(4)));
    assertAtAMessage(session);
  }

  @ParameterizedTest
  @ValueSource(ints = { 0, 1 })
  void shouldAcceptACursorOfTheLargestSide(final int tallInsteadOfWide) throws IOException {
    final RfbSession session = connected();
    final int wide = tallInsteadOfWide == 1 ? 1 : RfbGuard.MAXIMUM_SIDE;
    final int tall = tallInsteadOfWide == 1 ? RfbGuard.MAXIMUM_SIDE : 1;
    session.server(concat(update(1), rectangle(0, 0, wide, tall, CURSOR_ENCODING), pixels(wide * tall * 4 + ((wide + 7) / 8) * tall)));
    assertAtAMessage(session);
  }

  @Test
  void shouldAcceptZlibDataUpToItsBound() throws IOException {
    final RfbSession session = connected();
    final int limit = 16 * 16 * 4 + RfbGuard.ZLIB_SLACK;
    session.server(concat(update(1), rectangle(0, 0, 16, 16, ZLIB_ENCODING), unsigned32(limit), pixels(limit)));
    assertAtAMessage(session);
  }

  @Test
  void shouldCheckEveryByteReadOneAtATimeThroughItsStream() throws IOException {
    final RfbSession session = connected();
    final byte[] bytes = concat(update(1), rectangle(0, 0, 1, 1, RAW_ENCODING), pixels(4), new byte[] { 9 });
    final InputStream input = session.guard().serverInput(new ByteArrayInputStream(bytes));
    for (int offset = 0; offset < bytes.length - 1; offset++) {
      final int expected = bytes[offset] & 0xFF;
      final int read = input.read();
      assertEquals(expected, read);
    }

    final IOException refused = assertThrows(IOException.class, input::read);
    final String message = refused.getMessage();
    final boolean named = message.contains("message type 9");
    assertTrue(named, message);
  }

  @Test
  void shouldPassTheBytesThroughItsStreamsUnchanged() throws IOException {
    final RfbGuard guard = new RfbGuard();
    final byte[] version = version(MINOR_VERSION_3_8);
    final ByteArrayOutputStream sent = new ByteArrayOutputStream();
    final OutputStream output = guard.clientOutput(sent);
    final InputStream input = guard.serverInput(new ByteArrayInputStream(concat(version, new byte[] { 1, NONE })));
    final byte[] read = input.readNBytes(version.length);
    output.write(version);
    output.write(NONE);
    assertArrayEquals(version, read);

    final int count = input.read();
    final int type = input.read();
    final int end = input.read();
    final int endOfBuffer = input.read(new byte[4], 0, 4);
    assertEquals(1, count);
    assertEquals(NONE, type);
    assertEquals(-1, end);
    assertEquals(-1, endOfBuffer);

    final byte[] expected = concat(version, new byte[] { NONE });
    final byte[] written = sent.toByteArray();
    final boolean markSupported = input.markSupported();
    assertArrayEquals(expected, written);
    assertFalse(markSupported);
  }

  @Test
  void shouldSkipByReadingThroughTheGuard() throws IOException {
    final RfbGuard guard = new RfbGuard();
    final InputStream input = guard.serverInput(new ByteArrayInputStream(concat(version(MINOR_VERSION_3_8), new byte[] { 1 })));
    final long skipped = input.skip(12);
    assertEquals(12, skipped);

    final IOException refused = assertThrows(IOException.class, () -> input.skip(1));
    final String message = refused.getMessage();
    final boolean named = message.contains("before the client chose a version");
    assertTrue(named, message);

    final long skippedNothing = input.skip(-1);
    assertEquals(0, skippedNothing);
  }
}
