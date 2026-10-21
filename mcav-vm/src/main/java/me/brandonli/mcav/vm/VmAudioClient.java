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

import com.google.common.annotations.VisibleForTesting;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import me.brandonli.mcav.media.player.pipeline.filter.audio.AudioFilter;

/**
 * The audio connection to QEMU's VNC server: a second, shared RFB client of the display of the machine, which
 * receives the samples the guest plays through {@link QemuAudioProtocol} and hands them to a {@link Sink}.
 *
 * <p>QEMU is asked for the format of the audio pipeline, 16-bit little-endian stereo at 48 kHz, and converts to it
 * itself, so the samples need no conversion here. The whole handshake must finish within
 * {@value #HANDSHAKE_TIMEOUT_MILLISECONDS} ms; afterwards a reader thread waits for as long as the guest stays silent. A
 * connection that ends or breaks the protocol while nobody closed it, or whose sink fails, is reported, once.
 */
final class VmAudioClient implements Closeable {

  static final int HANDSHAKE_TIMEOUT_MILLISECONDS = 5_000;

  private static final String UNSTARTED_READER_NAME = "mcav-vm-audio-not-started";
  private static final String READER_THREAD_NAME = "mcav-vm-audio";
  private static final String DEADLINE_THREAD_NAME = "mcav-vm-audio-deadline";

  private final Socket socket;
  private final DataInputStream input;
  private final Sink sink;
  private final BiConsumer<String, Throwable> failures;

  private volatile Thread reader;
  private volatile boolean closed;

  private VmAudioClient(final Socket socket, final DataInputStream input, final Sink sink, final BiConsumer<String, Throwable> failures) {
    this.socket = socket;
    this.input = input;
    this.sink = sink;
    this.failures = failures;
    this.reader = new Thread(UNSTARTED_READER_NAME);
  }

  /**
   * Connects to the VNC server of a machine, enables its audio and starts receiving samples.
   *
   * @param address  the address of the VNC server
   * @param password the password of the display, or an empty string for a display without one
   * @param sink     receives the samples
   * @param failures receives an unexpected end of the connection
   * @return the connected client
   * @throws IOException if the connection or the handshake fails
   */
  static VmAudioClient connect(
    final InetSocketAddress address,
    final String password,
    final Sink sink,
    final BiConsumer<String, Throwable> failures
  ) throws IOException {
    final Socket socket = new Socket();
    final CompletableFuture<Void> deadline = scheduleHandshakeDeadline(socket);
    try {
      socket.connect(address, HANDSHAKE_TIMEOUT_MILLISECONDS);

      final InputStream rawInput = socket.getInputStream();
      final DataInputStream input = new DataInputStream(new BufferedInputStream(rawInput));
      final OutputStream rawOutput = socket.getOutputStream();
      final DataOutputStream output = new DataOutputStream(new BufferedOutputStream(rawOutput));
      handshake(input, output, password);
      deadline.cancel(false);

      final VmAudioClient client = new VmAudioClient(socket, input, sink, failures);
      client.startReading();

      return client;
    } catch (final IOException exception) {
      closeQuietly(socket);
      throw exception;
    }
  }

  private static CompletableFuture<Void> scheduleHandshakeDeadline(final Socket socket) {
    // the read timeout only limits a pause, so a server that trickles its handshake is cut off once it took too long;
    // a deadline cancelled in time never runs, and one that runs gets a thread of its own, as the common pool of the
    // server may be busy with the tasks of other plugins for longer than the deadline
    final Executor later = CompletableFuture.delayedExecutor(
      HANDSHAKE_TIMEOUT_MILLISECONDS,
      TimeUnit.MILLISECONDS,
      VmAudioClient::onOwnThread
    );
    return CompletableFuture.runAsync(() -> closeQuietly(socket), later);
  }

