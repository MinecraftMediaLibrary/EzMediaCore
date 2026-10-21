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
package me.brandonli.mcav.bukkit.resourcepack.provider.http;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultFileRegion;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Answers exactly one HTTP request per connection with the served file.
 *
 * <p>The handler reads the request until the end of its headers, looks only at the request method, and answers
 * {@code GET} with the file and {@code HEAD} with its headers. Any other method is rejected. The connection is
 * closed after the response, which keeps the handler simple and is exactly what Minecraft clients expect when
 * they download a resource pack. The file body is sent as a zero-copy file region.
 *
 * <p>The file is opened once per request, and both the {@code Content-Length} header and the body come from that
 * open file. A pack that is replaced while a download starts therefore never produces a response whose length
 * does not match its body. Once the request is complete, the read timeout of the connection is removed, so a slow
 * download is never cut off.
 *
 * <p>A new handler is created for every connection, so it is never shared between threads.
 */
final class FileServerHandler extends ChannelInboundHandlerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileServerHandler.class);
  private static final String CLOSE_FAILED = "Could not close a resource pack download connection after an error.";
  private static final ChannelFutureListener LOG_CLOSE_FAILURE = FileServerHandler::logCloseFailure;
  private static final int MAXIMUM_HEADER_BYTES = 8192;
  private static final int INITIAL_REQUEST_BUFFER_BYTES = 256;
  private static final byte[] HEADER_TERMINATOR = { '\r', '\n', '\r', '\n' };
  private static final String STATUS_OK = "200 OK";
  private static final String STATUS_NOT_FOUND = "404 Not Found";
  private static final String STATUS_METHOD_NOT_ALLOWED = "405 Method Not Allowed";
  private static final String STATUS_HEADER_TOO_LARGE = "431 Request Header Fields Too Large";
  private static final String CONTENT_TYPE = "application/zip";
  private static final String DEFAULT_FILE_NAME = "resourcepack.zip";

  private static final String GET_METHOD = "GET";
  private static final String HEAD_METHOD = "HEAD";

  private static final String STATUS_LINE_PREFIX = "HTTP/1.1 ";

  private static final String LINE_TERMINATOR = "\r\n";

  private static final String CONNECTION_CLOSE_HEADER = "Connection: close\r\n";

  private static final Pattern UNSAFE_FILE_NAME_CHARACTERS = Pattern.compile("[\\p{Cntrl}\"\\\\]");

  private final Path filePath;
  private final String fileName;
  private final FileOpener opener;

  private @Nullable ByteBuf requestBuffer;
  private boolean responded;

  /**
   * Constructs a new handler for one connection.
   *
   * @param filePath the file to serve
   */
  FileServerHandler(final Path filePath) {
    this(filePath, FileServerHandler::openFile);
  }

  /**
   * Constructs a new handler for one connection that opens the file with the specified opener.
   *
   * @param filePath the file to serve
   * @param opener   opens the file for reading
   */
  @VisibleForTesting
  FileServerHandler(final Path filePath, final FileOpener opener) {
    this.filePath = filePath;
    this.fileName = getFileName(filePath);
    this.opener = opener;
  }

  private static FileChannel openFile(final Path path) throws IOException {
    return FileChannel.open(path, StandardOpenOption.READ);
  }

  /**
   * Returns the name of the served file for the {@code Content-Disposition} header.
   *
   * @param path the path of the served file
   * @return the file name, or a generic name if the path has none, such as a root directory
   */
  @VisibleForTesting
  static String getFileName(final Path path) {
    final Path fileNamePath = path.getFileName();
    return fileNamePath == null ? DEFAULT_FILE_NAME : fileNamePath.toString();
  }

  /**
   * Allocates the buffer that collects the request headers.
   *
   * @param context the context of this handler
   */
  @Override
  public void handlerAdded(final ChannelHandlerContext context) {
    Preconditions.checkNotNull(context);

    final ByteBufAllocator allocator = context.alloc();
    this.requestBuffer = allocator.buffer(INITIAL_REQUEST_BUFFER_BYTES, MAXIMUM_HEADER_BYTES);
  }

  /**
   * Releases the request buffer. Calling this method again after the buffer was released has no effect.
   *
   * @param context the context of this handler
   */
  @Override
  public void handlerRemoved(final ChannelHandlerContext context) {
    Preconditions.checkNotNull(context);

    final ByteBuf buffer = this.requestBuffer;
    if (buffer != null) {
      this.requestBuffer = null;
      buffer.release();
    }
  }

  /**
   * Collects request bytes until the headers are complete, then answers the request. The received message is
   * always released, and everything that arrives after the response was sent is dropped.
   *
   * @param context the context of this handler
   * @param message the received bytes
   */
  @Override
  public void channelRead(final ChannelHandlerContext context, final Object message) {
    Preconditions.checkNotNull(context);
    Preconditions.checkNotNull(message);

    try {
      final ByteBuf buffer = this.requestBuffer;
      if (this.responded || buffer == null || !(message instanceof final ByteBuf data)) {
        return;
      }

      this.collect(context, buffer, data);
    } finally {
      ReferenceCountUtil.release(message);
    }
  }

  private void collect(final ChannelHandlerContext context, final ByteBuf buffer, final ByteBuf data) {
    // only the headers count against the limit, so a read that ends them may carry more, which is never looked at
    final int incoming = data.readableBytes();
    final int available = buffer.maxWritableBytes();
    final int taken = Math.min(incoming, available);
    buffer.writeBytes(data, taken);
    final int headerEnd = this.indexOfHeaderEnd(buffer);
    if (headerEnd < 0) {
      if (data.isReadable()) {
        this.respondWithStatus(context, STATUS_HEADER_TOO_LARGE);
      }

      return;
    }

    this.stopReadTimeout(context);
    final String method = this.readMethod(buffer);
    this.respond(context, method);
  }

  // The request is complete, so the client has nothing more to send while it downloads the file. The write timeout of
  // the pipeline takes over from here and closes a download that stalls.
  private void stopReadTimeout(final ChannelHandlerContext context) {
    final ChannelPipeline pipeline = context.pipeline();
    final ChannelHandler timeout = pipeline.get(FileHttpChannelInitializer.READ_TIMEOUT_NAME);
    if (timeout != null) {
      pipeline.remove(timeout);
    }
  }

  private int indexOfHeaderEnd(final ByteBuf buffer) {
    final int start = buffer.readerIndex();
    final int writerIndex = buffer.writerIndex();
    final int end = writerIndex - HEADER_TERMINATOR.length;
    for (int index = start; index <= end; index++) {
      final boolean matches = this.isHeaderTerminatorAt(buffer, index);
      if (matches) {
        return index;
      }
    }

    return -1;
  }

  private boolean isHeaderTerminatorAt(final ByteBuf buffer, final int index) {
    for (int offset = 0; offset < HEADER_TERMINATOR.length; offset++) {
      final byte actual = buffer.getByte(index + offset);
      if (actual != HEADER_TERMINATOR[offset]) {
        return false;
      }
    }

    return true;
  }

  private String readMethod(final ByteBuf buffer) {
    final int start = buffer.readerIndex();
    final int end = buffer.writerIndex();
    int spaceIndex = start;
    while (spaceIndex < end) {
      final byte current = buffer.getByte(spaceIndex);
      if (current == ' ') {
        break;
      }
      spaceIndex++;
    }

    final int length = spaceIndex - start;
    return buffer.toString(start, length, StandardCharsets.US_ASCII);
  }

  private void respond(final ChannelHandlerContext context, final String method) {
    final boolean isGet = method.equals(GET_METHOD);
    final boolean isHead = method.equals(HEAD_METHOD);
    if (!isGet && !isHead) {
      this.respondWithStatus(context, STATUS_METHOD_NOT_ALLOWED);
      return;
    }

    final FileChannel channel;
    try {
      channel = this.opener.open(this.filePath);
    } catch (final IOException exception) {
      this.respondWithStatus(context, STATUS_NOT_FOUND);
      return;
    }

    this.respondWithFile(context, channel, method);
  }

  private void respondWithFile(final ChannelHandlerContext context, final FileChannel channel, final String method) {
    final long fileLength;
    try {
      fileLength = channel.size();
    } catch (final IOException exception) {
      this.closeQuietly(channel);
      this.respondWithStatus(context, STATUS_NOT_FOUND);
      return;
    }

    this.responded = true;
    this.sendFile(context, channel, fileLength, method);
  }

  /**
   * Sends the headers and, unless only the headers were requested, the file itself without copying it into memory.
   * The file region closes the channel once it was sent.
   */
  private void sendFile(final ChannelHandlerContext context, final FileChannel channel, final long fileLength, final String method) {
    final ByteBuf headers = this.createHeaders(fileLength, this.fileName);
    if (method.equals(HEAD_METHOD)) {
      this.closeQuietly(channel);
      final ChannelFuture headersFuture = context.writeAndFlush(headers);
      headersFuture.addListener(ChannelFutureListener.CLOSE);
      return;
    }

    final DefaultFileRegion region = new DefaultFileRegion(channel, 0, fileLength);
    final ChannelFuture headersFuture = context.write(headers);
    headersFuture.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    final ChannelFuture bodyFuture = context.writeAndFlush(region);
    bodyFuture.addListener(ChannelFutureListener.CLOSE);
  }

  private void closeQuietly(final FileChannel channel) {
    try {
      channel.close();
    } catch (final IOException exception) {
      // The channel is read-only, so a failed close cannot lose written data.
    }
  }

  private void respondWithStatus(final ChannelHandlerContext context, final String status) {
    this.responded = true;
    final String text = STATUS_LINE_PREFIX + status + LINE_TERMINATOR + "Content-Length: 0\r\n" + CONNECTION_CLOSE_HEADER + LINE_TERMINATOR;
    final ByteBuf buffer = Unpooled.copiedBuffer(text, StandardCharsets.US_ASCII);
    final ChannelFuture future = context.writeAndFlush(buffer);
    future.addListener(ChannelFutureListener.CLOSE);
  }

  private ByteBuf createHeaders(final long contentLength, final String fileName) {
    final String safeFileName = UNSAFE_FILE_NAME_CHARACTERS.matcher(fileName).replaceAll("");
    final String text =
      STATUS_LINE_PREFIX +
      STATUS_OK +
      LINE_TERMINATOR +
      "Content-Type: " +
      CONTENT_TYPE +
      LINE_TERMINATOR +
      "Content-Length: " +
      contentLength +
      LINE_TERMINATOR +
      "Content-Disposition: attachment; filename=\"" +
      safeFileName +
      "\"\r\n" +
      "Cache-Control: no-store\r\n" +
      CONNECTION_CLOSE_HEADER +
      LINE_TERMINATOR;
    return Unpooled.copiedBuffer(text, StandardCharsets.US_ASCII);
  }

  /**
   * Closes the connection when anything goes wrong, including a read timeout. Errors of a single download must
   * never affect the server.
   *
   * @param context the context of this handler
   * @param cause   the error
   */
  @Override
  public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
    Preconditions.checkNotNull(context);
    Preconditions.checkNotNull(cause);

    final ChannelFuture closing = context.close();
    closing.addListener(LOG_CLOSE_FAILURE);
  }

  /**
   * Logs why a connection could not be closed after an error. Nothing else is left to do for such a connection, so
   * the failure is only recorded for debugging.
   */
  private static void logCloseFailure(final ChannelFuture closing) {
    final boolean closed = closing.isSuccess();
    if (closed) {
      return;
    }

    final Throwable failure = closing.cause();
    LOGGER.debug(CLOSE_FAILED, failure);
  }

  /**
   * Opens the served file for reading.
   */
  @FunctionalInterface
  interface FileOpener {
    /**
     * Opens a file for reading.
     *
     * @param path the file to open
     * @return the open file, which the caller closes
     * @throws IOException if the file cannot be opened
     */
    FileChannel open(Path path) throws IOException;
  }
}
