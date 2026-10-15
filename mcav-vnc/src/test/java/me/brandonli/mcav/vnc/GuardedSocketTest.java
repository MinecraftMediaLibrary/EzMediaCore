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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.junit.jupiter.api.Test;

// The socket the VNC client connects through answers for the socket it guards, and closes it.
final class GuardedSocketTest {

  @Test
  void shouldAnswerForTheSocketItGuardsAndClosesIt() throws IOException {
    final Socket socket = mock(Socket.class);
    when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
    final GuardedSocket guarded = new GuardedSocket(socket);

    final boolean noDelayBefore = guarded.getTcpNoDelay();
    final boolean connectedBefore = guarded.isConnected();
    final boolean closedBefore = guarded.isClosed();
    assertFalse(noDelayBefore);
    assertFalse(connectedBefore);
    assertFalse(closedBefore);

    when(socket.getTcpNoDelay()).thenReturn(true);
    when(socket.isConnected()).thenReturn(true);
    when(socket.isClosed()).thenReturn(true);
    final boolean noDelay = guarded.getTcpNoDelay();
    final boolean connected = guarded.isConnected();
    final boolean closed = guarded.isClosed();
    assertTrue(noDelay);
    assertTrue(connected);
    assertTrue(closed);

    guarded.close();
    verify(socket).close();
  }
}
