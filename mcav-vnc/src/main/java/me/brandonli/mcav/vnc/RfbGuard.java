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

import com.google.common.base.Preconditions;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Checks what a VNC server sends before the VNC client reads it. The client library allocates what the server's
 * length fields name - the desktop name, the clipboard text, the framebuffer - without a bound, so a hostile server
 * could make the Minecraft server's JVM allocate gigabytes. The guard follows the protocol (RFB 3.3, 3.7 and 3.8, RFC
 * 6143) byte by byte on both directions of the connection, and checks every length and size before the client sees
 * it: a length above its bound, a message type or encoding the client cannot decode anyway, or bytes where the
 * protocol allows none end the connection with an {@link IOException} instead of reaching the client.
 *
 * <p>It frames the security types the client supports (none, VNC authentication and MS-Logon II), and the server
 * messages and rectangle encodings the client decodes (raw, copy rectangle, RRE, hextile, zlib, and the desktop size
 * and cursor pseudo-encodings). The client's own messages are only read to learn the version and security type it
 * chose and the pixel format it asked for, which decide how the server's messages are framed.
 */
final class RfbGuard {

  /** The largest framebuffer side accepted: a 4K desktop fits, and the client's picture stays under 64 MiB. */
  static final int MAXIMUM_SIDE = 4096;

  /** The longest desktop name accepted, in bytes. */
  static final int MAXIMUM_NAME = 4096;

  /** The longest failure reason accepted, in bytes. */
  static final int MAXIMUM_REASON = 4096;

  /** The longest clipboard text accepted, in bytes; the client does not use it. */
  static final int MAXIMUM_TEXT = 1 << 20;

  /** Compressed zlib data may exceed what it decompresses to by a little; this is the room allowed. */
  static final int ZLIB_SLACK = 1 << 16;

  private static final int VERSION_BYTES = 12;
  private static final String VERSION_PREFIX = "RFB 003.";
  private static final char VERSION_TERMINATOR = '\n';

  private static final int BYTE_MASK = 0xFF;

  private static final Set<Integer> SUPPORTED_BITS_PER_PIXEL = Set.of(8, 16, 32);

  private static final int UNKNOWN = -1;

  private static final int NONE = 1;

  private static final int VNC = 2;

  private static final int MS_LOGON = 113;

  private static final int MINIMUM_MINOR_WITH_TYPE_LIST = 7;

  private static final int MINIMUM_MINOR_WITH_REASONS = 8;

  private final Object lock = new Object();

  private final ServerParser server = new ServerParser();

  private final ClientParser client = new ClientParser();

  private int minorVersion = UNKNOWN;

  private int securityType = UNKNOWN;

  private int clientBytesPerPixel = UNKNOWN;

  /**
   * Wraps the stream the server's bytes arrive on.
   *
   * @param input the stream of the connection
   * @return the stream the client reads, which fails instead of passing on a byte the protocol does not allow there
   */
  InputStream serverInput(final InputStream input) {
    Preconditions.checkNotNull(input, "Input must not be null!");
    return new GuardedInput(input);
  }

  /**
   * Wraps the stream the client's bytes leave on.
   *
   * @param output the stream of the connection
   * @return the stream the client writes, which passes every byte on unchanged
   */
  OutputStream clientOutput(final OutputStream output) {
    Preconditions.checkNotNull(output, "Output must not be null!");
    return new ObservedOutput(output);
  }

  /**
   * Checks bytes the server sent, in order.
   *
   * @throws IOException if they break the protocol or a bound
   */
  void acceptServer(final byte[] buffer, final int offset, final int length) throws IOException {
    synchronized (this.lock) {
      this.server.accept(buffer, offset, length);
    }
  }

  /** Reads bytes the client sent, in order. */
  void acceptClient(final byte[] buffer, final int offset, final int length) {
    synchronized (this.lock) {
      this.client.accept(buffer, offset, length);
    }
  }

  private int bytesPerPixel() {
    return this.clientBytesPerPixel > 0 ? this.clientBytesPerPixel : this.server.serverBytesPerPixel;
  }

  private static IOException violation(final String what) {
    final String message = "VNC server sent %s, which breaks the protocol or a bound!".formatted(what);
    return new IOException(message);
  }

  private static int unsigned8(final byte[] field, final int at) {
    return field[at] & BYTE_MASK;
  }