  /**
   * Agrees on RFB 3.8 with the password of the display, or without authentication if the server asks for none, shares
   * the display, and enables the audio in the format of the audio pipeline.
   *
   * @param input    the stream from the server
   * @param output   the stream to the server
   * @param password the password of the display, or an empty string for none
   * @throws IOException if the handshake fails
   */
  private static void handshake(final DataInputStream input, final DataOutputStream output, final String password) throws IOException {
    QemuAudioProtocol.readVersion(input);
    QemuAudioProtocol.writeVersion(output);
    output.flush();

    final boolean authenticate = QemuAudioProtocol.negotiateSecurity(input, output, password);
    output.flush();

    if (authenticate) {
      QemuAudioProtocol.answerChallenge(input, output, password);
      output.flush();
    }

    QemuAudioProtocol.readSecurityResult(input);
    QemuAudioProtocol.writeClientInitialization(output);
    output.flush();

    QemuAudioProtocol.readServerInitialization(input);
    QemuAudioProtocol.writeSetEncodings(output);
    QemuAudioProtocol.writeEnableAudio(output, AudioFilter.CHANNELS, AudioFilter.SAMPLE_RATE);
    output.flush();
  }

  private void startReading() {
    final Thread thread = new Thread(this::read, READER_THREAD_NAME);
    thread.setDaemon(true);
    this.reader = thread;
    thread.start();
  }

  private void read() {
    final byte[] buffer = new byte[QemuAudioProtocol.MAXIMUM_AUDIO_BYTES];
    try {
      while (true) {
        final QemuAudioProtocol.Message message = QemuAudioProtocol.readMessage(this.input, AudioFilter.FRAME_SIZE, size -> buffer);
        this.deliver(message);
      }
    } catch (final IOException | RuntimeException exception) {
      if (!this.closed) {
        closeQuietly(this.socket);
        this.failures.accept("The audio connection of the virtual machine ended", exception);
      }
    }
  }

  private void deliver(final QemuAudioProtocol.Message message) {
    final QemuAudioProtocol.Kind kind = message.getKind();
    if (kind == QemuAudioProtocol.Kind.DATA) {
      final byte[] samples = message.getSamples();
      final int length = message.getLength();
      this.sink.accept(samples, length);
    } else if (kind == QemuAudioProtocol.Kind.END) {
      // QEMU sends nothing until the guest plays again, so the sink keeps the time of the pause itself
      this.sink.quiet();
    }
  }

  /**
   * Closes the connection and waits for the reader thread to end. Safe to call more than once.
   */
  @Override
  public void close() {
    this.closed = true;
    closeQuietly(this.socket);
    join(this.reader);
  }

  private static void onOwnThread(final Runnable task) {
    Thread.ofVirtual().name(DEADLINE_THREAD_NAME).start(task);
  }

  /**
   * Waits for a thread of the sound to end, at most {@value #HANDSHAKE_TIMEOUT_MILLISECONDS} ms, unless it is the calling
   * thread: a filter or a failure callback that releases the player runs on the thread it would wait for.
   *
   * @param thread the thread
   */
  static void join(final Thread thread) {
    final Thread caller = Thread.currentThread();
    final boolean ownThread = thread.equals(caller);
    if (ownThread) {
      return;
    }

    try {
      thread.join(HANDSHAKE_TIMEOUT_MILLISECONDS);
    } catch (final InterruptedException exception) {
      caller.interrupt();
    }
  }

  /**
   * Closes a socket whose failure to close changes nothing, as the connection is gone either way.
   *
   * @param closeable the socket
   */
  static void closeQuietly(final Closeable closeable) {
    try {
      closeable.close();
    } catch (final IOException exception) {
      // the connection is gone either way
    }
  }

  /**
   * Gets the thread that reads the connection, so tests can check how it runs.
   *
   * @return the reader thread
   */
  @VisibleForTesting
  Thread getReader() {
    return this.reader;
  }

  /**
   * Checks whether the reader thread still runs.
   *
   * @return true while samples can arrive
   */
  boolean isAlive() {
    return this.reader.isAlive();
  }

  /**
   * Receives the samples of the guest.
   */
  @FunctionalInterface
  interface Sink {
    /**
     * Takes samples, which must be copied: the buffer is reused for the next message.
     *
     * @param samples the buffer, 16-bit little-endian stereo at 48 kHz
     * @param length  the number of bytes of samples at its start, whole frames
     */
    void accept(byte[] samples, int length);

    /**
     * Learns that the guest stopped playing: nothing arrives until it plays again.
     */
    default void quiet() {}
  }
}
