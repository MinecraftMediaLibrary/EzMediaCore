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
package me.brandonli.mcav.utils.http;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import org.junit.jupiter.api.Tag;

@Tag("fuzz")
final class IdleTimeoutInputStreamFuzzTest {

  private static final Duration GENEROUS = Duration.ofSeconds(30);

  @FuzzTest(maxDuration = "30s")
  void shouldDeliverEveryByteOfTheSourceInOrder(final FuzzedDataProvider data) throws IOException {
    final byte[] content = data.consumeBytes(data.consumeInt(0, 4096));
    final int[] pieces = data.consumeInts(16);
    final PiecewiseStream source = new PiecewiseStream(content, pieces);
    final ByteArrayOutputStream received = new ByteArrayOutputStream();
    try (final InputStream stream = new IdleTimeoutInputStream(source, GENEROUS)) {
      int reads = 0;
      int count = 0;
      while (count != -1 && data.remainingBytes() > 0 && reads < 10_000) {
        reads++;
        count = this.readRandomRange(data, stream, received);
      }

      final byte[] rest = stream.readAllBytes();
      received.write(rest);
    }

    final byte[] delivered = received.toByteArray();
    assertArrayEquals(content, delivered, "every byte of the source, once and in order");
  }

  private int readRandomRange(final FuzzedDataProvider data, final InputStream stream, final ByteArrayOutputStream received)
    throws IOException {
    final int length = data.consumeInt(-2, 300);
    final int offset = data.consumeInt(-2, 300);
    final byte[] buffer = new byte[data.consumeInt(0, 300)];
    final boolean valid = offset >= 0 && length >= 0 && offset + length <= buffer.length;
    final int count;
    try {
      count = stream.read(buffer, offset, length);
    } catch (final IndexOutOfBoundsException exception) {
      assertFalse(valid, "a valid range was refused");

      return 0;
    }

    assertTrue(valid, () -> "an invalid range was accepted: offset " + offset + ", length " + length + " of " + buffer.length);

    if (count == -1) {
      return count;
    }

    final boolean withinRange = count >= 0 && count <= length;
    assertTrue(withinRange, () -> count + " bytes for a request of " + length);

    received.write(buffer, offset, count);

    return count;
  }

  private static final class PiecewiseStream extends InputStream {

    private final byte[] content;
    private final int[] pieces;
    private int position;
    private int piece;

    PiecewiseStream(final byte[] content, final int[] pieces) {
      this.content = content;
      this.pieces = pieces;
    }

    @Override
    public int read() {
      if (this.position >= this.content.length) {
        return -1;
      }

      final byte next = this.content[this.position];
      this.position++;

      return next & 0xFF;
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int length) {
      if (length == 0) {
        return 0;
      }

      if (this.position >= this.content.length) {
        return -1;
      }

      final int wanted = this.pieces.length == 0 ? length : 1 + Math.floorMod(this.pieces[this.piece % this.pieces.length], length);
      this.piece++;
      final int count = Math.min(wanted, this.content.length - this.position);
      System.arraycopy(this.content, this.position, buffer, offset, count);
      this.position += count;

      return count;
    }
  }
}
