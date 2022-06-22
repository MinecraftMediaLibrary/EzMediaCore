package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.utility.io.FileUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ChunkDownloadTest {

  private static final HttpClient HTTP_CLIENT;

  static {
    HTTP_CLIENT = HttpClient.newHttpClient();
  }

  public static void main(final String[] args)
      throws URISyntaxException, IOException, InterruptedException {

    final Path test = Paths.get(System.getProperty("user.dir"), "test");
    FileUtils.createDirectoryIfNotExistsExceptionally(test);

    final String url = "https://rr4---sn-4g5e6nze.googlevideo.com/videoplayback?expire=1655885425&ei=EXqyYt_kEcLhgQf9-63wDg&ip=88.198.20.120&id=o-AClQS2n8H1YrlRP1bAwBd3_0IxLrwYjR42KcPohHihps&itag=249&source=youtube&requiressl=yes&mh=mq&mm=31%2C26&mn=sn-4g5e6nze%2Csn-f5f7ln7y&ms=au%2Conr&mv=u&mvi=4&pl=24&spc=4ocVC6WAku9SCoWHtnqzBZDu3k8ffqs&vprv=1&mime=audio%2Fwebm&ns=8PpnI93t59ccrUf_IPRUEGkG&gir=yes&clen=1705940&dur=287.021&lmt=1651174737916785&mt=1655863476&fvip=2&keepalive=yes&fexp=24001373%2C24007246&beids=23886204&c=WEB&txp=4532434&n=UQTde19pQKrOn52Qif&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cspc%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&sig=AOq0QJ8wRQIgb2xop4SESS_W1a1hKOskecDEywEk15RQq4dlhkGa8IMCIQCYmiH7jKooacMGHTsMquJjeP0uXfGBeCZD8xmpVYLF2g%3D%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl&lsig=AG3C_xAwRgIhANgrLbgSCy4fhnmq5CgCcjE7Eo5bgq7pcEXwxfpc1305AiEA8mNuDUZa8lgdIjClfTuG18wbNPF24pKcK3zwklV20rM%3D";

    final Path file = test.resolve("file.test");
    downloadUrlChunks(file, url);

  }

  public static @NotNull Path downloadUrlChunks(@NotNull final Path path, @NotNull final String url)
      throws IOException, InterruptedException {

    final HttpRequest.Builder builder = HttpRequest.newBuilder();
    builder.uri(URI.create(url));
    final HttpResponse<InputStream> stream = HTTP_CLIENT.send(builder.build(),
        BodyHandlers.ofInputStream());
    final InputStream input = stream.body();

    return downloadInChunks(input, path);
  }

  @Contract("_, _ -> param2")
  private static @NotNull Path downloadInChunks(@NotNull final InputStream source, @NotNull final Path path) throws IOException {
    final int chunk = 5 * 1_000 * 1_000;
    long start = 0;
    long end = chunk;
    final ReadableByteChannel in = Channels.newChannel(source);
    final FileChannel out = new FileOutputStream(path.toFile()).getChannel();
    final ByteBuffer buffer = ByteBuffer.allocate(chunk);
    while (in.read(buffer) > 0) {
      out.transferFrom(in, start, chunk);
      start = end;
      end += chunk;
    }
    return path;
  }
}
