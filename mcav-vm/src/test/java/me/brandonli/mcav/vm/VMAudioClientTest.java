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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import me.brandonli.mcav.vm.testing.VncPasswords;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VMAudioClientTest {

  private static final String PASSWORD = "Pa55word";
  private static final SecureRandom RANDOM = new SecureRandom();

  private final List<byte[]> chunks = new CopyOnWriteArrayList<>();
  private final List<String> failures = new CopyOnWriteArrayList<>();
  private ServerSocket server;

  @BeforeEach
  void listen() throws IOException {
    this.server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
  }

  @AfterEach
  void close() throws IOException {
    this.server.close();
  }

  private InetSocketAddress address() {
    return new InetSocketAddress(InetAddress.getLoopbackAddress(), this.server.getLocalPort());
  }

  private VMAudioClient connect() throws IOException {
    return VMAudioClient.connect(
      this.address(),
      PASSWORD,
      (samples, length) -> this.chunks.add(Arrays.copyOf(samples, length)),
      (message, failure) -> this.failures.add(message)
    );
  }

  /**
   * Plays the server side of the handshake QEMU does, and checks what the client sends.
   *
   * @param socket the connection
   * @return the streams, for what follows the handshake
   * @throws IOException if the connection fails
   */
  private static Streams handshake(final Socket socket) throws IOException {
    return handshake(socket, PASSWORD);
  }

  /**
   * Plays the server side of the handshake of a display with a password, as QEMU's with password-secret, or of one
   * without, for an empty password.
   */
  private static Streams handshake(final Socket socket, final String password) throws IOException {
    final DataInputStream in = new DataInputStream(socket.getInputStream());
    final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    out.write("RFB 003.008\n".getBytes(StandardCharsets.US_ASCII));
    final byte[] version = new byte[12];
    in.readFully(version);
    final String clientVersion = new String(version, StandardCharsets.US_ASCII);
    assertEquals("RFB 003.008\n", clientVersion);
    if (password.isEmpty()) {
      out.write(new byte[] { 1, 1 });
      final int securityType = in.readUnsignedByte();
      assertEquals(1, securityType, "no authentication");
    } else {
      out.write(new byte[] { 1, 2 });
      final int securityType = in.readUnsignedByte();
      assertEquals(2, securityType, "VNC authentication");
      final byte[] challenge = new byte[16];
      RANDOM.nextBytes(challenge);
      out.write(challenge);
      final byte[] response = new byte[16];
      in.readFully(response);
      final byte[] expectedResponse = VncPasswords.response(challenge, password);
      assertArrayEquals(expectedResponse, response, "the answer of the password");
    }
    out.writeInt(0);
    final int shared = in.readUnsignedByte();
    assertEquals(1, shared, "shared");
    out.writeShort(720);
    out.writeShort(400);
    out.write(new byte[16]);
    out.writeInt(4);
    out.write("QEMU".getBytes(StandardCharsets.US_ASCII));
    final byte[] requests = new byte[8 + 14];
    in.readFully(requests);
    final byte[] expected = QemuAudioProtocolTest.bytes(output -> {
      QemuAudioProtocol.writeSetEncodings(output);
      QemuAudioProtocol.writeEnableAudio(output, 2, 48_000);
    });
    assertArrayEquals(expected, requests);
    return new Streams(in, out);
  }

  @Test
  void shouldJoinADisplayWithoutAPasswordWithoutAuthentication() throws Exception {
    final CompletableFuture<Void> served = CompletableFuture.runAsync(() -> {
      try (final Socket socket = this.server.accept()) {
        handshake(socket, "");
        socket.getInputStream().read();
      } catch (final IOException exception) {
        throw new UncheckedIOException(exception);
      }
    });
    final VMAudioClient client = VMAudioClient.connect(this.address(), "", (samples, length) -> {}, (message, failure) -> {});
    final boolean alive = client.isAlive();
    assertTrue(alive);
    client.close();
    served.get(10, TimeUnit.SECONDS);
  }

  @Test
  void shouldEndTheHandshakeOfAWrongPasswordWithTheReasonOfTheServer() throws Exception {
    final CompletableFuture<Void> served = CompletableFuture.runAsync(() -> {
      try (final Socket socket = this.server.accept()) {
        final DataInputStream in = new DataInputStream(socket.getInputStream());
        final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.write("RFB 003.008\n".getBytes(StandardCharsets.US_ASCII));
        in.readFully(new byte[12]);
        out.write(new byte[] { 1, 2 });
        final int securityType = in.readUnsignedByte();
        assertEquals(2, securityType, "VNC authentication");
        out.write(new byte[16]);
        in.readFully(new byte[16]);
        out.writeInt(1);
        final byte[] reason = "Authentication failed".getBytes(StandardCharsets.US_ASCII);
        out.writeInt(reason.length);
        out.write(reason);
        out.flush();
      } catch (final IOException exception) {
        throw new UncheckedIOException(exception);
      }
    });
    final ProtocolException refused = assertThrows(ProtocolException.class, this::connect);
    final String message = refused.getMessage();
    assertEquals("VNC server refused the connection because of \"Authentication failed\"!", message);
    served.get(10, TimeUnit.SECONDS);
  }

  @Test
  void shouldHandTheSoundOfTheGuestToTheSinkAndReportTheEnd() throws Exception {
    final CompletableFuture<Void> served = CompletableFuture.runAsync(() -> {
      try (final Socket socket = this.server.accept()) {
        final Streams streams = handshake(socket);
        QemuAudioProtocolTest.writeAcknowledgement(streams.out());
        streams.out().write(new byte[] { (byte) 255, 1, 0, 1 });
        QemuAudioProtocolTest.writeData(streams.out(), new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        QemuAudioProtocolTest.writeData(streams.out(), new byte[] { 9, 9, 9, 9 });
        streams.out().write(new byte[] { (byte) 255, 1, 0, 0 });
        streams.out().flush();
        waitUntil(() -> this.chunks.size() == 2);
      } catch (final IOException exception) {
        throw new UncheckedIOException(exception);
      }
    });
    try (final VMAudioClient client = this.connect()) {
      served.get(10, TimeUnit.SECONDS);
      final byte[] expectedFirstChunk = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
      final byte[] expectedSecondChunk = new byte[] { 9, 9, 9, 9 };
      final byte[] firstChunk = this.chunks.getFirst();
      final byte[] secondChunk = this.chunks.get(1);
      assertArrayEquals(expectedFirstChunk, firstChunk);
      assertArrayEquals(expectedSecondChunk, secondChunk);
      waitUntil(() -> !client.isAlive());
      final List<String> expected = List.of("The audio connection of the virtual machine ended");
      assertEquals(expected, this.failures);
    }
  }

  @Test
  void shouldTellTheSinkWhenTheSoundOfTheGuestEnds() throws Exception {
    final List<String> heard = new CopyOnWriteArrayList<>();
    final VMAudioClient.Sink sink = new VMAudioClient.Sink() {
      @Override
      public void accept(final byte[] samples, final int length) {
        heard.add("sound " + length);
      }

      @Override
      public void quiet() {
        heard.add("quiet");
      }
    };
    final CompletableFuture<Void> served = CompletableFuture.runAsync(() -> {
      try (final Socket socket = this.server.accept()) {
        final Streams streams = handshake(socket);
        QemuAudioProtocolTest.writeAcknowledgement(streams.out());
        streams.out().write(new byte[] { (byte) 255, 1, 0, 1 });
        QemuAudioProtocolTest.writeData(streams.out(), new byte[] { 1, 2, 3, 4 });
        streams.out().write(new byte[] { (byte) 255, 1, 0, 0 });
        streams.out().flush();
        waitUntil(() -> heard.size() == 2);
      } catch (final IOException exception) {
        throw new UncheckedIOException(exception);
      }
    });
    try (final VMAudioClient client = VMAudioClient.connect(this.address(), PASSWORD, sink, (message, failure) -> {})) {
      served.get(10, TimeUnit.SECONDS);
      final List<String> expected = List.of("sound 4", "quiet");
      final boolean readerStarted = client.getReader() != null;
      assertEquals(expected, heard, "the end of the guest's sound, after its samples");
      assertTrue(readerStarted);
    }
    final VMAudioClient.Sink plain = (samples, length) -> {};
    plain.quiet();
  }

  @Test
  void shouldEndTheConnectionAndReportASinkThatFails() throws Exception {
    final CompletableFuture<Void> served = CompletableFuture.runAsync(() -> {
      try (final Socket socket = this.server.accept()) {
        final Streams streams = handshake(socket);
        QemuAudioProtocolTest.writeAcknowledgement(streams.out());
        streams.out().write(new byte[] { (byte) 255, 1, 0, 1 });
        QemuAudioProtocolTest.writeData(streams.out(), new byte[] { 1, 2, 3, 4 });
        streams.out().flush();
        final int next = streams.in().read();
        assertEquals(-1, next);
      } catch (final IOException exception) {
        throw new UncheckedIOException(exception);
      }
    });
    final VMAudioClient.Sink failing = (samples, length) -> {
      throw new IllegalStateException("sink broke!");
    };
    try (
      final VMAudioClient client = VMAudioClient.connect(this.address(), PASSWORD, failing, (message, failure) ->
        this.failures.add(message)
      )
    ) {
      served.get(10, TimeUnit.SECONDS);
      waitUntil(() -> !client.isAlive());
      final List<String> expected = List.of("The audio connection of the virtual machine ended");
      assertEquals(expected, this.failures);
    }
  }

  /**
   * Sends the banner of RFB one byte a second: every pause is shorter than the timeout of a read, the whole banner far
   * longer than the handshake may take.
   *
   * @param server the listening socket
   */
  private static void trickleTheBanner(final ServerSocket server) {
    try (final Socket socket = server.accept()) {
      final OutputStream out = socket.getOutputStream();
      for (final byte value : "RFB 003.008\n".getBytes(StandardCharsets.US_ASCII)) {
        out.write(value);
        out.flush();
        Thread.sleep(1_000L);
      }
    } catch (final IOException exception) {
      // the client gave up and closed the connection
    } catch (final InterruptedException exception) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Keeps every worker of the common pool busy until the returned latch opens, as the tasks of other plugins can.
   *
   * @return the latch that frees the workers
   */
  private static CountDownLatch occupyTheCommonPool() throws InterruptedException {
    final int workers = ForkJoinPool.getCommonPoolParallelism();
    final CountDownLatch busy = new CountDownLatch(workers);
    final CountDownLatch release = new CountDownLatch(1);
    final ForkJoinPool pool = ForkJoinPool.commonPool();
    for (int worker = 0; worker < workers; worker++) {
      pool.execute(() -> {
        busy.countDown();
        try {
          release.await();
        } catch (final InterruptedException exception) {
          Thread.currentThread().interrupt();
        }
      });
    }
    final boolean allBusy = busy.await(30, TimeUnit.SECONDS);
    assertTrue(allBusy, "every worker of the common pool is busy");
    return release;
  }

  @Test
  void shouldHoldTheHandshakeDeadlineWhileTheCommonPoolIsBusy() throws Exception {
    final CountDownLatch release = occupyTheCommonPool();
    try {
      final Thread server = Thread.ofPlatform()
        .daemon()
        .start(() -> {
          try {
            final Socket socket = this.server.accept();
            try {
              release.await();
            } finally {
              socket.close();
            }
          } catch (final IOException exception) {
            // the test ended
          } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
          }
        });
      assertTimeoutPreemptively(Duration.ofSeconds(60), () -> assertThrows(IOException.class, this::connect));
      final boolean serverAlive = server.isAlive();
      assertTrue(serverAlive, "the client gave up, not the server");
    } finally {
      release.countDown();
    }
  }

  @Test
  void shouldCutOffAServerThatTricklesItsHandshakeAtTheDeadline() throws Exception {
    final CompletableFuture<Void> served = CompletableFuture.runAsync(() -> trickleTheBanner(this.server));
    final long start = System.nanoTime();
    assertThrows(IOException.class, this::connect);
    final long seconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start);
    final boolean withinTheDeadline = seconds < VMAudioClient.HANDSHAKE_TIMEOUT_MILLIS / 1000 + 3;
    assertTrue(withinTheDeadline, "the handshake took " + seconds + " s");
    served.get(20, TimeUnit.SECONDS);
  }

  @Test
  void shouldNotWaitForItselfWhenAFailureCallbackClosesTheClient() throws Exception {
    final CompletableFuture<Void> served = CompletableFuture.runAsync(() -> {
      try (final Socket socket = this.server.accept()) {
        final Streams streams = handshake(socket);
        QemuAudioProtocolTest.writeAcknowledgement(streams.out());
      } catch (final IOException exception) {
        throw new UncheckedIOException(exception);
      }
    });
    final AtomicReference<VMAudioClient> self = new AtomicReference<>();
    final AtomicLong closeNanos = new AtomicLong(-1);
    final VMAudioClient client = VMAudioClient.connect(
      this.address(),
      PASSWORD,
      (samples, length) -> {},
      (message, failure) -> {
        final long begin = System.nanoTime();
        waitUntil(() -> self.get() != null);
        self.get().close();
        closeNanos.set(System.nanoTime() - begin);
      }
    );
    self.set(client);
    served.get(10, TimeUnit.SECONDS);
    waitUntil(() -> closeNanos.get() >= 0);
    final boolean closedQuickly = closeNanos.get() < TimeUnit.SECONDS.toNanos(1);
    assertTrue(closedQuickly, "closed without waiting for its own reader");
    client.close();
  }

  @Test
  void shouldRunTheReaderAsADaemonAndWaitForItWhenClosing() throws Exception {
    final CompletableFuture<Void> served = CompletableFuture.runAsync(() -> {
      try (final Socket socket = this.server.accept()) {
        handshake(socket);
        socket.getInputStream().read();
      } catch (final IOException exception) {
        throw new UncheckedIOException(exception);
      }
    });
    final VMAudioClient client = this.connect();
    final boolean daemon = client.getReader().isDaemon();
    assertTrue(daemon, "the sound never keeps the JVM alive");
    client.close();
    final boolean alive = client.isAlive();
    assertFalse(alive, "close returns once the reader ended");
    served.get(10, TimeUnit.SECONDS);
  }

  @Test
  void shouldEndQuietlyWhenClosed() throws Exception {
    final CompletableFuture<Void> served = CompletableFuture.runAsync(() -> {
      try (final Socket socket = this.server.accept()) {
        handshake(socket);
        socket.getInputStream().read();
      } catch (final IOException exception) {
        throw new UncheckedIOException(exception);
      }
    });
    final VMAudioClient client = this.connect();
    final boolean aliveBeforeClose = client.isAlive();
    assertTrue(aliveBeforeClose);
    Thread.currentThread().interrupt();
    try {
      client.close();
      final boolean interrupted = Thread.currentThread().isInterrupted();
      assertTrue(interrupted, "an interrupted close keeps the interrupt");
    } finally {
      Thread.interrupted();
    }
    client.close();
    waitUntil(() -> !client.isAlive());
    final boolean aliveAfterClose = client.isAlive();
    assertFalse(aliveAfterClose);
    served.get(10, TimeUnit.SECONDS);
    assertEquals(List.of(), this.failures);
  }

  @Test
  void shouldFailAConnectionWithoutAPasswordToAServerThatWantsOne() {
    final CompletableFuture<Void> served = CompletableFuture.runAsync(() -> {
      try (final Socket socket = this.server.accept()) {
        final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.write("RFB 003.008\n".getBytes(StandardCharsets.US_ASCII));
        out.write(new byte[] { 1, 2 });
        socket.getInputStream().readNBytes(12);
        socket.getInputStream().read();
      } catch (final IOException exception) {
        throw new UncheckedIOException(exception);
      }
    });
    final ProtocolException failure = assertThrows(ProtocolException.class, () ->
      VMAudioClient.connect(this.address(), "", (samples, length) -> {}, (message, cause) -> {})
    );
    final String message = failure.getMessage();
    assertEquals("VNC server asks for authentication, which the audio connection does not do!", message);
    served.join();
  }

  @Test
  void shouldChangeNothingWhenCloseFails() {
    final AtomicInteger closed = new AtomicInteger();
    VMAudioClient.closeQuietly(() -> {
      closed.incrementAndGet();
      throw new IOException("gone!");
    });
    final int closeCalls = closed.get();
    assertEquals(1, closeCalls);
  }

  private static void waitUntil(final BooleanSupplier condition) {
    final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
    while (!condition.getAsBoolean()) {
      if (System.nanoTime() > deadline) {
        throw new AssertionError("Condition did not hold in time!");
      }
      Thread.onSpinWait();
    }
  }

  /**
   * The streams of a connection.
   *
   * @param in  from the client
   * @param out to the client
   */
  private record Streams(DataInputStream in, DataOutputStream out) {}

  @Test
  void shouldKeepTheInterruptOfAnInterruptedWaitForAThreadOfTheSound() throws InterruptedException {
    final CountDownLatch release = new CountDownLatch(1);
    final Thread alive = new Thread(() -> {
      try {
        release.await();
      } catch (final InterruptedException exception) {
        Thread.currentThread().interrupt();
      }
    });
    alive.start();
    Thread.currentThread().interrupt();
    try {
      VMAudioClient.join(alive);
      final boolean interrupted = Thread.currentThread().isInterrupted();
      assertTrue(interrupted, "the interrupt stays for the caller");
    } finally {
      Thread.interrupted();
      release.countDown();
    }
    alive.join(10_000L);
    final boolean stillAlive = alive.isAlive();
    assertFalse(stillAlive);
  }
}