  private static int unsigned16(final byte[] field, final int at) {
    return (unsigned8(field, at) << Byte.SIZE) | unsigned8(field, at + 1);
  }

  private static long unsigned32(final byte[] field, final int at) {
    return ((long) unsigned16(field, at) << Short.SIZE) | unsigned16(field, at + Short.BYTES);
  }

  private static int pixelBytes(final int bitsPerPixel) throws IOException {
    if (!SUPPORTED_BITS_PER_PIXEL.contains(bitsPerPixel)) {
      throw violation("%s bits per pixel".formatted(bitsPerPixel));
    }
    return bitsPerPixel / Byte.SIZE;
  }

  private final class GuardedInput extends FilterInputStream {

    private static final int SKIP_BUFFER_BYTES = 8192;

    private GuardedInput(final InputStream input) {
      super(input);
    }

    @Override
    public int read() throws IOException {
      final int value = this.in.read();
      if (value >= 0) {
        final byte[] single = { (byte) value };
        RfbGuard.this.acceptServer(single, 0, single.length);
      }
      return value;
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int length) throws IOException {
      final int count = this.in.read(buffer, offset, length);
      if (count > 0) {
        RfbGuard.this.acceptServer(buffer, offset, count);
      }
      return count;
    }

    @Override
    public long skip(final long count) throws IOException {
      final byte[] buffer = new byte[(int) Math.min(Math.max(count, 0), SKIP_BUFFER_BYTES)];
      return Math.max(this.read(buffer, 0, buffer.length), 0);
    }

    @Override
    public boolean markSupported() {
      return false;
    }
  }

  private final class ObservedOutput extends FilterOutputStream {

    private ObservedOutput(final OutputStream output) {
      super(output);
    }

    @Override
    public void write(final int value) throws IOException {
      final byte[] single = { (byte) value };
      RfbGuard.this.acceptClient(single, 0, single.length);
      this.out.write(value);
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) throws IOException {
      RfbGuard.this.acceptClient(buffer, offset, length);
      this.out.write(buffer, offset, length);
    }
  }

  /** What the server's parser does with the next byte. */
  private enum Mode {
    FIELD,
    SKIP,
    // the server's next byte can only be framed once the client answered
    AWAIT_VERSION,
    AWAIT_CHOICE,
    CLOSED,
  }

  /** A field of the server's side of the protocol, with its length in bytes. */
  private enum Server {
    VERSION(VERSION_BYTES),
    SECURITY_TYPE(Integer.BYTES),
    SECURITY_COUNT(1),
    // one byte per type: the count read before it gives the real length
    SECURITY_TYPES(1),
    REASON_LENGTH(Integer.BYTES),
    CHALLENGE(16),
    // MS-Logon II: the generator, the modulus and the server's public key
    LOGON_KEYS(24),
    RESULT(Integer.BYTES),
    // the framebuffer's size, its pixel format and the length of its name
    INITIALIZATION(24),
    MESSAGE(1),
    // padding and the number of rectangles
    UPDATE(3),
    // position, size and encoding
    RECTANGLE(12),
    // the number of subrectangles, then the background pixel
    RRE(Integer.BYTES),
    TILE(1),
    SUBRECTANGLES(1),
    ZLIB_LENGTH(Integer.BYTES),
    // padding, the first colour and the number of colours
    COLOURS(5),
    // padding and the length of the text
    TEXT(7);

    private final int bytes;

    Server(final int bytes) {
      this.bytes = bytes;
    }
  }

  /** What the server's parser does when a field is complete. */
  @FunctionalInterface
  private interface Step {
    void run() throws IOException;
  }

  /** What follows a run of skipped bytes. */
  private enum Then {
    STATE,
    RECTANGLE,
    TILE,
    CLOSE,
  }

  /** Follows the server's side of the protocol. */
  private final class ServerParser {

    private static final int COLOUR_ENTRY = 6;
    private static final int SUBRECTANGLE_GEOMETRY = 8;
    private static final int HEXTILE_GEOMETRY = 2;
    private static final int TILE_SIDE = 16;
    private static final int RAW = 0;
    private static final int COPY_RECTANGLE = 1;
    private static final int RRE_ENCODING = 2;
    private static final int HEXTILE = 5;
    private static final int ZLIB = 6;
    private static final int DESKTOP_SIZE = -223;
    private static final int CURSOR = -239;
    private static final int TILE_RAW = 1;
    private static final int TILE_BACKGROUND = 2;
    private static final int TILE_FOREGROUND = 4;
    private static final int TILE_SUBRECTANGLES = 8;
    private static final int TILE_COLOURED = 16;

