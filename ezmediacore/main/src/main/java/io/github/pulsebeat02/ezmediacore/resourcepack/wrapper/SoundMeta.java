package io.github.pulsebeat02.ezmediacore.resourcepack.wrapper;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.pulsebeat02.ezmediacore.json.GsonProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class SoundMeta extends PackJSONFile {

  private final JsonObject root;
  private final Map<String, JsonObject> sounds;

  public SoundMeta() {
    this.root = new JsonObject();
    this.sounds = new HashMap<>();
  }

  public void writeSound(final String soundKey, final String soundName) {
    final JsonObject soundObject = new JsonObject();
    final JsonArray soundsArray = new JsonArray();
    final JsonObject soundDetail = new JsonObject();
    soundDetail.addProperty("name", soundName);
    soundsArray.add(soundDetail);
    soundObject.add("sounds", soundsArray);
    this.sounds.put(soundKey, soundObject);
  }

  @Override
  public void write(final String property, final Object obj) {
  }

  @Override
  public byte[] serialize() {
    final Set<Map.Entry<String, JsonObject>> entries = this.sounds.entrySet();
    for (final Map.Entry<String, JsonObject> entry : entries) {
      final String key = entry.getKey();
      final JsonObject value = entry.getValue();
      this.root.add(key, value);
    }
    final Gson gson = GsonProvider.getSimple();
    final String json = gson.toJson(this.root);
    return json.getBytes();
  }
}
