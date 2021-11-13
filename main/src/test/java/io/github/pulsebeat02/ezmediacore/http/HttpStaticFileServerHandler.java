package io.github.pulsebeat02.ezmediacore.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SystemPropertyUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

/**
 * A simple handler that serves incoming HTTP requests to send their respective HTTP responses. It
 * also implements {@code 'If-Modified-Since'} header to take advantage of browser cache, as
 * described in <a href="https://tools.ietf.org/html/rfc2616#section-14.25">RFC 2616</a>.
 *
 * <h3>How Browser Caching Works</h3>
 *
 * Web browser caching works with HTTP headers as illustrated by the following sample:
 *
 * <ol>
 *   <li>Request #1 returns the content of {@code /file1.txt}.
 *   <li>Contents of {@code /file1.txt} is cached by the browser.
 *   <li>Request #2 for {@code /file1.txt} does not return the contents of the file again. Rather, a
 *       304 Not Modified is returned. This tells the browser to use the contents stored in its
 *       cache.
 *   <li>The server knows the file has not been modified because the {@code If-Modified-Since} date
 *       is the same as the file's last modified date.
 * </ol>
 *
 * <pre>
 * Request #1 Headers
 * ===================
 * GET /file1.txt HTTP/1.1
 *
 * Response #1 Headers
 * ===================
 * HTTP/1.1 200 OK
 * Date:               Tue, 01 Mar 2011 22:44:26 GMT
 * Last-Modified:      Wed, 30 Jun 2010 21:36:48 GMT
 * Expires:            Tue, 01 Mar 2012 22:44:26 GMT
 * Cache-Control:      private, max-age=31536000
 *
 * Request #2 Headers
 * ===================
 * GET /file1.txt HTTP/1.1
 * If-Modified-Since:  Wed, 30 Jun 2010 21:36:48 GMT
 *
 * Response #2 Headers
 * ===================
 * HTTP/1.1 304 Not Modified
 * Date:               Tue, 01 Mar 2011 22:44:28 GMT
 *
 * </pre>
 */