    private static final int FRAMEBUFFER_UPDATE = 0;
    private static final int SET_COLOUR_MAP_ENTRIES = 1;
    private static final int BELL = 2;
    private static final int SERVER_CUT_TEXT = 3;

    private static final int SECURITY_TYPE_INVALID = 0;
    private static final int SECURITY_RESULT_OK = 0;

    private static final int FRAMEBUFFER_WIDTH_OFFSET = 0;
    private static final int FRAMEBUFFER_HEIGHT_OFFSET = 2;
    private static final int BITS_PER_PIXEL_OFFSET = 4;
    private static final int NAME_LENGTH_OFFSET = 20;

    private static final int RECTANGLE_COUNT_OFFSET = 1;

    private static final int RECTANGLE_LEFT_OFFSET = 0;
    private static final int RECTANGLE_TOP_OFFSET = 2;
    private static final int RECTANGLE_WIDTH_OFFSET = 4;
    private static final int RECTANGLE_HEIGHT_OFFSET = 6;
    private static final int RECTANGLE_ENCODING_OFFSET = 8;

    private static final int COLOUR_COUNT_OFFSET = 3;
    private static final int TEXT_LENGTH_OFFSET = 3;

    private static final int INITIAL_BYTES_PER_PIXEL = 4;

    private final byte[] field = new byte[Server.INITIALIZATION.bytes];

    private Mode mode = Mode.FIELD;

    private Server state = Server.VERSION;

    private int need = Server.VERSION.bytes;

    private int have;

    private long skip;

    private Then then = Then.STATE;

    private Server skipTo = Server.MESSAGE;

    private int serverBytesPerPixel = INITIAL_BYTES_PER_PIXEL;

    private int width;

    private int height;

    private int rectangles;

    private int rectangleWidth;

    private int rectangleHeight;

    private int tile;

    private int tileFlags;

    void accept(final byte[] buffer, final int offset, final int length) throws IOException {
      int at = offset;
      final int end = offset + length;
      while (at < end) {
        at = switch (this.mode) {
          case FIELD -> this.collect(buffer, at);
          case SKIP -> this.skipFrom(at, end);
          case AWAIT_VERSION -> this.afterVersion(at);
          case AWAIT_CHOICE -> this.afterChoice(at);
          case CLOSED -> throw violation("bytes after the connection ended");
        };
      }
    }

    private int collect(final byte[] buffer, final int at) throws IOException {
      if (this.have < this.field.length) {
        this.field[this.have] = buffer[at];
      }
      this.have++;
      if (this.have == this.need) {
        this.complete();
      }
      return at + 1;
    }

    private int skipFrom(final int at, final int end) {
      final long taken = Math.min(this.skip, end - at);
      this.skip -= taken;
      if (this.skip == 0) {
        this.continueAfterSkip();
      }
      return at + (int) taken;
    }

    private void continueAfterSkip() {
      this.mode = Mode.FIELD;
      final Runnable next = switch (this.then) {
        case RECTANGLE -> this::nextRectangle;
        case TILE -> this::nextTile;
        case STATE -> () -> this.expect(this.skipTo);
        case CLOSE -> this::close;
      };
      next.run();
    }

    private void skip(final long bytes, final Then after, final Server next) {
      this.then = after;
      this.skipTo = next;
      if (bytes == 0) {
        this.continueAfterSkip();
        return;
      }
      this.skip = bytes;
      this.mode = Mode.SKIP;
    }

    private void expect(final Server next) {
      this.state = next;
      this.have = 0;
      this.need = next.bytes + (next == Server.RRE ? RfbGuard.this.bytesPerPixel() : 0);
    }

    /** The connection ends after this field: any byte after it breaks the protocol. */
    private void close() {
      this.mode = Mode.CLOSED;
    }

