package io.github.pulsebeat02.ezmediacore.utility.misc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class NetworkUtils {

  private NetworkUtils() {}

  private static final String FALLBACK_IP = "127.0.0.1";
  public static final String HTTP_SERVER_IP = getPublicAddressInternal();

  private static String getPublicAddressInternal() {
    try (final HttpClient client = HttpClient.newHttpClient()) {
      final URI uri = URI.create("https://myexternalip.com/raw");
      final HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
      final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
      final HttpResponse<String> response = client.send(request, handler);
      return response.body();
    } catch (final IOException | InterruptedException e) {
      return FALLBACK_IP;
    }
  }

  public static String getPublicAddress() {
    return HTTP_SERVER_IP;
  }
}
