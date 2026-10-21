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

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.Tag;

// the guest can reach the loopback interface of the host through QEMU's user networking, so the server is untrusted
@Tag("fuzz")
final class QemuAudioProtocolFuzzTest {

  private static final int FRAME = 4;
  private static final String PASSWORD = "Pa55word";

  @FuzzTest(maxDuration = "30s")
  void shouldReadTheHandshakeAndTheMessagesOrRefuseThem(final FuzzedDataProvider data) {
    final byte[] bytes = data.consumeRemainingAsBytes();
    final DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes));
    final DataOutputStream output = new DataOutputStream(OutputStream.nullOutputStream());
    try {
      QemuAudioProtocol.readVersion(input);
      final boolean authenticates = QemuAudioProtocol.negotiateSecurity(input, output, PASSWORD);
      if (authenticates) {
        QemuAudioProtocol.answerChallenge(input, output, PASSWORD);
      }

      QemuAudioProtocol.readSecurityResult(input);
      QemuAudioProtocol.readServerInitialization(input);
      while (true) {
        final QemuAudioProtocol.Message message = QemuAudioProtocol.readMessage(input, FRAME, byte[]::new);
        final int length = message.getLength();
        final boolean wholeFramesWithinTheLimit = length >= 0 && length <= QemuAudioProtocol.MAXIMUM_AUDIO_BYTES && length % FRAME == 0;
        assertTrue(wholeFramesWithinTheLimit, () -> "length %s".formatted(length));
      }
    } catch (final IOException exception) {
      // malformed, cut short, or not expected: the connection ends
    }
  }
}