    private void complete() throws IOException {
      final Step step = switch (this.state) {
        case VERSION -> this::version;
        case SECURITY_TYPE -> this::securityType;
        case SECURITY_COUNT -> this::securityCount;
        case SECURITY_TYPES -> () -> this.mode = Mode.AWAIT_CHOICE;
        case REASON_LENGTH -> this::reason;
        case CHALLENGE, LOGON_KEYS -> () -> this.expect(Server.RESULT);
        case RESULT -> this::result;
        case INITIALIZATION -> this::initialization;
        case MESSAGE -> this::message;
        case UPDATE -> this::update;
        case RECTANGLE -> this::rectangle;
        case RRE -> this::rre;
        case TILE -> this::tile;
        case SUBRECTANGLES -> this::subrectangles;
        case ZLIB_LENGTH -> this::zlib;
        case COLOURS -> () -> this.skip((long) COLOUR_ENTRY * unsigned16(this.field, COLOUR_COUNT_OFFSET), Then.STATE, Server.MESSAGE);
        case TEXT -> () -> this.bounded(unsigned32(this.field, TEXT_LENGTH_OFFSET), MAXIMUM_TEXT, "clipboard text", Server.MESSAGE);
      };
      step.run();
    }

    /** A failure reason, after which the server closes the connection. */
    private void reason() throws IOException {
      final long length = unsigned32(this.field, 0);
      if (length > MAXIMUM_REASON) {
        throw violation("a failure reason of %s bytes".formatted(length));
      }
      this.skip(length, Then.CLOSE, Server.VERSION);
    }

    private void version() throws IOException {
      final String text = new String(this.field, 0, VERSION_BYTES, StandardCharsets.US_ASCII);
      if (!text.startsWith(VERSION_PREFIX) || text.charAt(VERSION_BYTES - 1) != VERSION_TERMINATOR) {
        throw violation("a protocol version other than 3.x");
      }
      this.mode = Mode.AWAIT_VERSION;
    }

    private int afterVersion(final int at) throws IOException {
      final int minor = RfbGuard.this.minorVersion;
      if (minor < 0) {
        throw violation("security data before the client chose a version");
      }
      this.mode = Mode.FIELD;
      this.expect(minor >= MINIMUM_MINOR_WITH_TYPE_LIST ? Server.SECURITY_COUNT : Server.SECURITY_TYPE);
      return at;
    }

    private int afterChoice(final int at) throws IOException {
      this.mode = Mode.FIELD;
      this.afterSecurity(RfbGuard.this.securityType);
      return at;
    }

    private void securityType() throws IOException {
      final long type = unsigned32(this.field, 0);
      if (type == SECURITY_TYPE_INVALID) {
        this.expect(Server.REASON_LENGTH);
        return;
      }
      RfbGuard.this.securityType = (int) Math.min(type, Integer.MAX_VALUE);
      this.afterSecurity(RfbGuard.this.securityType);
    }

    private void securityCount() {
      final int count = unsigned8(this.field, 0);
      if (count == 0) {
        this.expect(Server.REASON_LENGTH);
        return;
      }
      this.expect(Server.SECURITY_TYPES);
      this.need = count;
    }

    private void afterSecurity(final int type) throws IOException {
      if (type < 0) {
        throw violation("data before the client chose a security type");
      }
      switch (type) {
        case NONE -> this.expect(RfbGuard.this.minorVersion >= MINIMUM_MINOR_WITH_REASONS ? Server.RESULT : Server.INITIALIZATION);
        case VNC -> this.expect(Server.CHALLENGE);
        case MS_LOGON -> this.expect(Server.LOGON_KEYS);
        default -> throw violation("security type %s that the client does not support".formatted(type));
      }
    }

    private void result() {
      if (unsigned32(this.field, 0) == SECURITY_RESULT_OK) {
        this.expect(Server.INITIALIZATION);
      } else if (RfbGuard.this.minorVersion >= MINIMUM_MINOR_WITH_REASONS) {
        this.expect(Server.REASON_LENGTH);
      } else {
        this.close();
      }
    }

    private void initialization() throws IOException {
      this.resize(unsigned16(this.field, FRAMEBUFFER_WIDTH_OFFSET), unsigned16(this.field, FRAMEBUFFER_HEIGHT_OFFSET));
      this.serverBytesPerPixel = pixelBytes(unsigned8(this.field, BITS_PER_PIXEL_OFFSET));
      this.bounded(unsigned32(this.field, NAME_LENGTH_OFFSET), MAXIMUM_NAME, "desktop name", Server.MESSAGE);
    }

