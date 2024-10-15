package io.github.pulsebeat02.ezmediacore.resourcepack.wrapper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.pulsebeat02.ezmediacore.json.GsonProvider;

public final class PackMeta extends PackJSONFile {

  private final JsonObject root;
  private final JsonObject pack;

  public PackMeta() {
    this.root = new JsonObject();
    this.pack = new JsonObject();
  }

  public void writeDescription(final String description) {
    this.write("description", description);
  }

  public void writeFormat(final int format) {
    this.write("pack_format", format);
  }

  @Override
  public void write(final String property, final Object obj) {
    final String value = obj.toString();
    this.pack.addProperty(property, value);
  }

  @Override
  public byte[] serialize() {
    this.root.add("pack", this.pack);
    final Gson gson = GsonProvider.getSimple();
    final String json = gson.toJson(this.root);
    return json.getBytes();
  }
}
