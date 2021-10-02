package io.github.deluxemediaplugin;

import com.google.gson.Gson;
import io.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommandAttributes;
import io.github.pulsebeat02.deluxemediaplugin.json.GsonProvider;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JSONVideoAttributesTest {

  public static void main(final String[] args) throws IOException {
    final Gson gson = GsonProvider.getGson();
    System.out.println(gson.toJson(new VideoCommandAttributes()));
    try (final BufferedReader writer =
        Files.newBufferedReader(
            Path.of(
                System.getProperty("user.dir"),
                "deluxemediaplugin/src/main/resources/data/video-attributes.json"))) {
      System.out.println(gson.fromJson(writer, VideoCommandAttributes.class));
    }
  }
}