    private void resize(final int newWidth, final int newHeight) throws IOException {
      if (newWidth < 1 || newHeight < 1 || newWidth > MAXIMUM_SIDE || newHeight > MAXIMUM_SIDE) {
        throw violation("a framebuffer of %sx%s pixels".formatted(newWidth, newHeight));
      }
      this.width = newWidth;
      this.height = newHeight;
    }

    private void bounded(final long length, final int limit, final String what, final Server next) throws IOException {
      if (length > limit) {
        throw violation("a %s of %s bytes".formatted(what, length));
      }
      this.skip(length, Then.STATE, next);
    }

    private void message() throws IOException {
      final int type = unsigned8(this.field, 0);
      switch (type) {
        case FRAMEBUFFER_UPDATE -> this.expect(Server.UPDATE);
        case SET_COLOUR_MAP_ENTRIES -> this.expect(Server.COLOURS);
        case BELL -> this.expect(Server.MESSAGE);
        case SERVER_CUT_TEXT -> this.expect(Server.TEXT);
        default -> throw violation("message type %s".formatted(type));
      }
    }

    private void update() {
      this.rectangles = unsigned16(this.field, RECTANGLE_COUNT_OFFSET);
      this.nextRectangle();
    }

    private void nextRectangle() {
      if (this.rectangles == 0) {
        this.expect(Server.MESSAGE);
        return;
      }
      this.rectangles--;
      this.expect(Server.RECTANGLE);
    }

    private void rectangle() throws IOException {
      final int left = unsigned16(this.field, RECTANGLE_LEFT_OFFSET);
      final int top = unsigned16(this.field, RECTANGLE_TOP_OFFSET);
      final int width = unsigned16(this.field, RECTANGLE_WIDTH_OFFSET);
      final int height = unsigned16(this.field, RECTANGLE_HEIGHT_OFFSET);
      final int encoding = (int) unsigned32(this.field, RECTANGLE_ENCODING_OFFSET);
      final int pixel = RfbGuard.this.bytesPerPixel();
      if (encoding == DESKTOP_SIZE) {
        this.resize(width, height);
        this.nextRectangle();
        return;
      }
      if (encoding == CURSOR) {
        if (width > MAXIMUM_SIDE || height > MAXIMUM_SIDE) {
          throw violation("a cursor of %sx%s pixels".formatted(width, height));
        }
        final long mask = (long) ((width + Byte.SIZE - 1) / Byte.SIZE) * height;
        this.skip((long) width * height * pixel + mask, Then.RECTANGLE, Server.MESSAGE);
        return;
      }
      if (left + width > this.width || top + height > this.height) {
        throw violation("a rectangle outside the framebuffer");
      }
      this.rectangleWidth = width;
      this.rectangleHeight = height;
      switch (encoding) {
        case RAW -> this.skip((long) width * height * pixel, Then.RECTANGLE, Server.MESSAGE);
        case COPY_RECTANGLE -> this.skip(Integer.BYTES, Then.RECTANGLE, Server.MESSAGE);
        case RRE_ENCODING -> this.expect(Server.RRE);
        case HEXTILE -> {
          this.tile = 0;
          this.nextTile();
        }
        case ZLIB -> this.expect(Server.ZLIB_LENGTH);
        default -> throw violation("encoding %s".formatted(encoding));
      }
    }

    private void rre() throws IOException {
      final long count = unsigned32(this.field, 0);
      if (count > (long) this.rectangleWidth * this.rectangleHeight) {
        throw violation("%s RRE subrectangles in %sx%s pixels".formatted(count, this.rectangleWidth, this.rectangleHeight));
      }
      final long each = RfbGuard.this.bytesPerPixel() + (long) SUBRECTANGLE_GEOMETRY;
      this.skip(count * each, Then.RECTANGLE, Server.MESSAGE);
    }

    private int tilesAcross() {
      return (this.rectangleWidth + TILE_SIDE - 1) / TILE_SIDE;
    }

    private int tiles() {
      return this.tilesAcross() * ((this.rectangleHeight + TILE_SIDE - 1) / TILE_SIDE);
    }

    private void nextTile() {
      if (this.tile == this.tiles()) {
        this.nextRectangle();
        return;
      }
      this.expect(Server.TILE);
    }

