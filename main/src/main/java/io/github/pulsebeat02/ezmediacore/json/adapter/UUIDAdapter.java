package io.github.pulsebeat02.ezmediacore.json.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public final class UUIDAdapter extends TypeAdapter<UUID> {

  @Override
  public void write(@NotNull final JsonWriter out, @NotNull final UUID value) throws IOException {
    out.beginObject();
    out.name("uuid").value(value.toString());
    out.endObject();
  }

  @Override
  public UUID read(@NotNull final JsonReader in) throws IOException {
    in.beginObject();
    String id = "";
    while (in.hasNext()) {
      if (in.nextName().equals("uuid")) {
        id = in.nextString();
        break;
      }
    }
    in.close();
    return UUID.fromString(id);
  }
}
