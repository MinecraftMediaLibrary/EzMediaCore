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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Objects;
import me.brandonli.mcav.vm.testing.EqualityAssertions;
import org.junit.jupiter.api.Test;

final class VmSettingsTest {

  private static final int PORT = 5901;
  private static final int WIDTH = 640;
  private static final int HEIGHT = 480;
  private static final int FRAME_RATE = 30;

  private static final int OTHER_PORT = 5902;
  private static final int OTHER_WIDTH = 641;
  private static final int OTHER_HEIGHT = 481;
  private static final int OTHER_FRAME_RATE = 31;

  private static final int MAXIMUM_PORT = 65535;

  @Test
  void shouldKeepTheGivenValues() {
    final VmSettings settings = VmSettings.of(5905, WIDTH, HEIGHT, 25);
    final int port = settings.getPort();
    final int width = settings.getWidth();
    final int height = settings.getHeight();
    final int frameRate = settings.getTargetFrameRate();
    assertEquals(5905, port);
    assertEquals(WIDTH, width);
    assertEquals(HEIGHT, height);
    assertEquals(25, frameRate);
  }

  @Test
  void shouldPickAFreeVncPort() throws IOException {
    final VmSettings settings = VmSettings.of(320, 200, 10);
    final int port = settings.getPort();
    final int width = settings.getWidth();
    final boolean vncRange = port >= VmProcess.FIRST_VNC_PORT && port <= MAXIMUM_PORT;
    assertTrue(vncRange, () -> "port %s".formatted(port));
    assertEquals(320, width);

    final InetAddress loopback = InetAddress.ofLiteral("127.0.0.1");
    try (final ServerSocket socket = new ServerSocket(port, 1, loopback)) {
      final int bound = socket.getLocalPort();
      assertEquals(port, bound);
    }
  }

  @Test
  void shouldAcceptTheLimitsOfEveryRange() {
    final VmSettings lowest = VmSettings.of(VmProcess.FIRST_VNC_PORT, 1, 1, 1);
    final VmSettings highest = VmSettings.of(MAXIMUM_PORT, 1, 1, 1);
    final int lowestPort = lowest.getPort();
    final int highestPort = highest.getPort();
    assertEquals(VmProcess.FIRST_VNC_PORT, lowestPort);
    assertEquals(MAXIMUM_PORT, highestPort);
  }

  @Test
  void shouldRejectInvalidValues() {
    assertThrows(IllegalArgumentException.class, () -> VmSettings.of(VmProcess.FIRST_VNC_PORT - 1, WIDTH, HEIGHT, FRAME_RATE));
    assertThrows(IllegalArgumentException.class, () -> VmSettings.of(MAXIMUM_PORT + 1, WIDTH, HEIGHT, FRAME_RATE));
    assertThrows(IllegalArgumentException.class, () -> VmSettings.of(VmProcess.FIRST_VNC_PORT, 0, HEIGHT, FRAME_RATE));
    assertThrows(IllegalArgumentException.class, () -> VmSettings.of(VmProcess.FIRST_VNC_PORT, WIDTH, 0, FRAME_RATE));
    assertThrows(IllegalArgumentException.class, () -> VmSettings.of(VmProcess.FIRST_VNC_PORT, WIDTH, HEIGHT, 0));
    assertThrows(IllegalArgumentException.class, () -> VmSettings.of(0, HEIGHT, FRAME_RATE));
    assertThrows(IllegalArgumentException.class, () -> VmSettings.of(WIDTH, 0, FRAME_RATE));
    assertThrows(IllegalArgumentException.class, () -> VmSettings.of(WIDTH, HEIGHT, 0));
  }

  @Test
  void shouldCompareEveryValue() {
    final VmSettings settings = VmSettings.of(PORT, WIDTH, HEIGHT, FRAME_RATE);
    final VmSettings equal = VmSettings.of(PORT, WIDTH, HEIGHT, FRAME_RATE);
    final VmSettings otherPort = VmSettings.of(OTHER_PORT, WIDTH, HEIGHT, FRAME_RATE);
    final VmSettings otherWidth = VmSettings.of(PORT, OTHER_WIDTH, HEIGHT, FRAME_RATE);
    final VmSettings otherHeight = VmSettings.of(PORT, WIDTH, OTHER_HEIGHT, FRAME_RATE);
    final VmSettings otherFrameRate = VmSettings.of(PORT, WIDTH, HEIGHT, OTHER_FRAME_RATE);
    EqualityAssertions.assertEqualityContract(settings, equal, otherPort, otherWidth, otherHeight, otherFrameRate);
  }

  @Test
  void shouldBuildTheHashCodeFromEveryValueInOrder() {
    final VmSettings settings = VmSettings.of(PORT, WIDTH, HEIGHT, FRAME_RATE);
    final int hash = settings.hashCode();
    final int expected = Objects.hash(PORT, WIDTH, HEIGHT, FRAME_RATE);
    assertEquals(expected, hash);
  }

  @Test
  void shouldGiveSettingsThatDifferInOneValueDifferentHashCodes() {
    final VmSettings settings = VmSettings.of(PORT, WIDTH, HEIGHT, FRAME_RATE);
    final VmSettings otherPort = VmSettings.of(OTHER_PORT, WIDTH, HEIGHT, FRAME_RATE);
    final VmSettings otherWidth = VmSettings.of(PORT, OTHER_WIDTH, HEIGHT, FRAME_RATE);
    final VmSettings otherHeight = VmSettings.of(PORT, WIDTH, OTHER_HEIGHT, FRAME_RATE);
    final VmSettings otherFrameRate = VmSettings.of(PORT, WIDTH, HEIGHT, OTHER_FRAME_RATE);
    final int hash = settings.hashCode();
    final int portHash = otherPort.hashCode();
    final int widthHash = otherWidth.hashCode();
    final int heightHash = otherHeight.hashCode();
    final int frameRateHash = otherFrameRate.hashCode();
    assertNotEquals(hash, portHash);
    assertNotEquals(hash, widthHash);
    assertNotEquals(hash, heightHash);
    assertNotEquals(hash, frameRateHash);
  }

  @Test
  void shouldDescribeItself() {
    final VmSettings settings = VmSettings.of(PORT, WIDTH, HEIGHT, FRAME_RATE);
    final String text = settings.toString();
    assertEquals("VmSettings{port=5901, width=640, height=480, targetFrameRate=30}", text);
  }
}