    private void tile() {
      final int flags = unsigned8(this.field, 0);
      final int pixel = RfbGuard.this.bytesPerPixel();
      final int across = this.tilesAcross();
      final int tileWidth = Math.min(TILE_SIDE, this.rectangleWidth - (this.tile % across) * TILE_SIDE);
      final int tileHeight = Math.min(TILE_SIDE, this.rectangleHeight - (this.tile / across) * TILE_SIDE);
      this.tile++;
      if ((flags & TILE_RAW) != 0) {
        this.skip((long) tileWidth * tileHeight * pixel, Then.TILE, Server.MESSAGE);
        return;
      }
      this.tileFlags = flags;
      final int colours = ((flags & TILE_BACKGROUND) != 0 ? pixel : 0) + ((flags & TILE_FOREGROUND) != 0 ? pixel : 0);
      if ((flags & TILE_SUBRECTANGLES) != 0) {
        this.skip(colours, Then.STATE, Server.SUBRECTANGLES);
        return;
      }
      this.skip(colours, Then.TILE, Server.MESSAGE);
    }

    private void subrectangles() {
      final int count = unsigned8(this.field, 0);
      final int colour = (this.tileFlags & TILE_COLOURED) != 0 ? RfbGuard.this.bytesPerPixel() : 0;
      this.skip((long) count * (colour + HEXTILE_GEOMETRY), Then.TILE, Server.MESSAGE);
    }

    private void zlib() throws IOException {
      final long length = unsigned32(this.field, 0);
      final long limit = (long) this.rectangleWidth * this.rectangleHeight * RfbGuard.this.bytesPerPixel() + ZLIB_SLACK;
      if (length > limit) {
        throw violation("zlib data of %s bytes for %sx%s pixels".formatted(length, this.rectangleWidth, this.rectangleHeight));
      }
      this.skip(length, Then.RECTANGLE, Server.MESSAGE);
    }
  }

  /** What the client's parser does with the next byte. */
  private enum ClientMode {
    FIELD,
    SKIP,
    UNTRACKED,
  }

  /** A field of the client's side of the protocol, as far as the server's framing depends on it. */
  private enum Client {
    VERSION,
    CHOICE,
    HANDSHAKE,
    MESSAGE,
    BODY,
  }

  /** Follows the client's side of the protocol. */
  private final class ClientParser {

    private static final int VNC_RESPONSE = 16;
    private static final int LOGON_RESPONSE = 8 + 256 + 64;
    private static final int CLIENT_INITIALIZATION = 1;
    private static final int SET_PIXEL_FORMAT = 0;
    private static final int SET_ENCODINGS = 2;
    private static final int UPDATE_REQUEST = 3;
    private static final int KEY = 4;
    private static final int POINTER = 5;
    private static final int CUT_TEXT = 6;
    private static final int PIXEL_FORMAT_BODY = 19;
    private static final int ENCODINGS_BODY = 3;
    private static final int UPDATE_REQUEST_BODY = 9;
    private static final int KEY_BODY = 7;
    private static final int POINTER_BODY = 5;
    private static final int CUT_TEXT_BODY = 7;
    private static final int ENCODING_BYTES = 4;

    private static final int MESSAGE_TYPE_BYTES = 1;
    private static final int CHOICE_BYTES = 1;
    private static final int PENDING_LENGTH = 0;
    private static final int UNTRACKED_BODY = -1;

    private static final int VERSION_MINOR_OFFSET = 8;
    private static final char ZERO_DIGIT = '0';
    private static final int MAXIMUM_DIGIT = 9;
    private static final int DECIMAL_RADIX = 10;

    private static final int PIXEL_FORMAT_BITS_OFFSET = 3;
    private static final int ENCODING_COUNT_OFFSET = 1;
    private static final int CUT_TEXT_LENGTH_OFFSET = 3;

    private final byte[] field = new byte[PIXEL_FORMAT_BODY];

    private ClientMode mode = ClientMode.FIELD;

    private Client state = Client.VERSION;

    private int need = VERSION_BYTES;

    private int have;

    private long skip;

    private int type;

    void accept(final byte[] buffer, final int offset, final int length) {
      int at = offset;
      final int end = offset + length;
      while (at < end) {
        at = switch (this.mode) {
          case FIELD -> this.collect(buffer, at);
          case SKIP -> this.skipFrom(at, end);
          case UNTRACKED -> end;
        };
      }
    }

