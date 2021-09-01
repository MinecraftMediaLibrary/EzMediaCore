package io.github.pulsebeat02.ezmediacore.libshout;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.annotations.Beta;
import com.google.common.io.LineReader;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import io.github.pulsebeat02.ezmediacore.libshout.exception.BadFileException;
import io.github.pulsebeat02.ezmediacore.libshout.exception.PushStreamException;
import io.github.pulsebeat02.ezmediacore.libshout.exception.ReadStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * libshout from source url to target ip port
 *
 * @author caorong
 */
public class Jlibshout {

  private final String user;
  private final String password;
  private final String targetHost;
  private final int targetPort;
  private final String mounter;
  private final MimeType mimeType;

  private String iceName = "no name";
  private String iceDesc = null;

  // target icecast  outputStream
  private OutputStream outputStream;
  private Socket socket;

  //  URL url = new URL("http://source:hackme@localhost:8030/res_1065_24");
  public Jlibshout(final String targetHost, final int targetPort, final String mounter)
      throws Exception {
    this("source", "hackme", targetHost, targetPort, mounter, MimeType.mp3);
  }

  //  URL url = new URL("http://source:hackme@localhost:8030/res_1065_24");
  public Jlibshout(
      final String targetHost, final int targetPort, final String mounter, final MimeType mimeType)
      throws Exception {
    this("source", "hackme", targetHost, targetPort, mounter, mimeType);
  }

  public Jlibshout(
      final String user,
      final String password,
      final String targetHost,
      final int targetPort,
      final String mounter,
      final MimeType mimeType)
      throws Exception {
    this.user = user;
    this.password = password;
    this.targetHost = targetHost;
    this.targetPort = targetPort;
    this.mounter = mounter;
    this.mimeType = mimeType;
    this.init();
  }

  public static void main(final String[] args) throws Exception {
    final Jlibshout jlibshout = new Jlibshout("localhost", 8030, "/javashout2");
    jlibshout.pushLiveHttpStream("http://192.168.3.52:8030/res_16_64");
  }

  public void setIceDesc(final String iceDesc) {
    this.iceDesc = iceDesc;
  }

  public void setIceName(final String iceName) {
    this.iceName = iceName;
  }

  private void init() throws Exception {
    try {
      this.socket = new Socket(this.targetHost, this.targetPort);
      this.outputStream = this.socket.getOutputStream();
      final PrintWriter out = new PrintWriter(this.outputStream, false);
      final InputStream inputStream = this.socket.getInputStream();

      // send an HTTP request to the web server
      out.println(String.format("SOURCE %s HTTP/1.0", this.mounter));
      out.println(
          String.format(
              "Authorization: Basic %s",
              HttpRequest.Base64.encode(this.user + ":" + this.password)));
      out.println("User-Agent: libshout/2.3.1");
      out.println(String.format("Content-Type: %s", this.mimeType.getContentType()));
      out.println(String.format("ice-name: %s", this.iceName));
      out.println("ice-public: 0");
      if (this.iceDesc != null) {
        out.println(String.format("ice-description: %s", this.iceDesc));
      }
      out.println();
      out.flush();

      // check if 404
      final LineReader lineReader = new LineReader(new InputStreamReader(inputStream));
      final String data = lineReader.readLine();

      this.handleResponse(data);
    } catch (final Exception e) {
      if (this.socket != null && !this.socket.isClosed()) {
        try {
          this.socket.close();
        } catch (final IOException e1) {
          // skip
        }
      }
      throw e;
    }
  }

  /**
   * push mp3 with sleep 32 4kb/s 64 8kb/s
   *
   * <p>unaccurate version
   *
   * @param mp3
   * @throws FileNotFoundException
   */
  @Beta
  public void pushMp3(final File mp3) throws IOException, BadFileException {
    final InputStream inputStream = new FileInputStream(mp3);
    try {
      final Mp3File mp3File = new Mp3File(mp3.getAbsolutePath());
      final long l = (long) ((double) mp3File.getLength() / mp3File.getLengthInSeconds());
      this.pushMediaFileAsStream(inputStream, (int) l + 1);
    } catch (final UnsupportedTagException e) {
      throw new BadFileException("parse mp3 error!", e);
    } catch (final InvalidDataException e) {
      throw new BadFileException("parse mp3 error!", e);
    }
  }

  @Beta
  public void pushMediaFileAsStream(final InputStream inputStream, final int bufferSize)
      throws IOException {
    final byte[] buffer = new byte[bufferSize];
    try {
      // mainloop, write every specified size, reduce syscall
      while (true) {
        final int readed = inputStream.read(buffer, 0, bufferSize);
        // EOF
        if (readed < 0) {
          break;
        } else {
          this.outputStream.write(buffer, 0, readed);
          this.outputStream.flush();
        }
        try {
          Thread.sleep(1000);
        } catch (final InterruptedException e) {
          // skip
        }
      }
    } catch (final IOException e) {
      try {
        inputStream.close();
      } catch (final IOException e1) {
        // skip
      }
      throw e;
    }
  }

  /**
   * push 'live' http stream support mp3, ogg, webm etc without sync
   *
   * <p>sync is dependence on source stream
   *
   * <p>this method will block until exception or reach EOF
   *
   * <p>
   *
   * <p>about buffersize is 8k e.g mp3 64kbit/s => 8kb/s => sleep time may be 1s will be just in
   * time
   *
   * <p>maybe throw:
   *
   * <p>ReadStreamEx (404), sourceStreamIOEx
   *
   * <p>
   *
   * @param url source live http stream url
   */
  public void pushLiveHttpStream(final String url) throws Exception {
    this.pushLiveHttpStream(url, 8192);
  }

  public void pushLiveHttpStream(final String url, final int bufferSize) throws Exception {
    final int code = HttpRequest.get(url).code();
    if (code != 200) {
      throw new ReadStreamException(code);
    }
    final InputStream inputStream = HttpRequest.get(url).stream();

    // resued buffer
    final byte[] buffer = new byte[bufferSize];
    try {
      int count = 0;
      // mainloop, write every specified size, reduce syscall
      while (true) {
        // 避免 最后一段
        if (inputStream.available() > bufferSize || count > 4) {
          final int readed = inputStream.read(buffer, 0, bufferSize);
          // EOF
          if (readed < 0) {
            break;
          } else {
            this.outputStream.write(buffer, 0, readed);
            this.outputStream.flush();
            count = 0;
          }
        }
        try {
          count++;
          Thread.sleep(400);
        } catch (final InterruptedException e) {
          // skip
        }
      }
    } catch (final Exception e) {
      try {
        inputStream.close();
      } catch (final IOException e1) {
        // skip
      }
      throw e;
    }
  }

  public void refresh() throws Exception {
    try {
      this.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    this.init();
  }

  public void close() throws IOException {
    if (this.socket != null) {
      this.socket.close();
    }
  }

  /**
   * 密码错误 HTTP/1.0 401 Authentication Required 同一个 mounter 重复推流 HTTP/1.0 403 Forbidden
   *
   * @param data
   */
  private void handleResponse(final String data) {
    if (data.startsWith("HTTP/1.0 401")) {
      throw new PushStreamException("auth error! check username and password");
    } else if (data.startsWith("HTTP/1.0 403 Forbidden")) {
      throw new PushStreamException("invalid operation! this stream is already streaming!");
    } else {
      if (!data.startsWith("HTTP/1.0 200")) {
        throw new PushStreamException("unknown exception! " + data);
      }
    }
  }
}
