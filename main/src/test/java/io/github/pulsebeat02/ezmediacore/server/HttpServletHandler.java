package io.github.pulsebeat02.ezmediacore.server;

import io.github.pulsebeat02.ezmediacore.utility.http.HttpUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public final class HttpServletHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  public static final DateFormat HTTP_DATE_FORMAT;
  public static final int HTTP_CACHE_SECONDS;

  static {
    HTTP_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    HTTP_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    HTTP_CACHE_SECONDS = 60;
  }

  private final Path directory;
  private FullHttpRequest request;

  public HttpServletHandler(@NotNull final Path directory) {
    this.directory = directory;
  }

  @Override
  public void channelRead0(
      @NotNull final ChannelHandlerContext ctx, @NotNull final FullHttpRequest request)
      throws Exception {

    this.request = request;

    if (this.isBadResult(ctx, request)) {
      return;
    }

    if (this.isInvalidRequest(ctx, request)) {
      return;
    }

    final String uri = request.uri();
    final String path = this.sanitizeUri(uri);
    if (this.isInvalidPath(ctx, path)) {
      return;
    }

    final Path file = Path.of(path);
    if (this.isForbiddenFile(ctx, file)) {
      return;
    }

    if (this.isDirectory(ctx, uri, file)) {
      return;
    }

    if (this.isInvalidFile(ctx, file)) {
      return;
    }

    if (this.isModifiedFile(ctx, request, file)) {
      return;
    }

    final RandomAccessFile raf = new RandomAccessFile(file.toString(), "r");
    final long size = raf.length();

    final HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
    this.setContentLengthHeader(response, size);
    this.setContentTypeHeader(response, file);
    this.setDateAndCacheHeaders(response, file);
    this.setConnectionStatus(response);
    this.writeHttpResponse(ctx, response);
    this.handleFileTransfer(ctx, raf);
  }

  private boolean isModifiedFile(
      @NotNull final ChannelHandlerContext ctx,
      @NotNull final FullHttpRequest request,
      @NotNull final Path file)
      throws ParseException, IOException {
    final String since = request.headers().get(IF_MODIFIED_SINCE);
    if (since != null && !since.isEmpty()) {
      final Date date = HTTP_DATE_FORMAT.parse(since);
      final long seconds = date.getTime() / 1000;
      final long modified = Files.getLastModifiedTime(file).toMillis() / 1000;
      if (seconds == modified) {
        this.sendNotModified(ctx);
        return true;
      }
    }
    return false;
  }

  private void handleFileTransfer(
      @NotNull final ChannelHandlerContext ctx, @NotNull final RandomAccessFile raf)
      throws IOException {
    final boolean keepAlive = HttpUtil.isKeepAlive(this.request);
    final long size = raf.length();
    final ChannelFuture lastContentFuture = this.getFileContentHandler(ctx, raf, size);
    if (!keepAlive) {
      lastContentFuture.addListener(ChannelFutureListener.CLOSE);
    }
  }

  private ChannelFuture getFileContentHandler(
      @NotNull final ChannelHandlerContext ctx, final RandomAccessFile raf, final long size)
      throws IOException {
    return this.isSslEnabled(ctx)
        ? this.getFileChunkFuture(ctx, raf, size)
        : this.getInsecureFuture(ctx);
  }

  private ChannelFuture getInsecureFuture(@NotNull final ChannelHandlerContext ctx) {
    return ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
  }

  private ChannelFuture getFileChunkFuture(
      @NotNull final ChannelHandlerContext ctx,
      @NotNull final RandomAccessFile raf,
      final long size)
      throws IOException {
    final ChannelFuture lastContentFuture;
    final ChunkedFile chunked = new ChunkedFile(raf, 0, size, 8192);
    final HttpChunkedInput input = new HttpChunkedInput(chunked);
    lastContentFuture = ctx.writeAndFlush(input, ctx.newProgressivePromise());
    return lastContentFuture;
  }

  private boolean isSslEnabled(@NotNull final ChannelHandlerContext ctx) {
    return ctx.pipeline().get(SslHandler.class) != null;
  }

  private boolean isInvalidFile(@NotNull final ChannelHandlerContext ctx, final Path file) {
    if (!Files.isRegularFile(file)) {
      this.sendError(ctx, FORBIDDEN);
      return true;
    }
    return false;
  }

  private boolean isDirectory(
      @NotNull final ChannelHandlerContext ctx, final String uri, final Path file)
      throws IOException {
    if (Files.isDirectory(file)) {
      if (uri.endsWith("/")) {
        this.sendListing(ctx, file);
      } else {
        this.sendRedirect(ctx, uri + '/');
      }
      return true;
    }
    return false;
  }

  private boolean isForbiddenFile(@NotNull final ChannelHandlerContext ctx, final Path file)
      throws IOException {
    if (Files.isHidden(file) || Files.notExists(file)) {
      this.sendError(ctx, NOT_FOUND);
      return true;
    }
    return false;
  }

  private boolean isInvalidPath(@NotNull final ChannelHandlerContext ctx, final String path) {
    if (path == null) {
      this.sendError(ctx, FORBIDDEN);
      return true;
    }
    return false;
  }

  private boolean isInvalidRequest(
      @NotNull final ChannelHandlerContext ctx, @NotNull final FullHttpRequest request) {
    if (!GET.equals(request.method())) {
      this.sendError(ctx, METHOD_NOT_ALLOWED);
      return true;
    }
    return false;
  }

  private boolean isBadResult(
      @NotNull final ChannelHandlerContext ctx, @NotNull final FullHttpRequest request) {
    if (!request.decoderResult().isSuccess()) {
      this.sendError(ctx, BAD_REQUEST);
      return true;
    }
    return false;
  }

  private void writeHttpResponse(
      @NotNull final ChannelHandlerContext ctx, @NotNull final HttpResponse response) {
    ctx.write(response);
  }

  @Override
  public void exceptionCaught(
      @NotNull final ChannelHandlerContext ctx, @NotNull final Throwable cause) {
    cause.printStackTrace();
    if (ctx.channel().isActive()) {
      this.sendError(ctx, INTERNAL_SERVER_ERROR);
    }
  }

  private @Nullable String sanitizeUri(@NotNull final String uri) {

    final String url = URLDecoder.decode(uri, StandardCharsets.UTF_8);
    if (uri.isEmpty() || uri.charAt(0) != '/') {
      return null;
    }

    final String replaced = url.replace('/', File.separatorChar);
    if (HttpUtils.checkTreeAttack(replaced)) {
      return null;
    }

    return this.directory.resolve(uri).toString();
  }

  private void sendListing(@NotNull final ChannelHandlerContext ctx, @NotNull final Path dir)
      throws IOException {
    final StringBuilder buf = new StringBuilder(HttpUtils.createBaseHtmlContent(dir));
    this.appendFullFileList(buf, dir);
    this.appendHtmlBody(buf);
    final ByteBuf buffer = this.createContentBuffer(ctx, buf);
    final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, buffer);
    this.setHtmlHeader(response);
    this.sendAndCleanupConnection(ctx, response);
  }

  private void appendHtmlBody(@NotNull final StringBuilder buf) {
    buf.append(HttpUtils.HTML_BODY);
  }

  private void appendFullFileList(@NotNull final StringBuilder buf, @NotNull final Path dir)
      throws IOException {
    try (final Stream<Path> files = Files.walk(dir, 1)) {
      final List<Path> valid = files.toList();
      for (final Path path : valid) {
        this.appendFileListing(buf, path);
      }
    }
  }

  private void appendFileListing(@NotNull final StringBuilder buf, @NotNull final Path path)
      throws IOException {

    if (Files.isHidden(path)) {
      return;
    }

    if (!Files.isReadable(path)) {
      return;
    }

    if (!HttpUtils.isValidFileName(path)) {
      return;
    }

    buf.append(HttpUtils.createFileHtmlContent(path));
  }

  @NotNull
  private ByteBuf createContentBuffer(
      @NotNull final ChannelHandlerContext ctx, @NotNull final StringBuilder buf) {
    final ByteBuf buffer = ctx.alloc().buffer(buf.length());
    buffer.writeCharSequence(buf.toString(), CharsetUtil.UTF_8);
    return buffer;
  }

  private void setHtmlHeader(@NotNull final HttpResponse response) {
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
  }

  private void sendRedirect(
      @NotNull final ChannelHandlerContext ctx, @NotNull final String newUri) {
    final ByteBuf buffer = Unpooled.EMPTY_BUFFER;
    final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND, buffer);
    response.headers().set(HttpHeaderNames.LOCATION, newUri);
    this.sendAndCleanupConnection(ctx, response);
  }

  private void sendError(
      @NotNull final ChannelHandlerContext ctx, @NotNull final HttpResponseStatus status) {
    final String message = "Failure: %s\r\n".formatted(status);
    final ByteBuf buf = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, buf);
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
    this.sendAndCleanupConnection(ctx, response);
  }

  private void sendNotModified(@NotNull final ChannelHandlerContext ctx) {
    final ByteBuf buffer = Unpooled.EMPTY_BUFFER;
    final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED, buffer);
    this.setDateHeader(response);
    this.sendAndCleanupConnection(ctx, response);
  }

  private void sendAndCleanupConnection(
      @NotNull final ChannelHandlerContext ctx, @NotNull final FullHttpResponse response) {
    this.setContentLengthHeader(response, response.content().readableBytes());
    this.setConnectionStatus(response);
    this.flushConnection(ctx, response);
  }

  private void flushConnection(
      @NotNull final ChannelHandlerContext ctx, @NotNull final HttpResponse response) {
    final ChannelFuture flushPromise = ctx.writeAndFlush(response);
    final boolean keepAlive = HttpUtil.isKeepAlive(this.request);
    if (!keepAlive) {
      flushPromise.addListener(ChannelFutureListener.CLOSE);
    }
  }

  private void setConnectionStatus(@NotNull final HttpResponse response) {
    final FullHttpRequest request = this.request;
    final boolean keepAlive = HttpUtil.isKeepAlive(request);
    if (!keepAlive) {
      response.headers().set(CONNECTION, CLOSE);
    } else if (request.protocolVersion().equals(HTTP_1_0)) {
      response.headers().set(CONNECTION, KEEP_ALIVE);
    }
  }

  private void setContentLengthHeader(@NotNull final HttpResponse response, final long length) {
    response.headers().set(CONTENT_LENGTH, length);
  }

  private void setDateHeader(@NotNull final HttpResponse response) {
    final String date = HTTP_DATE_FORMAT.format(new GregorianCalendar().getTime());
    response.headers().set(DATE, date);
  }

  private void setDateAndCacheHeaders(
      @NotNull final HttpResponse response, @NotNull final Path path) throws IOException {
    final Calendar calendar = new GregorianCalendar();
    this.addDate(response, calendar);
    calendar.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
    this.addCacheExpireDate(response, calendar);
    this.addModifiedDate(response, path);
  }

  private void addDate(@NotNull final HttpResponse response, @NotNull final Calendar calendar) {
    final String time = HTTP_DATE_FORMAT.format(calendar.getTime());
    response.headers().set(DATE, time);
  }

  private void addCacheExpireDate(
      @NotNull final HttpResponse response, @NotNull final Calendar calendar) {
    final String expire = HTTP_DATE_FORMAT.format(calendar.getTime());
    response.headers().set(EXPIRES, expire);
    response.headers().set(CACHE_CONTROL, "private, max-age=%s".formatted(HTTP_CACHE_SECONDS));
  }

  private void addModifiedDate(@NotNull final HttpResponse response, @NotNull final Path path)
      throws IOException {
    final Date time = new Date(Files.getLastModifiedTime(path).toMillis());
    final String modified = HTTP_DATE_FORMAT.format(time);
    response.headers().set(LAST_MODIFIED, modified);
  }

  private void setContentTypeHeader(@NotNull final HttpResponse response, @NotNull final Path file)
      throws IOException {
    response.headers().set(CONTENT_TYPE, Files.probeContentType(file));
  }
}
