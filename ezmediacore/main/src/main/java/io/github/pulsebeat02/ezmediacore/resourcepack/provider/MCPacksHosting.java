package io.github.pulsebeat02.ezmediacore.resourcepack.provider;

import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import io.github.pulsebeat02.ezmediacore.util.io.PathUtils;

public final class MCPacksHosting {

  private static final String WEBSITE_URL = "https://mc-packs.net/";
  private static final String DOWNLOAD_REGEX = "input[readonly][value^=https]";

  public MCPacksHosting() {
  }

  public String uploadPack(final Path zip) {
    final String name = PathUtils.getName(zip);
    try (final InputStream stream = Files.newInputStream(zip);
         final InputStream fast = new FastBufferedInputStream(stream)) {
      final Connection.Response uploadResponse = this.getResponse(name, fast);
      final Document document = uploadResponse.parse();
      return this.getDownloadUrl(document);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private String getDownloadUrl(final Document document) {
    final Elements elements = document.select(DOWNLOAD_REGEX);
    final Element element = elements.first();
    if (element == null) {
      throw new IllegalStateException("Download URL not found!");
    }
    return element.val();
  }

  private Response getResponse(final String name, final InputStream fast) throws IOException {
    return Jsoup.connect(WEBSITE_URL).data("file", name, fast).method(Method.POST).execute();
  }
}
