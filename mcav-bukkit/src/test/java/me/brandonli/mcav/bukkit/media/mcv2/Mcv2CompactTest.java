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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

final class Mcv2CompactTest {

  @Test
  void shouldStoreBothWholePixelVectorsInEveryTenByteRecord() throws Mcv2Exception {
    for (final int motionX : new int[] { -128, -7, 0, 7, 127 }) {
      for (final int motionY : new int[] { -128, -1, 0, 7, 127 }) {
        final byte[] record = { (byte) motionX, (byte) motionY, -128, 127, -1, 0, 0x18, 0x72, 0x4E, 0x5A };
        final byte[] data = Mcv2WireFrames.block(32, 5, 2, record, false);
        final int offset = Mcv2Decoder.parse(data).getLeaf(0).offset();
        final byte storedX = data[offset];
        assertEquals(motionX, storedX);
        final byte storedY = data[offset + 1];
        assertEquals(motionY, storedY);
        final int recordLength = data.length - offset;
        assertEquals(10, recordLength);
        final byte[] storedRecord = Arrays.copyOfRange(data, offset, data.length);
        assertArrayEquals(record, storedRecord);
      }
    }
  }

  @Test
  void shouldRequireExactlyTenBytesForEveryBlockSize() throws Mcv2Exception {
    for (final int size : new int[] { 8, 16, 32 }) {
      for (int length = 0; length <= 20; length++) {
        final byte[] data = Mcv2WireFrames.block(size, 5, 0, new byte[length], false);
        if (length == 10) {
          final int payloadLength = data.length - Mcv2Decoder.parse(data).getPayloadStart();
          assertEquals(10, payloadLength);
        } else {
          assertThrows(Mcv2Exception.class, () -> Mcv2Decoder.parse(data));
        }
      }
    }
  }

  @Test
  void shouldRefuseEveryQuantizerAboveTwo() {
    for (int quantizer = 3; quantizer < 8; quantizer++) {
      final byte[] data = Mcv2WireFrames.block(32, 5, quantizer, new byte[10], false);
      assertThrows(Mcv2Exception.class, () -> Mcv2Decoder.parse(data));
    }
  }
}
