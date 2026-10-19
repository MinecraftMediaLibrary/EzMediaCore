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
package me.brandonli.mcav.vm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * The part of the RFB protocol that the audio connection to QEMU's VNC server speaks, with QEMU's audio extension.
 *
 * <p>The connection is a second, shared client of the display: it agrees on RFB 3.8 with the password of the display
 * (VNC authentication) or, if the server asks for none, without authentication, asks for
 * the audio pseudo-encoding ({@value #AUDIO_ENCODING}), which QEMU confirms with an empty rectangle of that encoding,
 * sets the format of the samples and enables the audio. It never asks for the picture, so QEMU sends it none. From
 * then on QEMU sends a begin and an end whenever the guest starts and stops playing, and the samples in between.
 *
 * <p>The reader is strict and bounded: every length it reads has a limit that is checked before anything is
 * allocated, a message it does not expect ends the connection with a {@link ProtocolException}, and the few messages
 * a VNC server may send unasked, a bell, a change of the colour map and the clipboard, are skipped.
 */
final class QemuAudioProtocol {

  /**
   * The pseudo-encoding of QEMU's audio extension.
   */
  static final int AUDIO_ENCODING = -259;

  /**
   * The sample format QEMU is asked for: signed 16 bits.
   */
  private static final int FORMAT_S16 = 3;

  /**
   * The longest audio message accepted, in bytes; QEMU sends about 10 ms at a time.
   */
  static final int MAXIMUM_AUDIO_BYTES = 64 * 1024;

  /**
   * The longest text a VNC server may send as a reason or a name, in bytes.
   */
  static final int MAXIMUM_TEXT_BYTES = 4096;

  /**
   * The longest clipboard text that is skipped, in bytes.
   */
  static final int MAXIMUM_CUT_TEXT_BYTES = 1024 * 1024;

  /**
   * The most rectangles one update may announce.
   */
  static final int MAXIMUM_RECTANGLES = 16;

  static final int SERVER_UPDATE = 0;
  static final int SERVER_COLOUR_MAP = 1;
  static final int SERVER_BELL = 2;
  static final int SERVER_CUT_TEXT = 3;
  static final int SERVER_QEMU = 255;
  static final int QEMU_AUDIO = 1;
  private static final int AUDIO_END = 0;
  private static final int AUDIO_BEGIN = 1;
  static final int AUDIO_DATA = 2;

  private static final String VERSION = "RFB 003.008\n";
  private static final int VERSION_BYTES = 12;
  private static final Pattern SERVER_VERSION = Pattern.compile("RFB \\d{3}\\.\\d{3}\n");
  private static final int SECURITY_NONE = 1;
  private static final int SECURITY_VNC = 2;
  private static final int CHALLENGE_BYTES = 16;
  // VNC authentication takes the first eight characters of the password as the DES key
  private static final int KEY_BYTES = 8;
  private static final int DES_BLOCK_BYTES = 8;
  // CBC from a zero vector encrypts a single block as ECB, the mode VNC authentication uses
  private static final String CIPHER = "DES/CBC/NoPadding";
  private static final int CLIENT_SET_ENCODINGS = 2;
  private static final int CLIENT_QEMU = 255;
  private static final int CLIENT_AUDIO_ENABLE = 0;
  private static final int CLIENT_AUDIO_SET_FORMAT = 2;
  private static final int PIXEL_FORMAT_BYTES = 16;
  private static final int COLOUR_BYTES = 6;
  private static final int RECTANGLE_BYTES = 8;
  private static final int SKIP_BUFFER_BYTES = 8192;

  private static final String REFUSED_MESSAGE = "VNC server refused the connection because of \"%s\"!";

  private QemuAudioProtocol() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated!");
  }

  /**
   * Reads the version of the server, which must speak RFB 3.8 or later.
   *
   * @param input the stream from the server
   * @throws IOException if the stream ends or the server speaks an older version or no RFB at all
   */
  static void readVersion(final DataInput input) throws IOException {
    final byte[] bytes = new byte[VERSION_BYTES];
    input.readFully(bytes);
    final String version = new String(bytes, StandardCharsets.US_ASCII);
    if (!SERVER_VERSION.matcher(version).matches()) {
      final String message = "VNC server does not speak RFB!";
      throw new ProtocolException(message);
    }
    final int major = Integer.parseInt(version.substring(4, 7));
    final int minor = Integer.parseInt(version.substring(8, 11));
    if (major < 3 || (major == 3 && minor < 8)) {
      final String message = "VNC server speaks RFB %s.%s instead of RFB 3.8 or later!".formatted(major, minor);
      throw new ProtocolException(message);
    }
  }

  /**
   * Answers with RFB 3.8.
   *
   * @param output the stream to the server
   * @throws IOException if the stream fails
   */
  static void writeVersion(final DataOutput output) throws IOException {
    output.write(VERSION.getBytes(StandardCharsets.US_ASCII));
  }

  /**
   * Reads the security types of the server and chooses one: VNC authentication when the server offers it and a
   * password is known, otherwise no authentication, which the server must then offer.
   *
   * @param input    the stream from the server
   * @param output   the stream to the server
   * @param password the password of the display, or an empty string for none
   * @return true if VNC authentication was chosen, whose challenge {@link #answerChallenge} answers next
   * @throws IOException if the stream ends, the server refuses the connection, or it asks for an authentication the
   *                     connection cannot do
   */
  static boolean negotiateSecurity(final DataInput input, final DataOutput output, final String password) throws IOException {
    final int count = input.readUnsignedByte();
    if (count == 0) {
      final String reason = readText(input);
      final String message = REFUSED_MESSAGE.formatted(reason);
      throw new ProtocolException(message);
    }
    boolean withoutAuthentication = false;
    boolean withPassword = false;
    for (int index = 0; index < count; index++) {
      final int type = input.readUnsignedByte();
      withoutAuthentication = withoutAuthentication || type == SECURITY_NONE;
      withPassword = withPassword || type == SECURITY_VNC;
    }
    final boolean authenticate = withPassword && !password.isEmpty();
    if (!authenticate && !withoutAuthentication) {
      final String message = "VNC server asks for authentication, which the audio connection does not do!";
      throw new ProtocolException(message);
    }
    output.writeByte(authenticate ? SECURITY_VNC : SECURITY_NONE);
    return authenticate;
  }

  /**
   * Answers the challenge of VNC authentication with the password (RFC 6143, 7.2.2).
   *
   * @param input    the stream from the server
   * @param output   the stream to the server
   * @param password the password of the display
   * @throws IOException if the stream ends, or DES is missing from the Java runtime
   */
  static void answerChallenge(final DataInput input, final DataOutput output, final String password) throws IOException {
    final byte[] challenge = new byte[CHALLENGE_BYTES];
    input.readFully(challenge);
    final byte[] response = respond(challenge, password);
    output.write(response);
  }

  /**
   * Encrypts the challenge with DES, block by block. The key is the first eight bytes of the password, padded with
   * zeros, with the bits of every byte reversed.
   *
   * @param challenge the challenge, a multiple of eight bytes
   * @param password  the password
   * @return the response
   * @throws IOException if DES is missing from the Java runtime
   */
  static byte[] respond(final byte[] challenge, final String password) throws IOException {
    final byte[] characters = password.getBytes(StandardCharsets.ISO_8859_1);
    final byte[] key = new byte[KEY_BYTES];
    final int length = Math.min(KEY_BYTES, characters.length);
    for (int index = 0; index < length; index++) {
      key[index] = (byte) (Integer.reverse(characters[index] & 0xFF) >>> 24);
    }
    final byte[] response = new byte[challenge.length];
    try {
      final Cipher cipher = Cipher.getInstance(CIPHER);
      final SecretKeySpec secret = new SecretKeySpec(key, "DES");
      final IvParameterSpec zeros = new IvParameterSpec(new byte[DES_BLOCK_BYTES]);
      for (int block = 0; block < challenge.length; block += DES_BLOCK_BYTES) {
        cipher.init(Cipher.ENCRYPT_MODE, secret, zeros);
        cipher.doFinal(challenge, block, DES_BLOCK_BYTES, response, block);
      }
    } catch (final GeneralSecurityException exception) {
      final String message = "Java runtime cannot answer the password challenge of the VNC server!";
      throw new IOException(message, exception);
    }
    return response;
  }

  /**
   * Reads the result of the security handshake.
   *
   * @param input the stream from the server
   * @throws IOException if the stream ends or the server refused the connection
   */
  static void readSecurityResult(final DataInput input) throws IOException {
    final int result = input.readInt();
    if (result != 0) {
      final String reason = readText(input);
      final String message = REFUSED_MESSAGE.formatted(reason);
      throw new ProtocolException(message);
    }
  }

  /**
   * Asks to share the display with the other clients.
   *
   * @param output the stream to the server
   * @throws IOException if the stream fails
   */
  static void writeClientInitialization(final DataOutput output) throws IOException {
    output.writeByte(1);
  }

  /**
   * Reads the description of the display, which the audio connection does not need.
   *
   * @param input the stream from the server
   * @throws IOException if the stream ends or the name of the display is longer than allowed
   */
  static void readServerInitialization(final DataInput input) throws IOException {
    skipFully(input, 4 + PIXEL_FORMAT_BYTES);
    readText(input);
  }

  /**
   * Asks for the audio pseudo-encoding, and nothing else.
   *
   * @param output the stream to the server
   * @throws IOException if the stream fails
   */
  static void writeSetEncodings(final DataOutput output) throws IOException {
    output.writeByte(CLIENT_SET_ENCODINGS);
    output.writeByte(0);
    output.writeShort(1);
    output.writeInt(AUDIO_ENCODING);
  }

  /**
   * Sets the format of the samples and enables the audio.
   *
   * @param output     the stream to the server
   * @param channels   the number of channels, 1 or 2
   * @param sampleRate the sample rate in hertz
   * @throws IOException if the stream fails
   */
  static void writeEnableAudio(final DataOutput output, final int channels, final int sampleRate) throws IOException {
    output.writeByte(CLIENT_QEMU);
    output.writeByte(QEMU_AUDIO);
    output.writeShort(CLIENT_AUDIO_SET_FORMAT);
    output.writeByte(FORMAT_S16);
    output.writeByte(channels);
    output.writeInt(sampleRate);
    output.writeByte(CLIENT_QEMU);
    output.writeByte(QEMU_AUDIO);
    output.writeShort(CLIENT_AUDIO_ENABLE);
  }

  /**
   * Reads one message of the server.
   *
   * @param input     the stream from the server
   * @param frameSize the bytes of one frame of samples; the samples of a message are whole frames
   * @param buffers   gives a buffer of at least the asked size for the samples of a message
   * @return what the message said
   * @throws IOException if the stream ends or the message is not one the audio connection expects
   */
  static Message readMessage(final DataInput input, final int frameSize, final IntFunction<byte[]> buffers) throws IOException {
    final int type = input.readUnsignedByte();
    return switch (type) {
      case SERVER_UPDATE -> readUpdate(input);
      case SERVER_COLOUR_MAP -> {
        skipFully(input, 1 + 2);
        final int colours = input.readUnsignedShort();
        skipFully(input, (long) colours * COLOUR_BYTES);
        yield Message.IGNORED;
      }
      case SERVER_BELL -> Message.IGNORED;
      case SERVER_CUT_TEXT -> {
        skipFully(input, 3);
        final long length = Integer.toUnsignedLong(input.readInt());
        if (length > MAXIMUM_CUT_TEXT_BYTES) {
          final String message = "VNC server sent %s bytes of clipboard text, more than the %s bytes allowed!".formatted(
            length,
            MAXIMUM_CUT_TEXT_BYTES
          );
          throw new ProtocolException(message);
        }
        skipFully(input, length);
        yield Message.IGNORED;
      }
      case SERVER_QEMU -> readQemu(input, frameSize, buffers);
      default -> {
        final String message = "VNC server sent message type %s, which the audio connection does not expect!".formatted(type);
        throw new ProtocolException(message);
      }
    };
  }

  private static Message readUpdate(final DataInput input) throws IOException {
    skipFully(input, 1);
    final int rectangles = input.readUnsignedShort();
    if (rectangles > MAXIMUM_RECTANGLES) {
      final String message = "VNC server announced %s rectangles, more than the %s allowed!".formatted(rectangles, MAXIMUM_RECTANGLES);
      throw new ProtocolException(message);
    }
    for (int index = 0; index < rectangles; index++) {
      skipFully(input, RECTANGLE_BYTES);
      final int encoding = input.readInt();
      if (encoding != AUDIO_ENCODING) {
        final String message = "VNC server sent a rectangle of encoding %s, which the audio connection never asks for!".formatted(encoding);
        throw new ProtocolException(message);
      }
    }
    return rectangles == 0 ? Message.IGNORED : Message.ACKNOWLEDGED;
  }

  private static Message readQemu(final DataInput input, final int frameSize, final IntFunction<byte[]> buffers) throws IOException {
    final int submessage = input.readUnsignedByte();
    if (submessage != QEMU_AUDIO) {
      final String message = "VNC server sent QEMU message %s instead of audio!".formatted(submessage);
      throw new ProtocolException(message);
    }
    final int operation = input.readUnsignedShort();
    return switch (operation) {
      case AUDIO_END -> Message.END;
      case AUDIO_BEGIN -> Message.BEGIN;
      case AUDIO_DATA -> {
        final long length = Integer.toUnsignedLong(input.readInt());
        if (length > MAXIMUM_AUDIO_BYTES || length % frameSize != 0) {
          final String message = "VNC server sent %s bytes of audio, not whole frames of at most %s bytes!".formatted(
            length,
            MAXIMUM_AUDIO_BYTES
          );
          throw new ProtocolException(message);
        }
        final int size = (int) length;
        final byte[] buffer = buffers.apply(size);
        input.readFully(buffer, 0, size);
        yield Message.data(buffer, size);
      }
      default -> {
        final String message = "VNC server sent audio operation %s, which the audio connection does not know!".formatted(operation);
        throw new ProtocolException(message);
      }
    };
  }

  private static String readText(final DataInput input) throws IOException {
    final long length = Integer.toUnsignedLong(input.readInt());
    if (length > MAXIMUM_TEXT_BYTES) {
      final String message = "VNC server sent a text of %s bytes, more than the %s bytes allowed!".formatted(length, MAXIMUM_TEXT_BYTES);
      throw new ProtocolException(message);
    }
    final byte[] bytes = new byte[(int) length];
    input.readFully(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  /**
   * Skips bytes, failing like a read when the stream ends before them.
   *
   * @param input the stream
   * @param count the number of bytes
   * @throws IOException if the stream ends or fails
   */
  private static void skipFully(final DataInput input, final long count) throws IOException {
    final byte[] scratch = new byte[(int) Math.min(count, SKIP_BUFFER_BYTES)];
    long left = count;
    while (left > 0) {
      final int chunk = (int) Math.min(left, scratch.length);
      input.readFully(scratch, 0, chunk);
      left -= chunk;
    }
  }

  /**
   * What one message of the server said.
   */
  static final class Message {

    static final Message IGNORED = new Message(Kind.IGNORED, new byte[0], 0);
    static final Message ACKNOWLEDGED = new Message(Kind.ACKNOWLEDGED, new byte[0], 0);
    static final Message BEGIN = new Message(Kind.BEGIN, new byte[0], 0);
    static final Message END = new Message(Kind.END, new byte[0], 0);

    private final Kind kind;
    private final byte[] samples;
    private final int length;

    private Message(final Kind kind, final byte[] samples, final int length) {
      this.kind = kind;
      this.samples = samples;
      this.length = length;
    }

    static Message data(final byte[] samples, final int length) {
      return new Message(Kind.DATA, samples, length);
    }

    Kind getKind() {
      return this.kind;
    }

    /**
     * Gets the buffer that holds the samples of a data message, which the reader may reuse for the next message.
     *
     * @return the buffer
     */
    byte[] getSamples() {
      return this.samples;
    }

    /**
     * Gets the number of sample bytes of a data message.
     *
     * @return the length, 0 for every other message
     */
    int getLength() {
      return this.length;
    }
  }

  /**
   * The kinds of messages.
   */
  enum Kind {
    /**
     * A message the audio connection skips.
     */
    IGNORED,
    /**
     * QEMU confirmed that it sends audio.
     */
    ACKNOWLEDGED,
    /**
     * The guest started to play.
     */
    BEGIN,
    /**
     * The guest stopped playing.
     */
    END,
    /**
     * Samples.
     */
    DATA,
  }
}
