package io.github.pulsebeat02.ezmediacore.json.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public final class PathAdapter extends TypeAdapter<Path> {

  @Override
  public void write(@NotNull final JsonWriter out, @NotNull final Path value) throws IOException {
    out.beginObject();
    out.name("path").value(value.toString());
    out.endObject();
  }

  @Override
  public Path read(@NotNull final JsonReader in) throws IOException {
    in.beginObject();
    String path = "";
    while (in.hasNext()) {
      if (in.nextName().equals("path")) {
        path = in.nextString();
        break;
      }
    }
    in.close();
    return Path.of(path);
  }
}
