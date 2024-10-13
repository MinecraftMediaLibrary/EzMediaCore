/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package rewrite.http.netty;

import static io.netty.handler.codec.http.HttpHeaderNames.CACHE_CONTROL;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.DATE;
import static io.netty.handler.codec.http.HttpHeaderNames.EXPIRES;
import static io.netty.handler.codec.http.HttpHeaderNames.IF_MODIFIED_SINCE;
import static io.netty.handler.codec.http.HttpHeaderNames.LAST_MODIFIED;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_MODIFIED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.util.function.Predicate.not;

import io.github.pulsebeat02.ezmediacore.utility.io.PathUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;



public final class HttpServletHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  public static final DateFormat HTTP_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
  public static final int HTTP_CACHE_SECONDS = 60;
  private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
  private static final Pattern VALID_FILE_NAME = Pattern.compile("[^-._]?[^<>&\"]*");
  public static final String BASE_FOLDER_HTML_CONTENT;
  public static final String FILE_HTML_CONTENT = "<li><a href=\"%s\">%s</a></li>\r\n";
  public static final String HTML_BODY = "</ul></body></html>\r\n";

  static {
    HTTP_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    BASE_FOLDER_HTML_CONTENT =
            """
                    <!DOCTYPE html>\r
                    <html><head><meta charset='utf-8' /><title>Listing of: %s</title></head><body>\r
                    <h3>Listing of: %s</h3>\r
                    <ul><li><a href="../">..</a></li>\r
                    """;
  }

  private final Path directory;

  private FullHttpRequest request;

  public HttpServletHandler(final Path directory) {
    this.directory = directory;
  }

  public boolean isValidFileName( final Path path) {
    final String name = PathUtils.getName(path);
    final Matcher matcher = VALID_FILE_NAME.matcher(name);
    return matcher.matches();
  }

  public String createBaseHtmlContent(final Path directory) {
    return BASE_FOLDER_HTML_CONTENT.formatted(directory, directory);
  }

  public String createFileHtmlContent(  final Path parent,  final Path file) {
    final Path relative = parent.relativize(file);
    return FILE_HTML_CONTENT.formatted(relative, relative);
  }

  @Override
  public void channelRead0(
       final ChannelHandlerContext ctx,  final FullHttpRequest request)
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
       final ChannelHandlerContext ctx,
       final FullHttpRequest request,
       final Path file)
      throws ParseException, IOException {
    final String since = request.headers().get(IF_MODIFIED_SINCE);
    if (since != null && !since.isEmpty()) {
      final Date date = HTTP_DATE_FORMAT.parse(since);
      final long seconds = date.getTime() / 1000;
      final FileTime fileTime = Files.getLastModifiedTime(file);
      final long ms = fileTime.toMillis();
      final long modified = ms / 1000;
      if (seconds == modified) {
        this.sendNotModified(ctx);
        return true;
      }
    }
    return false;
  }

  private void handleFileTransfer(
       final ChannelHandlerContext ctx,  final RandomAccessFile raf)
      throws IOException {
    final boolean keepAlive = HttpUtil.isKeepAlive(this.request);
    final long size = raf.length();
    final ChannelFuture lastContentFuture = this.getFileContentHandler(ctx, raf, size);
    if (!keepAlive) {
      lastContentFuture.addListener(ChannelFutureListener.CLOSE);
    }
  }

  private ChannelFuture getFileContentHandler(
       final ChannelHandlerContext ctx, final RandomAccessFile raf, final long size)
      throws IOException {
    return this.getFileChunkFuture(ctx, raf, size);
  }

  private ChannelFuture getFileChunkFuture(
       final ChannelHandlerContext ctx,
       final RandomAccessFile raf,
      final long size)
      throws IOException {
    final ChannelFuture lastContentFuture;
    final ChunkedFile chunked = new ChunkedFile(raf, 0, size, 8192);
    final HttpChunkedInput input = new HttpChunkedInput(chunked);
    final ChannelProgressivePromise promise = ctx.newProgressivePromise();
    lastContentFuture = ctx.writeAndFlush(input, promise);
    return lastContentFuture;
  }

  private boolean isInvalidFile( final ChannelHandlerContext ctx, final Path file) {
    if (!Files.isRegularFile(file)) {
      this.sendError(ctx, FORBIDDEN);
      return true;
    }
    return false;
  }

  private boolean isDirectory(
       final ChannelHandlerContext ctx, final String uri, final Path file)
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

  private boolean isForbiddenFile( final ChannelHandlerContext ctx, final Path file)
      throws IOException {
    if (Files.notExists(file) || Files.isHidden(file)) {
      this.sendError(ctx, NOT_FOUND);
      return true;
    }
    return false;
  }

  private boolean isInvalidPath( final ChannelHandlerContext ctx, final String path) {
    if (path == null) {
      this.sendError(ctx, FORBIDDEN);
      return true;
    }
    return false;
  }

  private boolean isInvalidRequest(
       final ChannelHandlerContext ctx,  final FullHttpRequest request) {
    if (!GET.equals(request.method())) {
      this.sendError(ctx, METHOD_NOT_ALLOWED);
      return true;
    }
    return false;
  }

  private boolean isBadResult(
       final ChannelHandlerContext ctx,  final FullHttpRequest request) {
    if (!request.decoderResult().isSuccess()) {
      this.sendError(ctx, BAD_REQUEST);
      return true;
    }
    return false;
  }

  private void writeHttpResponse(
       final ChannelHandlerContext ctx,  final HttpResponse response) {
    ctx.write(response);
  }

  @Override
  public void exceptionCaught(
       final ChannelHandlerContext ctx,  final Throwable cause) {
    final Channel channel = ctx.channel();
    if (channel.isActive()) {
      this.sendError(ctx, INTERNAL_SERVER_ERROR);
    }
  }

  private  String sanitizeUri( final String uri) {

    final String url = URLDecoder.decode(uri, StandardCharsets.UTF_8);
    if (url.isEmpty() || url.charAt(0) != '/') {
      return null;
    }

    if (this.checkTreeAttack(url)) {
      return null;
    }

    final String resolve = url.startsWith("/") ? url.substring(1) : url;

    return this.directory.resolve(resolve).toString();
  }

  public boolean checkTreeAttack( final String result) {
    return result.contains("/.")
            || result.contains("./")
            || result.charAt(0) == '.'
            || result.charAt(result.length() - 1) == '.'
            || INSECURE_URI.matcher(result).matches();
  }

  private void sendListing( final ChannelHandlerContext ctx,  final Path dir)
      throws IOException {
    final StringBuilder buf = new StringBuilder(this.createBaseHtmlContent(dir));
    this.appendFullFileList(buf, dir);
    this.appendHtmlBody(buf);
    final ByteBuf buffer = this.createContentBuffer(ctx, buf);
    final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, buffer);
    this.setHtmlHeader(response);
    this.sendAndCleanupConnection(ctx, response);
  }

  private void appendHtmlBody( final StringBuilder buf) {
    buf.append(HTML_BODY);
  }

  private void appendFullFileList( final StringBuilder buf,  final Path dir)
      throws IOException {
    try (final Stream<Path> files = Files.walk(dir, 1)) {
      final Predicate<Path> predicate = not(path -> path.equals(dir));
      final List<Path> valid = files.filter(predicate).toList();
      for (final Path path : valid) {
        this.appendFileListing(buf, path);
      }
    }
  }

  private void appendFileListing( final StringBuilder buf,  final Path path)
      throws IOException {

    if (Files.isHidden(path)) {
      return;
    }

    if (!Files.isReadable(path)) {
      return;
    }

    if (!this.isValidFileName(path)) {
      return;
    }

    final String content = this.createFileHtmlContent(this.directory, path);
    buf.append(content);
  }


  private ByteBuf createContentBuffer(
       final ChannelHandlerContext ctx,  final StringBuilder buf) {
    final int len = buf.length();
    final ByteBufAllocator alloc = ctx.alloc();
    final ByteBuf buffer = alloc.buffer(len);
    final String content = buf.toString();
    buffer.writeCharSequence(content, CharsetUtil.UTF_8);
    return buffer;
  }

  private void setHtmlHeader( final HttpResponse response) {
    final HttpHeaders headers = response.headers();
    headers.set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
  }

  private void sendRedirect(
       final ChannelHandlerContext ctx,  final String newUri) {
    final ByteBuf buffer = Unpooled.EMPTY_BUFFER;
    final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND, buffer);
    response.headers().set(HttpHeaderNames.LOCATION, newUri);
    this.sendAndCleanupConnection(ctx, response);
  }

  private void sendError(
       final ChannelHandlerContext ctx,  final HttpResponseStatus status) {
    final String message = "Failure: %s\r\n".formatted(status);
    final ByteBuf buf = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, buf);
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
    this.sendAndCleanupConnection(ctx, response);
  }

  private void sendNotModified( final ChannelHandlerContext ctx) {
    final ByteBuf buffer = Unpooled.EMPTY_BUFFER;
    final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED, buffer);
    this.setDateHeader(response);
    this.sendAndCleanupConnection(ctx, response);
  }

  private void sendAndCleanupConnection(
       final ChannelHandlerContext ctx,  final FullHttpResponse response) {
    this.setContentLengthHeader(response, response.content().readableBytes());
    this.setConnectionStatus(response);
    this.flushConnection(ctx, response);
  }

  private void flushConnection(
       final ChannelHandlerContext ctx,  final HttpResponse response) {
    final ChannelFuture flushPromise = ctx.writeAndFlush(response);
    final boolean keepAlive = HttpUtil.isKeepAlive(this.request);
    if (!keepAlive) {
      flushPromise.addListener(ChannelFutureListener.CLOSE);
    }
  }

  private void setConnectionStatus( final HttpResponse response) {
    final FullHttpRequest request = this.request;
    final boolean keepAlive = HttpUtil.isKeepAlive(request);
    final HttpHeaders headers = response.headers();
    final HttpVersion version = request.protocolVersion();
    if (!keepAlive) {
      headers.set(CONNECTION, CLOSE);
    } else if (version.equals(HTTP_1_0)) {
      headers.set(CONNECTION, KEEP_ALIVE);
    }
  }

  private void setContentLengthHeader( final HttpResponse response, final long length) {
    final HttpHeaders headers = response.headers();
    headers.set(CONTENT_LENGTH, length);
  }

  private void setDateHeader( final HttpResponse response) {
    final GregorianCalendar calendar = new GregorianCalendar();
    final Date time = calendar.getTime();
    final String date = HTTP_DATE_FORMAT.format(time);
    final HttpHeaders headers = response.headers();
    headers.set(DATE, date);
  }

  private void setDateAndCacheHeaders(
       final HttpResponse response,  final Path path) throws IOException {
    final Calendar calendar = new GregorianCalendar();
    this.addDate(response, calendar);
    calendar.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
    this.addCacheExpireDate(response, calendar);
    this.addModifiedDate(response, path);
  }

  private void addDate( final HttpResponse response,  final Calendar calendar) {
    final Date date = calendar.getTime();
    final String time = HTTP_DATE_FORMAT.format(date);
    final HttpHeaders headers = response.headers();
    headers.set(DATE, time);
  }

  private void addCacheExpireDate(
       final HttpResponse response,  final Calendar calendar) {
    final Date time = calendar.getTime();
    final String expire = HTTP_DATE_FORMAT.format(time);
    final HttpHeaders headers = response.headers();
    headers.set(EXPIRES, expire);
    headers.set(CACHE_CONTROL, "private, max-age=%s".formatted(HTTP_CACHE_SECONDS));
  }

  private void addModifiedDate( final HttpResponse response,  final Path path)
      throws IOException {
    final FileTime fileTime = Files.getLastModifiedTime(path);
    final long ms = fileTime.toMillis();
    final Date date = new Date(ms);
    final String modified = HTTP_DATE_FORMAT.format(date);
    final HttpHeaders headers = response.headers();
    headers.set(LAST_MODIFIED, modified);
  }

  private void setContentTypeHeader( final HttpResponse response,  final Path file)
      throws IOException {
    try (final InputStream is = Files.newInputStream(file)) {
      final HttpHeaders headers = response.headers();
      final String type = URLConnection.guessContentTypeFromStream(is);
      final String corrected = type == null ? "application/octet-stream" : type;
      headers.set(CONTENT_TYPE, corrected);
    }
  }
}