    private int skipFrom(final int at, final int end) {
      final long taken = Math.min(this.skip, end - at);
      this.skip -= taken;
      if (this.skip == 0) {
        this.mode = ClientMode.FIELD;
        this.expect(Client.MESSAGE, MESSAGE_TYPE_BYTES);
      }
      return at + (int) taken;
    }

    private int collect(final byte[] buffer, final int at) {
      if (this.state == Client.HANDSHAKE && this.need == PENDING_LENGTH) {
        this.need = this.response() + CLIENT_INITIALIZATION;
      }
      if (this.have < this.field.length) {
        this.field[this.have] = buffer[at];
      }
      this.have++;
      if (this.have == this.need) {
        this.complete();
      }
      return at + 1;
    }

    private int response() {
      return switch (RfbGuard.this.securityType) {
        case VNC -> VNC_RESPONSE;
        case MS_LOGON -> LOGON_RESPONSE;
        default -> 0;
      };
    }

    private void expect(final Client next, final int bytes) {
      this.state = next;
      this.have = 0;
      this.need = bytes;
    }

    private void complete() {
      final Runnable step = switch (this.state) {
        case VERSION -> this::version;
        case CHOICE -> this::choice;
        case HANDSHAKE -> () -> this.expect(Client.MESSAGE, MESSAGE_TYPE_BYTES);
        case MESSAGE -> this::message;
        case BODY -> this::body;
      };
      step.run();
    }

    private void choice() {
      RfbGuard.this.securityType = unsigned8(this.field, 0);
      this.expect(Client.HANDSHAKE, PENDING_LENGTH);
    }

    private void version() {
      int minor = 0;
      for (int offset = VERSION_MINOR_OFFSET; offset < VERSION_BYTES - 1; offset++) {
        final int digit = this.field[offset] - ZERO_DIGIT;
        if (digit < 0 || digit > MAXIMUM_DIGIT) {
          this.mode = ClientMode.UNTRACKED;
          return;
        }
        minor = minor * DECIMAL_RADIX + digit;
      }
      RfbGuard.this.minorVersion = minor;
      if (minor >= MINIMUM_MINOR_WITH_TYPE_LIST) {
        this.expect(Client.CHOICE, CHOICE_BYTES);
      } else {
        this.expect(Client.HANDSHAKE, PENDING_LENGTH);
      }
    }

    private void message() {
      this.type = unsigned8(this.field, 0);
      final int body = switch (this.type) {
        case SET_PIXEL_FORMAT -> PIXEL_FORMAT_BODY;
        case SET_ENCODINGS -> ENCODINGS_BODY;
        case UPDATE_REQUEST -> UPDATE_REQUEST_BODY;
        case KEY -> KEY_BODY;
        case POINTER -> POINTER_BODY;
        case CUT_TEXT -> CUT_TEXT_BODY;
        default -> UNTRACKED_BODY;
      };
      if (body == UNTRACKED_BODY) {
        this.mode = ClientMode.UNTRACKED;
        return;
      }
      this.expect(Client.BODY, body);
    }

    private void body() {
      switch (this.type) {
        case SET_PIXEL_FORMAT -> {
          // three bytes of padding, then the pixel format, whose first byte is its bits per pixel
          final int bits = unsigned8(this.field, PIXEL_FORMAT_BITS_OFFSET);
          if (SUPPORTED_BITS_PER_PIXEL.contains(bits)) {
            RfbGuard.this.clientBytesPerPixel = bits / Byte.SIZE;
          }
          this.expect(Client.MESSAGE, MESSAGE_TYPE_BYTES);
        }
        case SET_ENCODINGS -> this.skipThenMessage((long) ENCODING_BYTES * unsigned16(this.field, ENCODING_COUNT_OFFSET));
        case CUT_TEXT -> this.skipThenMessage(unsigned32(this.field, CUT_TEXT_LENGTH_OFFSET));
        default -> this.expect(Client.MESSAGE, MESSAGE_TYPE_BYTES);
      }
    }

    private void skipThenMessage(final long bytes) {
      if (bytes == 0) {
        this.expect(Client.MESSAGE, MESSAGE_TYPE_BYTES);
        return;
      }
      this.skip = bytes;
      this.mode = ClientMode.SKIP;
    }
  }
}
