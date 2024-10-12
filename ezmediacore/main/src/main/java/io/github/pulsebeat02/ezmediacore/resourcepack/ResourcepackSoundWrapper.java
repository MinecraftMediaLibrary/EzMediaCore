/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.resourcepack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import rewrite.json.GsonProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Contract;



public class ResourcepackSoundWrapper extends ResourcepackWrapper implements SoundPackWrapper {

  private final Map<String, Path> sounds;

  {
    this.sounds = new HashMap<>();
  }

  ResourcepackSoundWrapper(
       final Path path,
       final String description,
      final int format,
       final Path icon) {
    super(path, description, format, icon);
  }

  @Contract("_, _, _ -> new")
  public static  ResourcepackSoundWrapper ofSoundPack(
       final Path path,  final String description, final int format) {
    return ofSoundPack(path, description, format, null);
  }

  @Contract("_, _, _, _ -> new")
  public static  ResourcepackSoundWrapper ofSoundPack(
       final Path path,
       final String description,
      final int format,
       final Path icon) {
    return new ResourcepackSoundWrapper(path, description, format, icon);
  }

  @Override
  public void wrap() throws IOException {
    this.onPackStartWrap();
    this.addFiles();
    this.internalWrap();
    this.onPackFinishWrap();
  }

  private void addFiles() {
    this.sounds.forEach(this::addOgg);
    this.addFile("assets/minecraft/sounds.json", this.createSoundJson());
  }

  private void addOgg( final String key,  final Path value) {
    try {
      this.addFile("assets/minecraft/sounds/%s.ogg".formatted(key), value);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public void addSound( final String key,  final Path path) {
    this.sounds.put(key, path);
  }

  @Override
  public void removeSound( final String key) {
    this.sounds.remove(key);
  }

  @Override
  public byte  [] createSoundJson() {

    final JsonObject category = new JsonObject();
    final JsonObject type = new JsonObject();
    final JsonArray sounds = new JsonArray();

    this.addSounds(category, type, sounds);
    this.addCategory(category, sounds);

    return GsonProvider.getPretty().toJson(type).getBytes(StandardCharsets.UTF_8);
  }

  private void addCategory( final JsonObject category,  final JsonArray sounds) {
    category.add("sounds", sounds);
  }

  private void addSounds(
       final JsonObject category,
       final JsonObject type,
       final JsonArray sounds) {
    this.sounds.forEach((key, value) -> this.addSound0(category, type, sounds, key));
  }

  private void addSound0(
       final JsonObject category,
       final JsonObject type,
       final JsonArray sounds,
       final String key) {
    sounds.add(key);
    type.add("emc", category);
  }

  @Override
  public  Map<String, Path> listSounds() {
    return this.sounds;
  }
}