public class HttpStaticFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
  public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
  public static final int HTTP_CACHE_SECONDS = 60;

  private FullHttpRequest request;

  private boolean checkRequest(@NotNull final ChannelHandlerContext ctx) {
    if (!this.request.decoderResult().isSuccess()) {
      this.sendError(ctx, BAD_REQUEST);
      return true;
    }
    return false;
  }

  private boolean checkType(@NotNull final ChannelHandlerContext ctx) {
    if (!GET.equals(this.request.method())) {
      this.sendError(ctx, METHOD_NOT_ALLOWED);
      return true;
    }
    return false;
  }

  private boolean checkPath(@NotNull final ChannelHandlerContext ctx, @Nullable final String path) {
    if (path == null) {
      this.sendError(ctx, FORBIDDEN);
      return true;
    }
    return false;
  }

  private boolean visiblePath(@NotNull final ChannelHandlerContext ctx, @NotNull final Path path)
      throws IOException {
    if (Files.isHidden(path) || Files.notExists(path)) {
      this.sendError(ctx, NOT_FOUND);
      return true;
    }
    return false;
  }

  @Override
  public void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest request)
      throws Exception {

    this.request = request;

    if (this.checkRequest(ctx) || this.checkType(ctx)) {
      return;
    }

    final boolean keepAlive = HttpUtil.isKeepAlive(request);
    final String uri = request.uri();
    final String path = sanitizeUri(uri);

    if (this.checkPath(ctx, path)) {
      return;
    }

    final Path file = Path.of(path);
    if (this.visiblePath(ctx, file)) {
      return;
    }

    if (Files.isDirectory(file)) {
      if (uri.endsWith("/")) {
        this.sendListing(ctx, file, uri);
      } else {
        this.sendRedirect(ctx, uri + '/');
      }
      return;
    }

    if (!file.isFile()) {
      this.sendError(ctx, FORBIDDEN);
      return;
    }
    
    final String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
    if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
      final SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
      final Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
      final long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
      final long fileLastModifiedSeconds = file.lastModified() / 1000;
      if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
        this.sendNotModified(ctx);
        return;
      }
    }

    final RandomAccessFile raf;
    try {
      raf = new RandomAccessFile(file, "r");
    } catch (final FileNotFoundException ignore) {
      this.sendError(ctx, NOT_FOUND);
      return;
    }
    final long fileLength = raf.length();

    final HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
    HttpUtil.setContentLength(response, fileLength);
    setContentTypeHeader(response, file);
    setDateAndCacheHeaders(response, file);

    if (!keepAlive) {
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
    } else if (request.protocolVersion().equals(HTTP_1_0)) {
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
    }

    // Write the initial line and the header.
    ctx.write(response);

    // Write the content.
    final ChannelFuture sendFileFuture;
    final ChannelFuture lastContentFuture;
    if (ctx.pipeline().get(SslHandler.class) == null) {
      sendFileFuture =
          ctx.write(
              new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
      // Write the end marker.
      lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    } else {
      sendFileFuture =
          ctx.writeAndFlush(
              new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
              ctx.newProgressivePromise());
      // HttpChunkedInput will write the end marker (LastHttpContent) for us.
      lastContentFuture = sendFileFuture;
    }

    sendFileFuture.addListener(
        new ChannelProgressiveFutureListener() {
          @Override
          public void operationProgressed(
              final ChannelProgressiveFuture future, final long progress, final long total) {
            if (total < 0) { // total unknown
              System.err.println(future.channel() + " Transfer progress: " + progress);
            } else {
              System.err.println(
                  future.channel() + " Transfer progress: " + progress + " / " + total);
            }
          }

          @Override
          public void operationComplete(final ChannelProgressiveFuture future) {
            System.err.println(future.channel() + " Transfer complete.");
          }
        });

    // Decide whether to close the connection or not.
    if (!keepAlive) {
      // Close the connection when the whole content is written out.
      lastContentFuture.addListener(ChannelFutureListener.CLOSE);
    }
  }

  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
    cause.printStackTrace();
    if (ctx.channel().isActive()) {
      this.sendError(ctx, INTERNAL_SERVER_ERROR);
    }
  }

  private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

  private static String sanitizeUri(String uri) {
    // Decode the path.
    try {
      uri = URLDecoder.decode(uri, "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      throw new Error(e);
    }

    if (uri.isEmpty() || uri.charAt(0) != '/') {
      return null;
    }

    // Convert file separators.
    uri = uri.replace('/', File.separatorChar);

    // Simplistic dumb security check.
    // You will have to do something serious in the production environment.
    if (uri.contains(File.separator + '.')
        || uri.contains('.' + File.separator)
        || uri.charAt(0) == '.'
        || uri.charAt(uri.length() - 1) == '.'
        || INSECURE_URI.matcher(uri).matches()) {
      return null;
    }

    // Convert to absolute path.
    return SystemPropertyUtil.get("user.dir") + File.separator + uri;
  }

  private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[^-\\._]?[^<>&\\\"]*");

  private void sendListing(final ChannelHandlerContext ctx, final File dir, final String dirPath) {
    final StringBuilder buf =
        new StringBuilder()
            .append("<!DOCTYPE html>\r\n")
            .append("<html><head><meta charset='utf-8' /><title>")
            .append("Listing of: ")
            .append(dirPath)
            .append("</title></head><body>\r\n")
            .append("<h3>Listing of: ")
            .append(dirPath)
            .append("</h3>\r\n")
            .append("<ul>")
            .append("<li><a href=\"../\">..</a></li>\r\n");

    final File[] files = dir.listFiles();
    if (files != null) {
      for (final File f : files) {
        if (f.isHidden() || !f.canRead()) {
          continue;
        }

        final String name = f.getName();
        if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
          continue;
        }

        buf.append("<li><a href=\"")
            .append(name)
            .append("\">")
            .append(name)
            .append("</a></li>\r\n");
      }
    }

    buf.append("</ul></body></html>\r\n");

    final ByteBuf buffer = ctx.alloc().buffer(buf.length());
    buffer.writeCharSequence(buf.toString(), CharsetUtil.UTF_8);

    final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, buffer);
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");

    this.sendAndCleanupConnection(ctx, response);
  }

  private void sendRedirect(final ChannelHandlerContext ctx, final String newUri) {
    final FullHttpResponse response =
        new DefaultFullHttpResponse(HTTP_1_1, FOUND, Unpooled.EMPTY_BUFFER);
    response.headers().set(HttpHeaderNames.LOCATION, newUri);

    this.sendAndCleanupConnection(ctx, response);
  }

  private void sendError(final ChannelHandlerContext ctx, final HttpResponseStatus status) {
    final FullHttpResponse response =
        new DefaultFullHttpResponse(
            HTTP_1_1,
            status,
            Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

    this.sendAndCleanupConnection(ctx, response);
  }

  /**
   * When file timestamp is the same as what the browser is sending up, send a "304 Not Modified"
   *
   * @param ctx Context
   */
  private void sendNotModified(final ChannelHandlerContext ctx) {
    final FullHttpResponse response =
        new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED, Unpooled.EMPTY_BUFFER);
    setDateHeader(response);

    this.sendAndCleanupConnection(ctx, response);
  }

  /**
   * If Keep-Alive is disabled, attaches "Connection: close" header to the response and closes the
   * connection after the response being sent.
   */
  private void sendAndCleanupConnection(
      final ChannelHandlerContext ctx, final FullHttpResponse response) {
    final FullHttpRequest request = this.request;
    final boolean keepAlive = HttpUtil.isKeepAlive(request);
    HttpUtil.setContentLength(response, response.content().readableBytes());
    if (!keepAlive) {
      // We're going to close the connection as soon as the response is sent,
      // so we should also make it clear for the client.
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
    } else if (request.protocolVersion().equals(HTTP_1_0)) {
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
    }

    final ChannelFuture flushPromise = ctx.writeAndFlush(response);

    if (!keepAlive) {
      // Close the connection as soon as the response is sent.
      flushPromise.addListener(ChannelFutureListener.CLOSE);
    }
  }

  /**
   * Sets the Date header for the HTTP response
   *
   * @param response HTTP response
   */
  private static void setDateHeader(final FullHttpResponse response) {
    final SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
    dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

    final Calendar time = new GregorianCalendar();
    response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));
  }

  /**
   * Sets the Date and Cache headers for the HTTP Response
   *
   * @param response HTTP response
   * @param fileToCache file to extract content type
   */
  private static void setDateAndCacheHeaders(final HttpResponse response, final File fileToCache) {
    final SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
    dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

    // Date header
    final Calendar time = new GregorianCalendar();
    response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

    // Add cache headers
    time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
    response.headers().set(HttpHeaderNames.EXPIRES, dateFormatter.format(time.getTime()));
    response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
    response
        .headers()
        .set(
            HttpHeaderNames.LAST_MODIFIED,
            dateFormatter.format(new Date(fileToCache.lastModified())));
  }

  /**
   * Sets the content type header for the HTTP Response
   *
   * @param response HTTP response
   * @param file file to extract content type
   */
  private static void setContentTypeHeader(final HttpResponse response, final File file) {
    final MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
    response
        .headers()
        .set(HttpHeaderNames.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
  }
}
